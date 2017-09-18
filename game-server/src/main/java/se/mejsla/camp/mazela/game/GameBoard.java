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
package se.mejsla.camp.mazela.game;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import nu.zoom.corridors.math.Vector2f;
import nu.zoom.corridors.math.XORShiftRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.game.physics.PhysicsSpace;
import se.mejsla.camp.mazela.network.common.ConnectionID;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class GameBoard {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final XORShiftRandom fastRandom = new XORShiftRandom(System.currentTimeMillis());
    private final PhysicsSpace physicsSpace = new PhysicsSpace();

    private final ConcurrentHashMap<ConnectionID, Player> players
            = new ConcurrentHashMap<>();

    public void addPlayer(ConnectionID connectionID) {
        log.debug("Adding a new player to the board");
        this.players.putIfAbsent(
                Preconditions.checkNotNull(connectionID),
                new Player((float) fastRandom.unitRandom(), (float) fastRandom.unitRandom())
        );
    }

    public void dropPlayer(final ConnectionID connectionID) {
        log.debug("Removing player: {} from the board", connectionID);
        this.players.remove(Preconditions.checkNotNull(connectionID));
    }

    public void tick(final float tpf) {
        for (Player p : players.values()) {
            p.update(tpf);
        }
        // Update physics engine
        this.physicsSpace.tick(tpf);
    }

    public List<EntityUpdate> snapshotGamestate() {
        final ArrayList<EntityUpdate> result = new ArrayList<>();
        players.forEach((id, player) -> {
            final Vector2f positition = player.getPositition();
            result.add(
                    new EntityUpdate(
                            id.getUuid(),
                            positition.x,
                            positition.y)
            );
        });
        return result;
    }

    public Set<ConnectionID> getPlayers() {
        return this.players
                .entrySet()
                .stream()
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }
}
