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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.common.ConnectionID;
import se.mejsla.camp.mazela.network.common.MessageUtilities;
import se.mejsla.camp.mazela.network.server.IncomingMessage;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class ServerFilter extends BaseFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final GrizzlyNetworkServer server;
    private static final Attribute<ConnectionID> CONNECTION_ATTRIBUTE
            = Grizzly.DEFAULT_ATTRIBUTE_BUILDER
                    .createAttribute(
                            MessageUtilities.Attributes.CONNECTION_ID
                    );

    public ServerFilter(GrizzlyNetworkServer server) {
        this.server = server;
    }

    @Override
    public NextAction handleAccept(FilterChainContext ctx) throws IOException {
        log.debug("Socket accept");
        assignUUIDToConnection(ctx);
        return super.handleAccept(ctx);
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        Buffer buffer = ctx.getMessage();
        int magicMarker = buffer.getInt();
        if (MessageUtilities.isMagicMarker(magicMarker)) {
            int remaining = buffer.remaining();
            final ByteBuffer messageData = ByteBuffer.allocate(remaining);
            buffer.get(messageData);
            messageData.flip();
            final ConnectionID connectionID = CONNECTION_ATTRIBUTE.get(ctx.getConnection());
            if (connectionID == null) {
                log.error("Connection read but connection is not assigned an ID");
                return ctx.getStopAction();
            } else {
                this.server.addIncomingMessage(
                        new IncomingMessage(connectionID, messageData)
                );
            }
        } else {
            // Stop processing malformed message
            log.info("Message is not prefixed with magic marker");

            return ctx.getStopAction();
        }
        return super.handleRead(ctx);
    }

    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
        final ConnectionID connectionID
                = CONNECTION_ATTRIBUTE.get(ctx.getConnection());
        if (connectionID != null) {
            server.dropConnection(connectionID);
        }
        return super.handleClose(ctx);
    }

    private void assignUUIDToConnection(FilterChainContext ctx) {
        final Connection connection = ctx.getConnection();

        // Check if the connection is already assigned an ID
        final ConnectionID existingID = CONNECTION_ATTRIBUTE.get(connection);
        if (existingID != null) {
            log.error("Connection already registered: {} - connection will be unregistered before assigning new ID.", existingID);

            // This opens for DOS if connectionID is known but for simplicity...
            this.server.dropConnection(existingID);
        }
        final UUID newConnId = UUID.randomUUID();
        ConnectionID connID = new ConnectionID(newConnId);
        CONNECTION_ATTRIBUTE.set(connection, connID);
        server.registerConnection(connID, connection);
    }
}
