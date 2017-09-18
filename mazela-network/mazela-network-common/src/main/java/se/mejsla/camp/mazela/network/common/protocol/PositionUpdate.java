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
package se.mejsla.camp.mazela.network.common.protocol;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class PositionUpdate {

    public static final int BYTES = Long.BYTES * 2 + Float.BYTES * 2;

    private final UUID entityID;
    private final float x;
    private final float y;

    public PositionUpdate(UUID entityID, float x, float y) {
        this.entityID = Preconditions.checkNotNull(entityID);
        this.x = x;
        this.y = y;
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

    void encode(final ByteBuffer buffer) {
        Preconditions.checkNotNull(buffer);

        buffer.putLong(this.entityID.getMostSignificantBits());
        buffer.putLong(this.entityID.getLeastSignificantBits());
        buffer.putFloat(x);
        buffer.putFloat(y);
    }

    static PositionUpdate decode(final ByteBuffer buffer) {
        Preconditions.checkNotNull(buffer);

        final long mostSignificant = buffer.getLong();
        final long leastSignificant = buffer.getLong();
        UUID id = new UUID(mostSignificant, leastSignificant);
        final float x = buffer.getFloat();
        final float y = buffer.getFloat();
        return new PositionUpdate(id, x, y);
    }

}
