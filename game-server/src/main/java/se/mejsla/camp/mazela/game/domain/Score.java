package se.mejsla.camp.mazela.game.domain;

import org.dyn4j.dynamics.Body;

import java.util.UUID;

public class Score implements GameModel{
    private final UUID uuid;
    private final Body physicsBody;
    private final int sore;

    public Score(Body physicsBody, int sore) {
        this.uuid = UUID.randomUUID();
        this.physicsBody = physicsBody;
        this.sore = sore;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public Body getPhysicsBody() {
        return physicsBody;
    }

    public int getSore() {
        return sore;
    }
}
