/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

/**
 * Fast random number generator, not thread safe.
 */
public class XORShiftRandom {

    private long current;
    protected static final long dB = (1L << 53) - 1L;
    protected static final double dMult = 1 / (double) dB;

    public XORShiftRandom(final long seed) {

        this.current = seed;
    }

    public long nextLong() {

        current ^= (current << 21);
        current ^= (current >>> 35);
        current ^= (current << 4);
        return current;
    }

    public long next(final int bits) {

        return (int) (nextLong() >>> (64 - bits));
    }

    /**
     * Unit random
     *
     * @return A random double in the range (0,1]
     */
    public double unitRandom() {

        return (nextLong() & dB) * dMult;
    }

    public static void main(String... args) {

        XORShiftRandom r = new XORShiftRandom(System.currentTimeMillis());
        for (int n = 0; n < 1000; n++) {
            System.out.println("" + Long.toBinaryString(r.next(1)));
        }
    }
}
