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
package se.mejsla.camp.mazela.game.physics;

import org.dyn4j.collision.AxisAlignedBounds;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;
import org.dyn4j.dynamics.World;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class PhysicsSpace {

    private final World world;

    public PhysicsSpace(final double worldBoundsWidth, final double worldBoundsHeight) {
        final AxisAlignedBounds worldBounds
                = new AxisAlignedBounds(worldBoundsWidth, worldBoundsHeight);
        this.world = new World(worldBounds);
    }

    public void tick(final float tpf) {
        this.world.update(tpf);
    }

    public World getWorld() {
        return this.world;
    }
}
