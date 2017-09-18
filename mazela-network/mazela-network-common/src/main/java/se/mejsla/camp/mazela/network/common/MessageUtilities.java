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
package se.mejsla.camp.mazela.network.common;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class MessageUtilities {

    private static final int MAGIC_MARKER = 0xbadbabe;
    public static final int AUTH_TOKEN = 0xcafedad;

    public static final int getMagicMarker() {
        return MAGIC_MARKER;
    }

    public static final boolean isMagicMarker(final int in) {
        return ((in & MAGIC_MARKER) == MAGIC_MARKER);
    }

    public interface Attributes {

        public String CONNECTION_ID = "ConnectionID";
    }
}
