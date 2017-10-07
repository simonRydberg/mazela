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
import nu.zoom.corridors.math.XORShiftRandom;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.game.domain.GameModel;
import se.mejsla.camp.mazela.game.domain.Player;
import se.mejsla.camp.mazela.game.domain.Score;
import se.mejsla.camp.mazela.game.physics.PhysicsSpace;
import se.mejsla.camp.mazela.network.common.ConnectionID;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Johan Maasing <johan@zoom.nu>
 */
public class GameBoard {

    private static final double PLAYER_INITAL_AREA_WIDTH = 18;
    private static final double PLAYER_INITAL_AREA_HEIGHT = 18;
    private static final double WORLD_BOUNDS_WIDTH = 20;
    private static final double WORLD_BOUNDS_HEIGHT = 20;
    private static final double BOUNCYNESS = 0.0001;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final XORShiftRandom fastRandom = new XORShiftRandom(System.currentTimeMillis());
    private final PhysicsSpace physicsSpace = new PhysicsSpace(WORLD_BOUNDS_WIDTH, WORLD_BOUNDS_HEIGHT);

    private final ConcurrentHashMap<ConnectionID, Player> players = new ConcurrentHashMap<>();
    private final List<GameModel> gameModels = new ArrayList<>();

    private final CopyOnWriteArrayList<AddingPlayer> pendingPlayerAdds;
    private final CopyOnWriteArrayList<ConnectionID> pendingPlayerDeletes;

    private class AddingPlayer{
        public ConnectionID connectionID;
        public String name;
    }

    public GameBoard() {
        this.pendingPlayerDeletes = new CopyOnWriteArrayList<>();
        this.pendingPlayerAdds = new CopyOnWriteArrayList<>();
        physicsSpace.getWorld().setGravity(new Vector2(0, 0));
        physicsSpace.getWorld().addListener(new ScoreContactListener(this));
        setupEdges();
    }

    private void setupEdges() {
        final Rectangle horizRectangle = new Rectangle(PLAYER_INITAL_AREA_WIDTH + 2, 1.0);
        final Rectangle verticalRectangle = new Rectangle(1, PLAYER_INITAL_AREA_WIDTH + 2);

        // test
        final Body top = new Body();
        final BodyFixture topFixture = new BodyFixture(horizRectangle);
        topFixture.setRestitution(BOUNCYNESS);
        top.addFixture(topFixture);
        top.setMass(MassType.INFINITE);
        top.translate(0, (PLAYER_INITAL_AREA_HEIGHT / 2) - 0.5);
        this.physicsSpace.getWorld().addBody(top);

        final Body bottom = new Body();
        final BodyFixture bottomFixture = new BodyFixture(horizRectangle);
        bottomFixture.setRestitution(BOUNCYNESS);
        bottom.addFixture(bottomFixture);
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -(PLAYER_INITAL_AREA_HEIGHT / 2) + 0.5);
        this.physicsSpace.getWorld().addBody(bottom);

        final Body left = new Body();
        final BodyFixture leftFixture = new BodyFixture(verticalRectangle);
        leftFixture.setRestitution(BOUNCYNESS);
        left.addFixture(leftFixture);
        left.setMass(MassType.INFINITE);
        left.translate(-(PLAYER_INITAL_AREA_HEIGHT / 2) + 0.5, 0);
        this.physicsSpace.getWorld().addBody(left);

        final Body right = new Body();
        final BodyFixture rightFixture = new BodyFixture(verticalRectangle);
        rightFixture.setRestitution(BOUNCYNESS);
        right.addFixture(rightFixture);
        right.setMass(MassType.INFINITE);
        right.translate((PLAYER_INITAL_AREA_HEIGHT / 2) - 0.5, 0);
        this.physicsSpace.getWorld().addBody(right);

        // Adding initial scores
        addScoreRandomPos(1, createColor(255, 153, 153), 2, 2);
        addScoreRandomPos(3, createColor(255, 255, 153), 4, 4);
        addScoreRandomPos(5, createColor(153, 255, 153), 6, 6);
        addScoreRandomPos(7, createColor(153, 255, 255), 8, 8);
    }

    private MazelaProtocol.Color createColor(int r, int g, int b) {
        return MazelaProtocol.Color.newBuilder()
                .setRed(r)
                .setGreen(g)
                .setBlue(b)
                .build();
    }

    public void addPlayer(ConnectionID connectionID, String nickname) {
        if (connectionID != null) {
            AddingPlayer addingPlayer = new AddingPlayer();
            addingPlayer.connectionID = connectionID;
            addingPlayer.name = nickname;
            this.pendingPlayerAdds.add(addingPlayer);
        }

    }

    public void dropPlayer(final ConnectionID connectionID) {
        log.debug("Removing player: {} from the board", connectionID);
        if (connectionID != null) {
            this.pendingPlayerDeletes.add(connectionID);
        }
    }

    private void addPendingPlayers() {
        for (AddingPlayer addingPlayer : this.pendingPlayerAdds) {
            this.players.computeIfAbsent(Preconditions.checkNotNull(addingPlayer.connectionID), c -> {
                log.debug("Adding player {} to the board", addingPlayer.connectionID);
                final Body body = new Body();
                final BodyFixture bodyFixture = new BodyFixture(new Circle(1.0));
                bodyFixture.setRestitution(BOUNCYNESS);
                body.addFixture(bodyFixture);
                body.setMass(MassType.NORMAL);
                double initialX = this.fastRandom.unitRandom() * PLAYER_INITAL_AREA_WIDTH - (PLAYER_INITAL_AREA_WIDTH / 2);
                double initialY = (PLAYER_INITAL_AREA_HEIGHT / 2) - 1.0;
                body.getTransform().translate(initialX, initialY);
                this.physicsSpace.getWorld().addBody(body);
                Player player = new Player(addingPlayer.connectionID.getUuid(), body);
                player.setName(addingPlayer.name);
                body.setUserData(player);
                return player;
            });
        }
        this.pendingPlayerAdds.clear();
    }

    private void addScoreRandomPos(int score, MazelaProtocol.Color color, double initialX, double initialY) {
        final Body body = new Body();
        final BodyFixture bodyFixture = new BodyFixture(new Circle(0.5f));
        bodyFixture.setRestitution(BOUNCYNESS);
        body.addFixture(bodyFixture);
        body.setMass(MassType.NORMAL);
//        double initialX = this.fastRandom.unitRandom() * PLAYER_INITAL_AREA_WIDTH;
//        double initialY = this.fastRandom.unitRandom() * PLAYER_INITAL_AREA_HEIGHT;
        body.getTransform().translate(initialX, initialY);
        this.physicsSpace.getWorld().addBody(body);
        Score scoreModel = new Score(body, score, color);
        body.setUserData(scoreModel);
        this.gameModels.add(scoreModel);
        log.debug("Adding score {} at '{}-{}'to the board", score, initialX, initialY);
    }

    private void removeAndLayoutNew(Score score) {
        double playerInitalAreaWidth = PLAYER_INITAL_AREA_WIDTH - 2;
        double initialX = this.fastRandom.unitRandom() * playerInitalAreaWidth - (playerInitalAreaWidth / 2);
        double playerInitalAreaHeight = PLAYER_INITAL_AREA_HEIGHT - 2;
        double initialY = this.fastRandom.unitRandom() * playerInitalAreaHeight - (playerInitalAreaHeight / 2);
        gameModels.remove(score);
        addScoreRandomPos(score.getScore(), score.getColor(), initialX, initialY);
    }

    private void removePendingPlayers() {
        for (ConnectionID connectionID : this.pendingPlayerDeletes) {
            final Player remove = this.players.remove(Preconditions.checkNotNull(connectionID));
            Body physicsBody = remove.getPhysicsBody();
            if (physicsBody != null) {
                this.physicsSpace.getWorld().removeBody(physicsBody);
            }
        }
        this.pendingPlayerDeletes.clear();
    }

    public void tick(final float tpf) {
        addPendingPlayers();
        removePendingPlayers();
        // Update physics engine
        this.physicsSpace.tick(tpf);

        for (Player p : players.values()) {
            p.update(tpf);
        }
    }

    public List<EntityUpdate> snapshotGamestate() {
        final ArrayList<EntityUpdate> result = new ArrayList<>();
        players.forEach((id, player) -> {
            Vector2 position = player.getPhysicsBody().getWorldCenter();
            result.add(
                    new EntityUpdate(
                            id.getUuid(),
                            (float) position.x,
                            (float) position.y,
                            player)
            );
        });
        gameModels.forEach(model -> {
            Vector2 position = model.getPhysicsBody().getWorldCenter();
            result.add(
                    new EntityUpdate(
                            model.getUuid(),
                            (float) position.x,
                            (float) position.y,
                            model)
            );
        });
        return result;
    }

    public Set<ConnectionID> getPlayers() {
        return this.players
                .entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void playerInput(
            final ConnectionID connectionID,
            final MazelaProtocol.ClientInput clientInput) {
        if (connectionID != null && clientInput != null) {
            final Player player = this.players.get(connectionID);
            if (player != null) {
                player.setInput(
                        clientInput.getLeft(),
                        clientInput.getRight(),
                        clientInput.getUp(),
                        clientInput.getDown()
                );
            }
        }
    }

    public static class ScoreContactListener extends CollisionAdapter {
        GameBoard gameBoard;

        public ScoreContactListener(GameBoard gameBoard) {
            this.gameBoard = gameBoard;
        }

        @Override
        public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {
            Player player = null;
            Body playerBody = null;
            Score score = null;
            Body scoreBody = null;
            if (body1.getUserData() instanceof Player && body2.getUserData() instanceof Score) {
                player = (Player) body1.getUserData();
                score = (Score) body2.getUserData();
                playerBody = body1;
                scoreBody = body2;
            } else if (body2.getUserData() instanceof Player && body1.getUserData() instanceof Score) {
                player = (Player) body2.getUserData();
                score = (Score) body1.getUserData();
                playerBody = body2;
                scoreBody = body1;
            } else {
                return true;
            }
            if (score == null) {
                return true;
            }

            player.addScore(score.getScore());
            gameBoard.physicsSpace.getWorld().removeBody(scoreBody);
            gameBoard.removeAndLayoutNew(score);
            return true;
        }
    }


}
