package se.mejsla.camp.mazela.game.domain;

import org.dyn4j.dynamics.Body;

public class Score implements GameModel{
    private final Body physicsBody;
    private final int sore;

    public Score(Body physicsBody, int sore) {
        this.physicsBody = physicsBody;
        this.sore = sore;
    }

    public Body getPhysicsBody() {
        return physicsBody;
    }

    public int getSore() {
        return sore;
    }
}
