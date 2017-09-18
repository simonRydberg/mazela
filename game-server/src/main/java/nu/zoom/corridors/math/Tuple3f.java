/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

public class Tuple3f extends Tuple2f {

    private static final long serialVersionUID = -4420271016232831969L;
    public float z;

    public Tuple3f(final float x, final float y, final float z) {

        super(x, y);
        this.z = z;
    }

    public Tuple3f() {
    }

    public Tuple3f(Tuple3f other) {

        super(other);
        this.z = other.z;
    }

    @Override
    public String toString() {

        return "Tuple3f [z=" + z + ", x=" + x + ", y=" + y + "]";
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Float.floatToIntBits(z);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Tuple3f other = (Tuple3f) obj;
        return Float.floatToIntBits(z) == Float.floatToIntBits(other.z);
    }
}
