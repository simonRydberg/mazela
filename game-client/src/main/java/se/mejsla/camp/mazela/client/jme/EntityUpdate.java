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
package se.mejsla.camp.mazela.client.jme;

import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

import java.util.UUID;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class EntityUpdate {

    private final UUID entityID;
    private final float x;
    private final float y;
    private final int state;
    private final MazelaProtocol.Color color;

    public EntityUpdate(UUID entityID, float x, float y, final int state, MazelaProtocol.Color color) {
        this.entityID = entityID;
        this.x = x;
        this.y = y;
        this.state = state;
        this.color = color;
    }

    public UUID getEntityID() {
        return entityID;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public MazelaProtocol.Color getColor() {
        return color;
    }
}
