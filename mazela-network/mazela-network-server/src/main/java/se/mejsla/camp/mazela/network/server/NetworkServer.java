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
package se.mejsla.camp.mazela.network.server;

import com.google.common.util.concurrent.Service;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import se.mejsla.camp.mazela.network.common.ConnectionID;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;

/**
 * Can send messages to other nodes on the network.
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public interface NetworkServer extends Service {

    /**
     * Send a message to the given recipients.
     *
     * @param data The data to send over the network. May not be null. The
     * buffer must be positioned correctly and have the limit set. After this
     * call the buffer might have its position changed.
     * @param recipient The recipients to send to. May not be null or empty.
     * @throws OutgoingQueueFullException If no more messages can be queued for
     * sending.
     * @throws NotConnectedException If the given connection ID is unknown or
     * not connected.
     */
    void sendMessage(ByteBuffer data, ConnectionID recipient) throws OutgoingQueueFullException, NotConnectedException;

    /**
     * Register a listener for dropped connections. A connection is dropped when
     * it is lost from the network or if a client re-authenticates using the
     * same connection. The callback might happen on any thread.
     *
     * @param l The listener for the callback. May not be null.
     */
    void addConnectionDroppedListener(Consumer<ConnectionID> l);

    /**
     * Get the next incoming message.
     *
     * @return The next message in the in-queue or null if no messages are
     * queued.
     */
    IncomingMessage getIncomingMessage();

    /**
     * Get dropped connections.
     *
     * @return The next message in the in-queue or null if no messages are
     * queued.
     */
    ConnectionID getDisconnected();
}
