/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

public class MathUtil {

	public static final double NANO_DOUBLE = 1000000000d;
	public static final float PI = (float) Math.PI;
	public static final float TWO_PI = PI * 2.0f;
	public static final float HALF_PI = PI / 2.0f;

	public static double nanosToSeconds(long nanos) {

		return nanos / NANO_DOUBLE;
	}

	public static double naonsToMillis(long nanos) {

		return nanos / 1000000d;
	}

	public static double secondsToNanos(double seconds) {

		return NANO_DOUBLE * seconds;
	}

	public static float toPositiveAngle(float angle) {

		while (angle < 0.0f) {
			angle += TWO_PI;
		}
		return angle;
	}

	public static float clampf(float min, float max, float value) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	public static float clampToUnit(float value) {
		return clampf(0.0f, 1.0f, value);
	}
}
