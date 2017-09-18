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

import java.nio.ByteBuffer;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class KeyboardInput {

    public static final int BYTES = Integer.BYTES;
    public static final int POSX = 1;
    public static final int NEGX = 2;
    public static final int POSY = 4;
    public static final int NEGY = 8;
    public static final int FIRE1 = 16;
    public static final int FIRE2 = 32;

    private final int input;

    public KeyboardInput(
            final boolean posx,
            final boolean negx,
            final boolean posy,
            final boolean negy,
            final boolean fire1,
            final boolean fire2) {

        int bits = 0;
        if (posx) {
            bits |= POSX;
        }
        if (negx) {
            bits |= NEGX;
        }
        if (posy) {
            bits |= POSY;
        }
        if (negy) {
            bits |= NEGY;
        }
        if (fire1) {
            bits |= FIRE1;
        }
        if (fire2) {
            bits |= FIRE2;
        }
        this.input = bits;
    }

    public KeyboardInput(final int bits) {
        this.input = bits;
    }

    public boolean isPosX() {
        return (this.input & POSX) == POSX;
    }

    public boolean isNegX() {
        return (this.input & NEGX) == NEGX;
    }

    public boolean isPosY() {
        return (this.input & POSY) == POSY;
    }

    public boolean isNegY() {
        return (this.input & NEGY) == NEGY;
    }

    public boolean isFire1() {
        return (this.input & FIRE1) == FIRE1;
    }

    public boolean isFire2() {
        return (this.input & FIRE2) == FIRE2;
    }

    void encode(final ByteBuffer buffer) {
        buffer.putInt(input);
    }

    static KeyboardInput decode(final ByteBuffer buffer) {
        return new KeyboardInput(buffer.getInt());
    }
}
