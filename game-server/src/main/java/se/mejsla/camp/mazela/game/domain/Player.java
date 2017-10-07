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
package se.mejsla.camp.mazela.game.domain;

import com.google.common.base.Preconditions;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Johan Maasing <johan@zoom.nu>
 */
public class Player implements GameModel {

    private final Body physicsBody;
    private final AtomicBoolean up = new AtomicBoolean(false);
    private final AtomicBoolean down = new AtomicBoolean(false);
    private final AtomicBoolean left = new AtomicBoolean(false);
    private final AtomicBoolean right = new AtomicBoolean(false);
    private final AtomicBoolean needsUpdate = new AtomicBoolean(false);
    private final AtomicInteger score = new AtomicInteger(0);
    private String name = "test";
    private final UUID uuid;

    public Player(UUID uuid, final Body physicsBody) {
        this.uuid = uuid;
        name = "Server: " + uuid.toString();
        this.physicsBody = Preconditions.checkNotNull(physicsBody);
    }

    public void update(final float tpf) {
        Vector2 force = new Vector2(0, 0);
        if (this.needsUpdate.get()) {
            this.needsUpdate.set(false);
        }
        if (this.right.get()) {
            force.add(tpf, 0);
        }
        if (this.left.get()) {
            force.add(-tpf, 0);
        }
        if (this.up.get()) {
            force.add(0, tpf);
        }
        if (this.down.get()) {
            force.add(0, -tpf);
        }
        force.multiply(4000);
        this.physicsBody.applyForce(force);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Body getPhysicsBody() {
        return physicsBody;
    }

    public void setInput(
            final boolean left,
            final boolean right,
            final boolean up,
            final boolean down) {
        this.up.set(up);
        this.down.set(down);
        this.left.set(left);
        this.right.set(right);
        this.needsUpdate.set(true);
    }

    public AtomicInteger getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score.addAndGet(score);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
