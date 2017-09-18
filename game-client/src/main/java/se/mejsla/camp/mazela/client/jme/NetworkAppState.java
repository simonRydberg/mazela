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
package se.mejsla.camp.mazela.client.jme;

import com.google.common.base.Preconditions;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import java.nio.ByteBuffer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.client.NetworkClient;
import se.mejsla.camp.mazela.network.common.MessageUtilities;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;
import se.mejsla.camp.mazela.network.common.protocol.AuthenticationReply;
import se.mejsla.camp.mazela.network.common.protocol.MessageCodec;
import se.mejsla.camp.mazela.network.common.protocol.MessageType;
import se.mejsla.camp.mazela.network.common.protocol.PositionUpdate;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class NetworkAppState extends AbstractAppState {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NetworkClient networkClient;
    private boolean authenticated = false;
    private boolean awaitingAuthentication = false;
    private final GameboardAppstate gameboardAppstate;

    public NetworkAppState(
            final NetworkClient networkClient,
            final GameboardAppstate gameboardAppstate) {
        this.networkClient = Preconditions.checkNotNull(networkClient);
        this.gameboardAppstate = Preconditions.checkNotNull(gameboardAppstate);
    }

    @Override
    public void update(float tpf) {
        handleNetwork();
    }

    private void handleNetwork() {
        if (!networkClient.isConnected()) {
            // Pretend that we got this from the user
            log.debug("Connecting to the server");
            this.networkClient.connect("127.0.0.1", 1666);
        } else {
            if (!authenticated) {
                if (!awaitingAuthentication) {
                    try {
                        log.debug("Authenticating user");
                        // pretend that we got the authentication info from the user
                        this.networkClient.sendMessage(
                                MessageCodec.encodeAuthenticationRequest(
                                        MessageUtilities.AUTH_TOKEN
                                )
                        );
                        this.awaitingAuthentication = true;
                    } catch (OutgoingQueueFullException | NotConnectedException ex) {
                        log.error("Unable to send authentication request", ex);
                    }
                }
            }

            ByteBuffer incomingMessage = this.networkClient.getNextMessage();
            while (incomingMessage != null) {
                // parse the message
                parsemessage(incomingMessage);
                incomingMessage = this.networkClient.getNextMessage();
            }
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
    }

    private void parsemessage(final ByteBuffer incomingMessage) {
        final MessageType type = MessageType.decode(incomingMessage.getInt());
        switch (type) {
            case AUTH_REPLY: {
                AuthenticationReply authReply
                        = MessageCodec.decodeAuthenticationReply(incomingMessage);
                this.awaitingAuthentication = false;
                this.authenticated = authReply.isAuthenticated();
                if (this.authenticated) {
                    log.debug("Authentication success: {}", authReply.getConnectionID());
                    // Pretend we have some lobby and that the user chooses to join a game.
                    try {
                        log.debug("Trying to join the game");
                        this.networkClient.sendMessage(MessageCodec.encodeJoinGameRequest());
                    } catch (OutgoingQueueFullException | NotConnectedException ex) {
                        log.error("Unable to send request to join game", ex);
                    }
                } else {
                    log.info("Failed authentication");
                }
                break;
            }

            case GAMEBOARD_UPDATE: {
                final List<PositionUpdate> updates
                        = MessageCodec.decodePositionUpdates(incomingMessage);
                this.gameboardAppstate.setPendingUpdates(updates);
                break;
            }

            default: {
                log.info("Server sent a message of type: {} that was ignored", type);
            }
        }

    }

}
