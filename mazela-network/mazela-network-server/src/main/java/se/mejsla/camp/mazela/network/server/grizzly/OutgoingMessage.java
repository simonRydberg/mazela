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

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.streams.AbstractStreamWriter;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class OutgoingMessage {

    private final Connection connection;
    private final Buffer data;

    public OutgoingMessage(Connection connection, Buffer data) {
        this.connection = connection;
        this.data = data;
    }

    void send() {
        this.connection
                .write(this.data)
                .addCompletionHandler(
                        new AbstractStreamWriter.DisposeBufferCompletionHandler(data)
                );
    }
}
