/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

import java.io.Serializable;

public class Tuple2f implements Serializable {

    private static final long serialVersionUID = -5568379330157608019L;
    public float x;
    public float y;

    public Tuple2f(final float x, final float y) {

        super();
        this.x = x;
        this.y = y;
    }

    public Tuple2f() {

        this(0, 0);
    }

    public Tuple2f(Tuple2f other) {

        this.x = other.x;
        this.y = other.y;
    }

    @Override
    public String toString() {

        return "Tuple2f [" + this.x + ", " + this.y + "]";
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tuple2f other = (Tuple2f) obj;
        return Float.floatToIntBits(x) == Float.floatToIntBits(other.x) && Float.floatToIntBits(y) == Float
                .floatToIntBits(other.y);
    }
}
