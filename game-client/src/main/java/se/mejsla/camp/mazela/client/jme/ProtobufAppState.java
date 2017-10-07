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
import com.google.protobuf.InvalidProtocolBufferException;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.client.NetworkClient;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class ProtobufAppState extends AbstractAppState {

    private static final int SERVER_PORT = 1666;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final NetworkClient networkClient;
    private boolean authenticated = false;
    private boolean awaitingAuthentication = false;
    private final GameboardAppstate gameboardAppstate;
    private final String serverHostName;

    public ProtobufAppState(final String serverHostName, final NetworkClient networkClient, final GameboardAppstate gameboardAppstate) {
        this.serverHostName = serverHostName;
        this.networkClient = Preconditions.checkNotNull(networkClient);
        this.gameboardAppstate = Preconditions.checkNotNull(gameboardAppstate);
    }

    @Override
    public void update(float tpf) {
        try {
            handleNetwork();
        } catch (InvalidProtocolBufferException
                | OutgoingQueueFullException
                | NotConnectedException ex) {
            log.error("Network problems", ex);
        }
    }

    private void handleNetwork() throws InvalidProtocolBufferException, OutgoingQueueFullException, NotConnectedException {
        if (!networkClient.isConnected()) {
            // Pretend that we got this from the user
            log.debug("Connecting to server " + serverHostName + ":" + SERVER_PORT);
            this.networkClient.connect(serverHostName, SERVER_PORT);
        } else {
            if (!authenticated) {
                if (!awaitingAuthentication) {
                    try {
                        log.debug("Authenticating user");
                        // pretend that we got the authentication info from the user
                        final MazelaProtocol.AuthenticateRequest.Builder authReq
                                = MazelaProtocol.AuthenticateRequest.newBuilder()
                                        .setName("foo")
                                        .setPassword("bar");
                        final byte[] messageBytes = MazelaProtocol.Envelope.newBuilder()
                                .setMessageType(MazelaProtocol.Envelope.MessageType.AuthenticateRequest)
                                .setAuthenticationRequest(authReq).build()
                                .toByteArray();
                        this.networkClient.sendMessage(
                                ByteBuffer.wrap(messageBytes)
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
                parseProtoMessage(incomingMessage);
                incomingMessage = this.networkClient.getNextMessage();
            }
        }
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
    }

    private void parseProtoMessage(final ByteBuffer incomingMessage)
            throws InvalidProtocolBufferException, OutgoingQueueFullException, NotConnectedException {
        MazelaProtocol.Envelope envelope
                = MazelaProtocol.Envelope.parseFrom(incomingMessage);
        MazelaProtocol.Envelope.MessageType type = envelope.getMessageType();
        switch (type) {
            case AuthenticationReply: {
                MazelaProtocol.AuthenticationReply authReply = envelope.getAuthenticationReply();
                this.awaitingAuthentication = false;
                this.authenticated = authReply.getAuthenticated();
                if (this.authenticated) {
                    MazelaProtocol.Uuid protocolUuid = authReply.getUuid();
                    UUID uuid = new UUID(
                            protocolUuid.getMostSignificantID(),
                            protocolUuid.getLeastSignificantID()
                    );
                    gameboardAppstate.setPlayerUUID(uuid);
                    if (authReply.hasColor()) {
                        gameboardAppstate.setPlayerColor(authReply.getColor());
                    } else {
                        gameboardAppstate.setPlayerColor(MazelaProtocol.Color
                                .newBuilder()
                                .setRed(255)
                                .setGreen(255)
                                .setBlue(255)
                                .build());
                    }
                    log.debug("Authentication success: {}", uuid);
                    // Pretend we have some lobby and that the user chooses to join a game.
                    log.debug("Trying to join the game");
                    final byte[] message = MazelaProtocol.Envelope
                            .newBuilder()
                            .setMessageType(MazelaProtocol.Envelope.MessageType.JoinPlayer)
                            .setJoinPlayer(
                                    MazelaProtocol.JoinPlayer.newBuilder()
                                            .setNickname("Foo")
                                            .build()
                            )
                            .build()
                            .toByteArray();
                    this.networkClient.sendMessage(ByteBuffer.wrap(message));
                } else {
                    log.info("Failed authentication");
                }
                break;
            }

            case GameboardUpdate: {
                final MazelaProtocol.GameboardUpdate gameboardUpdate = envelope.getGameboardUpdate();
                List<MazelaProtocol.GameboardUpdate.EntityUpdate> updatesList = gameboardUpdate.getUpdatesList();
                if (updatesList != null) {
                    final ArrayList<EntityUpdate> pendingUpdates = new ArrayList<>();
                    for (MazelaProtocol.GameboardUpdate.EntityUpdate update : updatesList) {
                        EntityUpdate pendingUpdate;
                        switch (update.getEntityType()) {
                            case PlayerEntity:
                                MazelaProtocol.PlayerEntity playerEntity = update.getPlayerEntity();
                                pendingUpdate = new PlayerEntityUpdate(
                                        new UUID(
                                                playerEntity.getUuid().getMostSignificantID(),
                                                playerEntity.getUuid().getLeastSignificantID()
                                        ),
                                        playerEntity.getCoords().getX(),
                                        playerEntity.getCoords().getY(),
                                        playerEntity.getScore(),
                                        playerEntity.getName(),
                                        playerEntity.getColor()
                                );
                                break;
                            case ScoreEntity:
                                MazelaProtocol.ScoreEntity scoreEntity = update.getScoreEntity();
                                pendingUpdate = new ScoreEntityUpdate(
                                        new UUID(
                                                scoreEntity.getUuid().getMostSignificantID(),
                                                scoreEntity.getUuid().getLeastSignificantID()
                                        ),
                                        scoreEntity.getCoords().getX(),
                                        scoreEntity.getCoords().getY(),
                                        scoreEntity.getScore(),
                                        scoreEntity.getColor()
                                );
                                break;
                            default:
                                throw new IllegalStateException("invalid entity type: " + update.getEntityType());
                        }
                        pendingUpdates.add(pendingUpdate);
                    }
                    this.gameboardAppstate.setPendingUpdates(pendingUpdates);
                }
                break;
            }

            default: {
                log.info("Server sent a message of type: {} that was ignored", type);
            }
        }

    }
}
