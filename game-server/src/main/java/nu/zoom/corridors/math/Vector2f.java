/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

public class Vector2f extends Tuple2f {

	private static final long serialVersionUID = -6333609426276041233L;

	public Vector2f(float x, float y) {

		super(x, y);
	}

	public Vector2f() {
	}

	public Vector2f(Tuple2f other) {

		super(other);
	}

	public void set(final Tuple2f other) {

		this.x = other.x;
		this.y = other.y;
	}

	public void set(final float x, final float y) {

		this.x = x;
		this.y = y;
	}

	/**
	 * Normalize this vector and store the result in the given argument. It is safe to use this vector as result (i.e.
	 * normalize itself).
	 *
	 * @param result The vector to store the result in. If null will create a new vector. It is safe to use this vector
	 * as result.
	 * @return The normalized result vector.
	 */
	public Vector2f normalize(Vector2f result) {

		if (result == null) {
			result = new Vector2f();
		}
		double length = length();
		float newx = (float) (x / length);
		float newy = (float) (y / length);
		result.x = newx;
		result.y = newy;
		return result;
	}

	/**
	 * Calculate the angle between this and another vector. This will calculate the normalized versions of both this and
	 * the other vector. If you know both vectors are normalized already you can calculate the angle using
	 * Math.acos(v1.dot(v2)) to avoid creating some temporary objects.
	 *
	 * @param other The vector to calculate the angle against.
	 * @return The angle in radians between this and the other vector.
	 */
	public float angleBetween(final Vector2f other) {

		final Vector2f thisNormalized = this.normalize(null);
		final Vector2f otherNormalized = other.normalize(null);
		double dot = thisNormalized.dot(otherNormalized);
		return (float) Math.acos(dot);
	}

	/**
	 * The dot product of this vector and the given argument. It is safe to pass this vector as argument.
	 *
	 * @param other The vector to calculate the dot product against.
	 * @return The dot product of this and the other vector.
	 */
	public double dot(final Tuple2f other) {

		return other.x * this.x + other.y * this.y;
	}

	/**
	 * Get the length of the vector.
	 *
	 * @return The length of the vector.
	 */
	public double length() {

		return Math.sqrt(dot(this));
	}

	/**
	 * Multiply each component of the vector with the given factor.
	 *
	 * @param factor The factor to multiply with.
	 * @param result The vector to store the result in. If null will create a new vector. It is safe to use this vector
	 * as result (mult. itself).
	 * @return The scaled vector result.
	 */
	public Vector2f multiply(final float factor, Vector2f result) {

		if (result == null) {
			result = new Vector2f();
		}
		final float newx = this.x * factor;
		final float newy = this.y * factor;
		result.x = newx;
		result.y = newy;
		return result;
	}

	/**
	 * Add the components of a vector to this vector components (i.e. this.x += other.x)
	 *
	 * @param v THe components to add to this vector.
	 * @param result The vector to store the result in, it is safe to use this vector as result. If null a new vector
	 * will be created to store the result in.
	 * @return The resulting vector.
	 */
	public Vector2f add(final Vector2f v, final Vector2f result) {
		final Vector2f r = (result == null) ? new Vector2f() : result;

		r.x = this.x + v.x;
		r.y = this.y + v.y;
		return r;
	}

	/**
	 * Subtract another vector from this vector.
	 *
	 * @param v The vector to subtract from this vector. May not be null.
	 * @param result The vector to store the result in, it is safe to use this vector as result. If null a new vector
	 * will be created to store the result in.
	 * @return The resulting vector.
	 */
	public Vector2f subtract(final Vector2f v, final Vector2f result) {
		final Vector2f r = (result == null) ? new Vector2f() : result;
		r.x = this.x - v.x;
		r.y = this.y - v.y;
		return r;
	}
}
