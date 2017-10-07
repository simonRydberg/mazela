package se.mejsla.camp.mazela.game.domain;

import org.dyn4j.dynamics.Body;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

import java.util.UUID;

public class Score implements GameModel{
    private final UUID uuid;
    private final Body physicsBody;
    private final int score;
    private final MazelaProtocol.Color color;

    public Score(Body physicsBody, int score, MazelaProtocol.Color color) {
        this.color = color;
        this.uuid = UUID.randomUUID();
        this.physicsBody = physicsBody;
        this.score = score;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public Body getPhysicsBody() {
        return physicsBody;
    }

    public int getScore() {
        return score;
    }

    public MazelaProtocol.Color getColor() {
        return color;
    }
}
