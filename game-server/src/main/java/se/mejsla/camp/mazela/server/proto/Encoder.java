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
package se.mejsla.camp.mazela.server.proto;

import se.mejsla.camp.mazela.game.EntityUpdate;
import se.mejsla.camp.mazela.game.domain.Player;
import se.mejsla.camp.mazela.game.domain.Score;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Johan Maasing <johan@zoom.nu>
 */
public abstract class Encoder {

    public static ByteBuffer encodeGameState(final List<EntityUpdate> updates,
                                             Map<UUID, MazelaProtocol.Color> colorForPlayer) {
        final MazelaProtocol.GameboardUpdate.Builder gameboardBuilder = MazelaProtocol.GameboardUpdate.newBuilder();
        for (EntityUpdate update : updates) {
            UUID entityID = update.getEntityID();

            MazelaProtocol.Coordinate coordinate = MazelaProtocol.Coordinate
                    .newBuilder()
                    .setX(update.getX())
                    .setY(update.getY())
                    .build();

            MazelaProtocol.Uuid uuid = MazelaProtocol.Uuid
                    .newBuilder()
                    .setLeastSignificantID(update.getEntityID().getLeastSignificantBits())
                    .setMostSignificantID(update.getEntityID().getMostSignificantBits())
                    .build();

            if (update.getModel() instanceof Player) {
                Player player = (Player) update.getModel();

                MazelaProtocol.GameboardUpdate.EntityUpdate eu
                        = MazelaProtocol.GameboardUpdate.EntityUpdate
                        .newBuilder()
                        .setEntityType(MazelaProtocol.GameboardUpdate.EntityUpdate.EntityType.PlayerEntity)
                        .setPlayerEntity(MazelaProtocol.PlayerEntity.newBuilder()
                                .setColor(colorForPlayer.get(entityID))
                                .setCoords(coordinate)
                                .setScore(player.getScore().intValue())
                                .setName(player.getName())
                                .setUuid(uuid)
                                .build())
                        .build();
                gameboardBuilder.addUpdates(eu);
            } else if (update.getModel() instanceof Score) {
                Score score = (Score) update.getModel();
                MazelaProtocol.GameboardUpdate.EntityUpdate eu
                        = MazelaProtocol.GameboardUpdate.EntityUpdate
                        .newBuilder()
                        .setEntityType(MazelaProtocol.GameboardUpdate.EntityUpdate.EntityType.ScoreEntity)
                        .setScoreEntity(MazelaProtocol.ScoreEntity.newBuilder()
                                .setScore(score.getSore())
                                .setCoords(coordinate)
                                .build())
                        .build();
                gameboardBuilder.addUpdates(eu);
            }

        }
        final byte[] bytes = MazelaProtocol.Envelope
                .newBuilder()
                .setGameboardUpdate(gameboardBuilder.build())
                .setMessageType(MazelaProtocol.Envelope.MessageType.GameboardUpdate)
                .build()
                .toByteArray();
        return ByteBuffer.wrap(bytes);
    }
}
