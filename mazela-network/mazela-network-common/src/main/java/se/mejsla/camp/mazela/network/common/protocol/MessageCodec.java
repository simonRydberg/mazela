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
package se.mejsla.camp.mazela.network.common.protocol;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple encoder of messages to bytes.
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public abstract class MessageCodec {

    public static MessageType getMessageType(final ByteBuffer buffer) {
        int typeCode = Preconditions.checkNotNull(buffer).getInt();
        return MessageType.decode(typeCode);
    }

    public static ByteBuffer encodeAuthenticationRequest(final int authToken) {
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES * 2);
        payload.putInt(MessageType.AUTH_REQEUEST.encode());
        payload.putInt(authToken);
        payload.flip();
        return payload;
    }

    public static int decodeAuthenticationRequest(final ByteBuffer payload) {
        return payload.getInt();
    }

    public static ByteBuffer encodeJoinGameRequest() {
        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES * 1);
        payload.putInt(MessageType.JOIN_GAME_REQUEST.encode());
        payload.flip();
        return payload;
    }

    /**
     * Encode an authentication reply.
     *
     * @param connectionID The connection ID assigned to the client if
     * successfully authenticated. Null if not authenticated.
     * @return
     */
    public static ByteBuffer encodeAuthenticationReply(final UUID connectionID) {
        Preconditions.checkNotNull(connectionID);

        final ByteBuffer payload = ByteBuffer.allocate(Integer.BYTES * 2 + Long.BYTES * 2);
        payload.putInt(MessageType.AUTH_REPLY.encode());
        if (connectionID != null) {
            payload.putLong(connectionID.getMostSignificantBits());
            payload.putLong(connectionID.getLeastSignificantBits());
        } else {
            payload.putLong(0);
            payload.putLong(0);
        }
        payload.flip();
        return payload;
    }

    public static AuthenticationReply decodeAuthenticationReply(final ByteBuffer payload) {
        long mostSignifcant = payload.getLong();
        long leastSignifcant = payload.getLong();
        final UUID connectionID;
        final boolean authenticated;
        if (mostSignifcant == 0) {
            connectionID = null;
            authenticated = false;
        } else {
            connectionID = new UUID(mostSignifcant, leastSignifcant);
            authenticated = true;
        }
        return new AuthenticationReply(authenticated, connectionID);
    }

    public static ByteBuffer encodePositionUpdates(final List<PositionUpdate> updates) {
        final int numUpdates = updates.size();
        final ByteBuffer payload = ByteBuffer.allocate(
                Integer.BYTES * 2 + numUpdates * PositionUpdate.BYTES
        );
        payload.putInt(MessageType.GAMEBOARD_UPDATE.encode());
        payload.putInt(numUpdates);
        updates.forEach((g) -> g.encode(payload));
        payload.flip();
        return payload;
    }

    public static final List<PositionUpdate> decodePositionUpdates(final ByteBuffer payload) {
        final int numUpdates = Preconditions.checkNotNull(payload).getInt();
        final ArrayList<PositionUpdate> updates = new ArrayList<>(numUpdates);
        for (int n = 0; n < numUpdates; n++) {
            updates.add(PositionUpdate.decode(payload));
        }
        return updates;
    }

    public static ByteBuffer encodeKeyboardInput(final KeyboardInput input) {
        final ByteBuffer payload = ByteBuffer.allocate(
                Integer.BYTES + KeyboardInput.BYTES
        );
        payload.putInt(MessageType.KEYBOARD_INPUT.encode());
        input.encode(payload);
        payload.flip();
        return payload;
    }

    public static KeyboardInput decodeKeyboardInput(final ByteBuffer payload) {
        return KeyboardInput.decode(payload);
    }
}
