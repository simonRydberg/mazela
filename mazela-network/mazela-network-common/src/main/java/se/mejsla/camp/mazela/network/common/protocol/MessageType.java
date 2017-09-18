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

/**
 * Indicates the type of network message.
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public enum MessageType {
    AUTH_REQEUEST(1), AUTH_REPLY(2), JOIN_GAME_REQUEST(3), GAMEBOARD_UPDATE(4), KEYBOARD_INPUT(5);

    private final int type;

    private MessageType(int type) {
        this.type = type;
    }

    public int encode() {
        return this.type;
    }

    public static MessageType decode(final int type) {
        for (MessageType t : MessageType.values()) {
            if (t.type == type) {
                return t;
            }
        }
        throw new IllegalArgumentException("Message type: " + type + " is unkown");
    }
}
