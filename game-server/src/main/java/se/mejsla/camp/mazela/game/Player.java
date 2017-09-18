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

import nu.zoom.corridors.math.MathUtil;
import nu.zoom.corridors.math.Vector2f;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class Player {

    private Vector2f positition = new Vector2f((float) Math.random(), (float) Math.random());
    private Vector2f velocity;

    public Player(final float initialX, final float initialY) {
        this.velocity = new Vector2f(initialX, initialY);
        this.velocity.normalize(this.velocity);
    }

    public void update(final float tpf) {
        this.positition.add(this.velocity.multiply(tpf, null), this.positition);

        // Some sort of physically impossible bouncing :-)
        if (this.positition.x > 1.0 || this.positition.x < 0.0) {
            this.velocity.x = -this.velocity.x;
            this.positition.x = MathUtil.clampToUnit(this.positition.x);
        }
        if (this.positition.y > 1.0 || this.positition.y < 0.0) {
            this.velocity.y = -this.velocity.y;
            this.positition.y = MathUtil.clampToUnit(this.positition.y);
        }
    }

    public Vector2f getPositition() {
        return positition;
    }

}
