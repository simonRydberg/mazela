/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

public class Tuple4f extends Tuple3f {

    public float w;

    public Tuple4f(final float x, final float y, final float z, final float w) {

        super(x, y, z);
        this.w = w;
    }

    public Tuple4f() {
    }

    public Tuple4f(Tuple4f other) {

        super(other);
        this.w = other.w;
    }
}
