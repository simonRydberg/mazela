/*
 * Copyright 2017 Johan Maasing <johan@zoom.nu>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.mejsla.camp.mazela.network.server.grizzly;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.memory.HeapMemoryManager;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.common.ConnectionID;
import se.mejsla.camp.mazela.network.common.MessageUtilities;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;
import se.mejsla.camp.mazela.network.server.IncomingMessage;
import se.mejsla.camp.mazela.network.server.NetworkServer;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class GrizzlyNetworkServer extends AbstractService implements NetworkServer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final int serverPort;
    private final Thread sendThread;
    private final CopyOnWriteArrayList<Consumer<ConnectionID>> droppedConnectionListeners
            = new CopyOnWriteArrayList<>();

    /**
     * Holds messages coming in through the network.
     */
    private final ConcurrentLinkedQueue<IncomingMessage> incomingMessageQueue = new ConcurrentLinkedQueue<>();
    /**
     * Holds messages that are to be sent on the network.
     */
    private final ArrayBlockingQueue<OutgoingMessage> outgoingMessageQueue;

    /**
     * Associates the recipient IDs with the grizzly connection to that
     * recipient.
     */
    private final ConcurrentHashMap<ConnectionID, Connection> knownConnections
            = new ConcurrentHashMap<>();

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final ExecutorService backgroundExecutor;
    private TCPNIOTransport transport;

    /**
     * Initialise the server. The server is not usable until it has been
     * started.
     *
     * @param maxOutgoingMessages The maximum number of outgoing messages to
     * queue. Must be larger than 0 and less than 1000.
     * @param threadFactory The thread factory to use to produce internal
     * executor threads. May not be null.
     * @param serverPort The port number on localhost the server will listen to.
     */
    public GrizzlyNetworkServer(
            final int maxOutgoingMessages,
            final ThreadFactory threadFactory,
            final int serverPort
    ) {
        Preconditions.checkArgument(
                maxOutgoingMessages > 0,
                "Max outgoing messages must be a positive integer"
        );
        Preconditions.checkArgument(
                maxOutgoingMessages < 1000,
                "Max outgoing messages must be less than 1000"
        );

        Preconditions.checkArgument(
                serverPort > 1024,
                "Listen port must be > 1024"
        );

        this.serverPort = serverPort;
        this.outgoingMessageQueue
                = new ArrayBlockingQueue<>(maxOutgoingMessages, false);
        this.backgroundExecutor = Executors.newCachedThreadPool(threadFactory);

        sendThread = Preconditions.checkNotNull(threadFactory).newThread(() -> {
            notifyStarted();
            while (!GrizzlyNetworkServer.this.shutdown.get()) {
                try {
                    OutgoingMessage msg = outgoingMessageQueue.poll(
                            300,
                            TimeUnit.MILLISECONDS
                    );
                    while (msg != null) {
                        // Send the message
                        msg.send();
                        msg = outgoingMessageQueue.poll();
                    }
                } catch (InterruptedException ex) {
                    log.error("Thread waiting for outgoing messages was interrupted, shutting down network service", ex);
                    GrizzlyNetworkServer.this.shutdown.set(true);
                    // Preserve interrupt flag
                    Thread.currentThread().interrupt();
                }
            }

            log.debug("Shutting down background executor");
            GrizzlyNetworkServer.this.backgroundExecutor.shutdown();
            try {
                backgroundExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                log.error("Thread waiting for background executor was interrupted, shutting down executor", ex);
                backgroundExecutor.shutdownNow();
                // Preserve interrupt flag
                Thread.currentThread().interrupt();
            }

            log.debug("Shutting down grizzly transport");
            transport.shutdown(2, TimeUnit.SECONDS);
            notifyStopped();

            log.debug("Network send thread terminated");
        });
    }

    @Override
    protected void doStart() {
        try {
            log.debug("Starting network server");
            final FilterChainBuilder serverFilterChainBuilder
                    = FilterChainBuilder.stateless();
            serverFilterChainBuilder
                    .add(new TransportFilter())
                    .add(new ServerFilter(this));
            transport = TCPNIOTransportBuilder.newInstance().setTcpNoDelay(true).build();
            transport.setProcessor(serverFilterChainBuilder.build());

            log.debug("Binding to port: {}", this.serverPort);
            transport.bind(this.serverPort);
            transport.start();

            this.sendThread.start();
        } catch (IOException ex) {
            log.error("Unable to start the network server. Unable to bind to port: {}", this.serverPort);
            notifyFailed(ex);
        }
    }

    @Override
    protected void doStop() {
        log.debug("Stopping network server");
        this.backgroundExecutor.shutdown();
        this.shutdown.set(true);
    }

    @Override
    public IncomingMessage getIncomingMessage() {
        return this.incomingMessageQueue.poll();
    }

    @Override
    public void sendMessage(ByteBuffer data, ConnectionID recipient)
            throws NotConnectedException, OutgoingQueueFullException {
        final Connection connection = this.knownConnections.get(
                Preconditions.checkNotNull(recipient, "recipients may not be null")
        );
        if (connection == null) {
            throw new NotConnectedException("Client with ID: " + recipient + " is not connected");
        } else {
            final Buffer grizzlyBuffer
                    = HeapMemoryManager.DEFAULT_MEMORY_MANAGER.allocate(Integer.BYTES + data.remaining());
            grizzlyBuffer.putInt(MessageUtilities.getMagicMarker());
            grizzlyBuffer.put(data);
            grizzlyBuffer.flip();
            data.flip();
            final OutgoingMessage message = new OutgoingMessage(connection, grizzlyBuffer);
            if (!this.outgoingMessageQueue.offer(message)) {
                throw new OutgoingQueueFullException("Unable to queue outgoing message, queue full");
            }
        }
    }

    /**
     * Callback from the grizzly server filter when a new connection has been
     * accepted and assigned an ID.
     *
     * @param id
     * @param conn
     */
    void registerConnection(ConnectionID id, Connection conn) {
        if (id != null && conn != null) {
            log.debug("Register connection with ID: {}", id);
            final Connection previousConnection
                    = this.knownConnections.put(id, conn);
            if (previousConnection != null) {
                log.error("There was an existing connection for id: {}", id);
            }
        }
    }

    /**
     * Callback from the grizzly server filter when a new message has arrived
     *
     * @param incomingMessage
     */
    void addIncomingMessage(IncomingMessage incomingMessage) {
        if (incomingMessage != null) {
            this.incomingMessageQueue.add(incomingMessage);
        }
    }

    /**
     * Callback from the grizzly server filter when a connection should be
     * dropped
     *
     * @param connectionID
     */
    void dropConnection(final ConnectionID connectionID) {
        if (connectionID != null) {
            backgroundExecutor.execute(() -> {
                this.knownConnections.remove(connectionID);
            });
            for (Consumer<ConnectionID> consumer : droppedConnectionListeners) {
                backgroundExecutor.execute(() -> {
                    try {
                        consumer.accept(connectionID);
                    } catch (Exception e) {
                        log.error("Dropped connection listener is poorly coded and throws unchecked exceptions.", e);
                    }
                });
            }
        }
    }

    @Override
    public void addConnectionDroppedListener(Consumer<ConnectionID> l) {
        if (l != null) {
            this.droppedConnectionListeners.add(l);
        }
    }
}
