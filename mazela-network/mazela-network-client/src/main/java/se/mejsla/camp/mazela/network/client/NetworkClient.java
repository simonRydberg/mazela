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
package se.mejsla.camp.mazela.network.client;

import com.google.common.util.concurrent.Service;
import java.nio.ByteBuffer;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;

/**
 * Skeleton network client interface. Exposes the network state and methods to
 * send messages.
 * <p>
 * The state is crude and should allow for reconnects and authentication
 * failures.
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public interface NetworkClient extends Service {

    /**
     * Async connect to the server.
     *
     * @param host
     * @param port
     */
    void connect(String host, int port);

    /**
     * Check if the client is connected to the server.
     *
     * @return true if connected.
     */
    boolean isConnected();

    /**
     * Async send a message to the server.
     *
     * @param data
     * @throws OutgoingQueueFullException
     * @throws NotConnectedException
     */
    void sendMessage(ByteBuffer data) throws OutgoingQueueFullException, NotConnectedException;

    /**
     * Get the next incoming message from the in-queue. The buffer is no longer
     * tracked by the client after this call and is considered 'owned' by the
     * consumer.
     *
     * @return The next incoming message or null if no messages are queued.
     */
    public ByteBuffer getNextMessage();

}
