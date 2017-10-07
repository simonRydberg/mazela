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
package se.mejsla.camp.mazela.server;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.protobuf.InvalidProtocolBufferException;
import nu.zoom.corridors.math.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.game.EntityUpdate;
import se.mejsla.camp.mazela.game.GameBoard;
import se.mejsla.camp.mazela.network.common.ConnectionID;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol.Uuid;
import se.mejsla.camp.mazela.network.server.IncomingMessage;
import se.mejsla.camp.mazela.network.server.NetworkServer;
import se.mejsla.camp.mazela.server.proto.Encoder;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * @author Johan Maasing <johan@zoom.nu>
 */
public class ServerService extends AbstractScheduledService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final NetworkServer networkServer;
    private long lastFrameTime = System.nanoTime();
    private final ExecutorService backgroundService;
    private final GameBoard gameBoard;
    private final CopyOnWriteArrayList<ConnectionID> authenticatedConnections = new CopyOnWriteArrayList<>();

    private final Map<UUID, MazelaProtocol.Color> colorForPlayer = new HashMap<>();
    private final List<MazelaProtocol.Color> freeColors = new ArrayList<>(Arrays.asList(
            createColor(0, 0, 255),
            createColor(255, 0, 0),
            createColor(0, 255, 0),
            createColor(0, 0, 255),
            createColor(255, 0, 255),
            createColor(125, 125, 0)
    ));

    private MazelaProtocol.Color createColor(int r, int g, int b) {
        return MazelaProtocol.Color.newBuilder()
                .setRed(r)
                .setGreen(g)
                .setBlue(b)
                .build();
    }

    public ServerService(
            final NetworkServer networkServer,
            final ThreadFactory threadFactory) {
        this.networkServer = Preconditions.checkNotNull(networkServer);
        this.backgroundService = Executors.newCachedThreadPool(
                Preconditions.checkNotNull(threadFactory)
        );

        this.gameBoard = new GameBoard();
        this.networkServer.addConnectionDroppedListener((id) -> {
            if (id != null) {
                this.authenticatedConnections.remove(id);
                this.gameBoard.dropPlayer(id);
                freeColors.add(colorForPlayer.get(id.getUuid()));
                colorForPlayer.remove(id.getUuid());
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
        log.debug("Shutting down background executor");
        this.backgroundService.shutdownNow();
        super.shutDown();
    }

    @Override
    protected void runOneIteration() throws Exception {
        try {
            for (IncomingMessage incomingMessage = this.networkServer.getIncomingMessage();
                 incomingMessage != null;
                 incomingMessage = this.networkServer.getIncomingMessage()) {
                asyncParseMessage(incomingMessage);
            }


            final long now = System.nanoTime();
            final long frameTime = now - this.lastFrameTime;
            this.lastFrameTime = now;
            final float tpf = (float) MathUtil.nanosToSeconds(frameTime);

            this.gameBoard.tick(tpf);
            final List<EntityUpdate> gameState = this.gameBoard.snapshotGamestate();
            final ByteBuffer payload = Encoder.encodeGameState(gameState, colorForPlayer);
            try {
                for (ConnectionID cID : this.gameBoard.getPlayers()) {
                    this.networkServer.sendMessage(payload, cID);
                }
            } catch (OutgoingQueueFullException | NotConnectedException ex) {
                log.error("Unable to send game state update to all clients", ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Scheduler scheduler() {
        return AbstractScheduledService.Scheduler.newFixedRateSchedule(
                300, 30, TimeUnit.MILLISECONDS
        );
    }

    private void asyncParseMessage(final IncomingMessage incomingMessage) {
        this.backgroundService.submit(() -> {
            try {
                final ConnectionID connectionID = incomingMessage.getConnectionID();
                final ByteBuffer messageData = incomingMessage.getData();
                final MazelaProtocol.Envelope envelope
                        = MazelaProtocol.Envelope.parseFrom(messageData);
                switch (envelope.getMessageType()) {
                    case AuthenticateRequest: {
                        final MazelaProtocol.AuthenticateRequest req = envelope.getAuthenticationRequest();
                        // Pretend we are looking up the user record
                        final String username = req.getName();
                        final String password = req.getPassword();
                        final MazelaProtocol.AuthenticationReply.Builder replyBuilder
                                = MazelaProtocol.AuthenticationReply.newBuilder();
                        if (password != null && password.length() > 0 && username != null && username.length() > 0) {
                            final UUID result = connectionID.getUuid();
                            replyBuilder.setAuthenticated(true);
                            replyBuilder.setUuid(
                                    Uuid
                                            .newBuilder()
                                            .setLeastSignificantID(result.getLeastSignificantBits())
                                            .setMostSignificantID(result.getMostSignificantBits())
                                            .build()
                            );
                            MazelaProtocol.Color color = freeColors.remove(0);
                            colorForPlayer.put(connectionID.getUuid(), color);
                            log.debug("Authentication success for connection: {}, with color: {}", result, color);
                            this.authenticatedConnections.add(connectionID);

                        } else {
                            log.debug("Authentication failed for connection: {}", connectionID);
                            replyBuilder.setAuthenticated(false);

                        }
                        networkServer.sendMessage(
                                ByteBuffer.wrap(
                                        MazelaProtocol.Envelope
                                                .newBuilder()
                                                .setMessageType(MazelaProtocol.Envelope.MessageType.AuthenticationReply)
                                                .setAuthenticationReply(replyBuilder.build())
                                                .build()
                                                .toByteArray()
                                ),
                                connectionID);

                        break;
                    }
                    case JoinPlayer: {
                        if (this.authenticatedConnections.contains(connectionID)) {
                            this.gameBoard.addPlayer(connectionID);
                        } else {
                            log.debug("Can not join game before authentication: {}", connectionID);
                        }
                    }
                    case ClientInput: {
                        this.gameBoard.playerInput(
                                connectionID,
                                envelope.getClientInput()
                        );
                        break;
                    }
                }
            } catch (IllegalArgumentException | NotConnectedException | OutgoingQueueFullException e) {
                log.error("Unable to parse network message", e);
            } catch (InvalidProtocolBufferException ex) {
                java.util.logging.Logger.getLogger(ServerService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
