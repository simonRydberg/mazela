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
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class Player {

    private final XORShiftRandom random = new XORShiftRandom(System.currentTimeMillis());
    private final Body physicsBody;

    public Player(final Body physicsBody) {
        this.physicsBody = Preconditions.checkNotNull(physicsBody);
    }

    public void update(final float tpf) {
        //this.physicsBody.applyForce(new Vector2((random.unitRandom() / 2) * 200, random.unitRandom() * 200));
    }

    public Body getPhysicsBody() {
        return physicsBody;
    }
}
