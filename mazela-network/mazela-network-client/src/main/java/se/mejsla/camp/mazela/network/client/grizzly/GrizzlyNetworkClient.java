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
package se.mejsla.camp.mazela.network.client.grizzly;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.memory.HeapMemoryManager;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.streams.AbstractStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.client.NetworkClient;
import se.mejsla.camp.mazela.network.common.MessageUtilities;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class GrizzlyNetworkClient extends AbstractService implements NetworkClient {

    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Logger log = LoggerFactory.getLogger(getClass());
    private TCPNIOTransport transport;
    private Connection connection;
    private final Thread sendThread;
    private final CountDownLatch sendThreadRunning = new CountDownLatch(1);

    /**
     * Holds messages coming in through the network.
     */
    private final ConcurrentLinkedQueue<ByteBuffer> incomingMessageQueue
            = new ConcurrentLinkedQueue<>();

    /**
     * Holds messages that are to be sent on the network.
     */
    private final ArrayBlockingQueue<Buffer> outgoingMessageQueue;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean terminating = new AtomicBoolean(false);

    public GrizzlyNetworkClient(final int maxOutgoingMessages, final ThreadFactory threadFactory) {
        Preconditions.checkArgument(
                maxOutgoingMessages > 0,
                "Max outgoing messages must be a positive integer"
        );
        Preconditions.checkArgument(
                maxOutgoingMessages < 1000,
                "Max outgoing messages must be less than 1000"
        );

        this.outgoingMessageQueue
                = new ArrayBlockingQueue<>(maxOutgoingMessages, false);

        sendThread = threadFactory.newThread(() -> {
            while (!this.shutdown.get()) {
                try {
                    Buffer messageToSend = this.outgoingMessageQueue
                            .poll(30, TimeUnit.MILLISECONDS);
                    while (messageToSend != null) {
                        this.connection
                                .write(messageToSend)
                                .addCompletionHandler(
                                        new AbstractStreamWriter.DisposeBufferCompletionHandler(messageToSend)
                                );
                        messageToSend = this.outgoingMessageQueue.poll();
                    }
                } catch (InterruptedException ex) {
                    log.error("Thread waiting for outgoing messages was interrupted, shutting down network service", ex);
                    this.shutdown.set(true);
                    // Preserve interrupt flag
                    Thread.currentThread().interrupt();
                }
            }
            sendThreadRunning.countDown();
            terminateService();
        });
    }

    @Override
    protected void doStart() {
        log.debug("Starting network client");

        FilterChainBuilder clientFilterChainBuilder = FilterChainBuilder.stateless();
        clientFilterChainBuilder.add(new TransportFilter());
        clientFilterChainBuilder.add(new ClientFilter());
        transport = TCPNIOTransportBuilder.newInstance().setTcpNoDelay(true).build();
        transport.setProcessor(clientFilterChainBuilder.build());
        try {
            transport.start();
            sendThread.start();
            notifyStarted();
        } catch (IOException ex) {
            log.error("unable to start TCP client", ex);
            this.shutdown.set(true);
            notifyFailed(ex);
        }
    }

    @Override
    protected void doStop() {
        log.debug("Stopping network client");
        this.shutdown.set(true);
        terminateService();
        notifyStopped();
    }

    private void terminateService() {
        if (!this.terminating.get()) {
            this.terminating.set(true);

            try {
                log.debug("Waiting for send thread to terminate");
                this.sendThreadRunning.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                log.error("Send thread did not terminate on time.", ex);
            }

            log.debug("Shutting down grizzly transport");
            this.transport.shutdown(2, TimeUnit.SECONDS);
            log.info("Network transport terminated");
        }
    }

    @Override
    public ByteBuffer getNextMessage() {
        return this.incomingMessageQueue.poll();
    }

    @Override
    public void sendMessage(ByteBuffer data)
            throws OutgoingQueueFullException, NotConnectedException {
        if (!this.isConnected()) {
            throw new NotConnectedException("Client is not connected");
        }
        final Buffer grizzlyBuffer
                = HeapMemoryManager.DEFAULT_MEMORY_MANAGER.allocate(data.remaining() + 2 * Integer.BYTES);
        grizzlyBuffer.putInt(MessageUtilities.getMagicMarker());
        grizzlyBuffer.put(data);
        grizzlyBuffer.flip();
        if (!this.outgoingMessageQueue.offer(grizzlyBuffer)) {
            log.error("Unable to enqueue outgoing message, queue is full.");
            throw new OutgoingQueueFullException("Queue is full");
        }
    }

    @Override
    public void connect(final String host, final int port) {
        log.debug("Connecting to {}:{}", host, port);
        this.transport.connect(host, port);
    }

    @Override
    public boolean isConnected() {
        return this.connected.get();
    }

    class ClientFilter extends BaseFilter {

        @Override
        public NextAction handleClose(final FilterChainContext ctx)
                throws IOException {
            log.debug("Connection close");
            connection = null;
            connected.set(false);
            return ctx.getInvokeAction();
        }

        @Override
        public NextAction handleConnect(final FilterChainContext ctx)
                throws IOException {
            log.debug("Connection established");
            connection = ctx.getConnection();
            connected.set(true);
            return ctx.getInvokeAction();
        }

        @Override
        public NextAction handleRead(final FilterChainContext ctx)
                throws IOException {
            final Buffer buffer = ctx.getMessage();
            if (buffer.remaining() > 4) {
                final int magicMarker = buffer.getInt();
                if (MessageUtilities.isMagicMarker(magicMarker)) {
                    final int remaining = buffer.remaining();
                    final ByteBuffer messageData = ByteBuffer.allocate(remaining);
                    buffer.get(messageData);
                    messageData.flip();
                    incomingMessageQueue.offer(messageData);
                } else {
                    // Stop processing malformed message
                    log.info("Message is not prefixed with magic marker");
                    return ctx.getStopAction();
                }
            } else {
                // Stop processing malformed message
                log.info("Buffer remaining to small to contain the magic marker and message type");

                return ctx.getStopAction();
            }

            return ctx.getInvokeAction();
        }
    }

}
