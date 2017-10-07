package se.mejsla.camp.mazela.game.domain;

import org.dyn4j.dynamics.Body;

import java.util.UUID;

public interface GameModel {

    Body getPhysicsBody();

    UUID getUuid();

}