/*
 * This file is copyright 2013 by Johan Maasing
 * You have no rights to this file. You may not distribute it in any form or shape.
 */
package nu.zoom.corridors.math;

import java.io.Serializable;

/**
 * *****************************************************************************
 * Gotten from libGDX package com.badlogic.gdx.math; Modified by Johan Maasing
 *
 * Copyright 2011 See NOTICE.txt file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *****************************************************************************
 */
/**
 * Encapsulates a 3D vector. Allows chaining operations by returning a reference
 * to itself in all modification methods.
 *
 * @author badlogicgames@gmail.com
 */
public class Vector3f extends Tuple3f implements Serializable {

    private static final long serialVersionUID = 3840054589595372522L;
    /**
     * Static temporary vector. Use with care! Use only when sure other code
     * will not also use this.
     *
     * @see #tmp()
     */
    public final static Vector3f tmp = new Vector3f();
    /**
     * Static temporary vector. Use with care! Use only when sure other code
     * will not also use this.
     *
     * @see #tmp()
     */
    public final static Vector3f tmp2 = new Vector3f();
    /**
     * Static temporary vector. Use with care! Use only when sure other code
     * will not also use this.
     *
     * @see #tmp()
     */
    public final static Vector3f tmp3 = new Vector3f();
    public final static Vector3f X = new Vector3f(1, 0, 0);
    public final static Vector3f Y = new Vector3f(0, 1, 0);
    public final static Vector3f Z = new Vector3f(0, 0, 1);
    public final static Vector3f Zero = new Vector3f(0, 0, 0);

    /**
     * Constructs a vector at (0,0,0)
     */
    public Vector3f() {
    }

    /**
     * Creates a vector with the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     */
    public Vector3f(float x, float y, float z) {

        this.set(x, y, z);
    }

    /**
     * Creates a vector from the given vector
     *
     * @param vector The vector
     */
    public Vector3f(Vector3f vector) {

        this.set(vector);
    }

    /**
     * Creates a vector from the given array. The array must have at least 3
     * elements.
     *
     * @param values The array
     */
    public Vector3f(float[] values) {

        this.set(values[0], values[1], values[2]);
    }

    /**
     * Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @return this vector for chaining
     */
    public Vector3f set(float x, float y, float z) {

        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Sets the components of the given vector
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector3f set(Vector3f vector) {

        return this.set(vector.x, vector.y, vector.z);
    }

    /**
     * Sets the components from the array. The array must have at least 3
     * elements
     *
     * @param values The array
     * @return this vector for chaining
     */
    public Vector3f set(float[] values) {

        return this.set(values[0], values[1], values[2]);
    }

    /**
     * @return a copy of this vector
     */
    public Vector3f cpy() {

        return new Vector3f(this);
    }

    /**
     * NEVER EVER SAVE THIS REFERENCE! Do not use this unless you are aware of
     * the side-effects, e.g. other methods might call this as well.
     *
     * @return a temporary copy of this vector
     */
    private Vector3f tmp() {

        return tmp.set(this);
    }

    /**
     * NEVER EVER SAVE THIS REFERENCE! Do not use this unless you are aware of
     * the side-effects, e.g. other methods might call this as well.
     *
     * @return a temporary copy of this vector
     */
    public Vector3f tmp2() {

        return tmp2.set(this);
    }

    /**
     * NEVER EVER SAVE THIS REFERENCE! Do not use this unless you are aware of
     * the side-effects, e.g. other methods might call this as well.
     *
     * @return a temporary copy of this vector
     */
    Vector3f tmp3() {

        return tmp3.set(this);
    }

    /**
     * Adds the given vector to this vector
     *
     * @param vector The other vector
     * @return This vector for chaining
     */
    public Vector3f add(Vector3f vector) {

        return this.add(vector.x, vector.y, vector.z);
    }

    /**
     * Adds the given vector to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining.
     */
    public Vector3f add(float x, float y, float z) {

        return this.set(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds the given value to all three components of the vector.
     *
     * @param values The value
     * @return This vector for chaining
     */
    public Vector3f add(float values) {

        return this.set(this.x + values, this.y + values, this.z + values);
    }

    /**
     * Subtracts the given vector from this vector
     *
     * @param a_vec The other vector
     * @return This vector for chaining
     */
    public Vector3f sub(Vector3f a_vec) {

        return this.sub(a_vec.x, a_vec.y, a_vec.z);
    }

    /**
     * Subtracts the other vector from this vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector3f sub(float x, float y, float z) {

        return this.set(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtracts the given value from all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3f sub(float value) {

        return this.set(this.x - value, this.y - value, this.z - value);
    }

    /**
     * Multiplies all components of this vector by the given value
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3f mul(float value) {

        return this.set(this.x * value, this.y * value, this.z * value);
    }

    /**
     * Multiplies all components of this vector by the given Vector3f's values
     *
     * @param other The Vector3f to multiply by
     * @return This vector for chaining
     */
    public Vector3f mul(Vector3f other) {

        return this.mul(other.x, other.y, other.z);
    }

    /**
     * Multiplies all components of this vector by the given values
     *
     * @param vx X value
     * @param vy Y value
     * @param vz Z value
     * @return This vector for chaining
     */
    public Vector3f mul(float vx, float vy, float vz) {

        return this.set(this.x * vx, this.y * vy, this.z * vz);
    }

    /**
     * Divides all components of this vector by the given value
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3f div(float value) {

        return this.mul(1 / value);
    }

    public Vector3f div(float vx, float vy, float vz) {

        return this.mul(1 / vx, 1 / vy, 1 / vz);
    }

    public Vector3f div(Vector3f other) {

        return this.mul(1 / other.x, 1 / other.y, 1 / other.z);
    }

    /**
     * @return The euclidian length
     */
    public float len() {

        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * @return The squared euclidian length
     */
    public float len2() {

        return x * x + y * y + z * z;
    }

    /**
     * @param vector The other vector
     * @return Wether this and the other vector are equal
     */
    public boolean idt(Vector3f vector) {

        return x == vector.x && y == vector.y && z == vector.z;
    }

    /**
     * @param vector The other vector
     * @return The euclidian distance between this and the other vector
     */
    public float dst(Vector3f vector) {

        float a = vector.x - x;
        float b = vector.y - y;
        float c = vector.z - z;

        a *= a;
        b *= b;
        c *= c;

        return (float) Math.sqrt(a + b + c);
    }

    /**
     * Normalizes this vector to unit length
     *
     * @return This vector for chaining
     */
    public Vector3f nor() {

        float len = this.len();
        if (len == 0) {
            return this;
        } else {
            return this.div(len);
        }
    }

    /**
     * @param vector The other vector
     * @return The dot product between this and the other vector
     */
    public float dot(Vector3f vector) {

        return x * vector.x + y * vector.y + z * vector.z;
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     *
     * @param vector The other vector
     * @return This vector for chaining
     */
    public Vector3f crs(Vector3f vector) {

        return this.set(y * vector.z - z * vector.y, z * vector.x - x * vector.z, x * vector.y - y * vector.x);
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector3f crs(float x, float y, float z) {

        return this.set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
    }

    /**
     * @return Whether this vector is a unit length vector
     */
    public boolean isUnit() {

        return this.len() == 1;
    }

    /**
     * @return Whether this vector is a zero vector
     */
    public boolean isZero() {

        return x == 0 && y == 0 && z == 0;
    }

    /**
     * Linearly interpolates between this vector and the target vector by alpha
     * which is in the range [0,1]. The result is stored in this vector.
     *
     * @param target The target vector
     * @param alpha The interpolation coefficient
     * @return This vector for chaining.
     */
    public Vector3f lerp(Vector3f target, float alpha) {

        Vector3f r = this.mul(1.0f - alpha);
        r.add(target.tmp().mul(alpha));
        return r;
    }

    /**
     * Spherically interpolates between this vector and the target vector by
     * alpha which is in the range [0,1]. The result is stored in this vector.
     *
     * @param target The target vector
     * @param alpha The interpolation coefficient
     * @return This vector for chaining.
     */
    public Vector3f slerp(Vector3f target, float alpha) {

        float dot = dot(target);
        if (dot > 0.99995 || dot < 0.9995) {
            this.add(target.tmp().sub(this).mul(alpha));
            this.nor();
            return this;
        }

        if (dot > 1) {
            dot = 1;
        }
        if (dot < -1) {
            dot = -1;
        }

        float theta0 = (float) Math.acos(dot);
        float theta = theta0 * alpha;
        Vector3f v2 = target.tmp().sub(x * dot, y * dot, z * dot);
        v2.nor();
        return this.mul((float) Math.cos(theta)).add(v2.mul((float) Math.sin(theta))).nor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return x + "," + y + "," + z;
    }

    /**
     * Returns the dot product between this and the given vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return The dot product
     */
    public float dot(float x, float y, float z) {

        return this.x * x + this.y * y + this.z * z;
    }

    /**
     * Returns the squared distance between this point and the given point
     *
     * @param point The other point
     * @return The squared distance
     */
    public float dst2(Vector3f point) {

        float a = point.x - x;
        float b = point.y - y;
        float c = point.z - z;

        a *= a;
        b *= b;
        c *= c;

        return a + b + c;
    }

    /**
     * Returns the squared distance between this point and the given point
     *
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @param z The z-component of the other point
     * @return The squared distance
     */
    public float dst2(float x, float y, float z) {

        float a = x - this.x;
        float b = y - this.y;
        float c = z - this.z;

        a *= a;
        b *= b;
        c *= c;

        return a + b + c;
    }

    public float dst(float x, float y, float z) {

        return (float) Math.sqrt(dst2(x, y, z));
    }

    /**
     * Compares this vector with the other vector, using the supplied epsilon
     * for fuzzy equality testing.
     *
     * @param obj
     * @param epsilon
     * @return whether the vectors are the same.
     */
    public boolean epsilonEquals(Vector3f obj, float epsilon) {

        if (obj == null) {
            return false;
        }
        if (Math.abs(obj.x - x) > epsilon) {
            return false;
        }
        if (Math.abs(obj.y - y) > epsilon) {
            return false;
        }
        return Math.abs(obj.z - z) <= epsilon;
    }

    /**
     * Compares this vector with the other vector, using the supplied epsilon
     * for fuzzy equality testing.
     *
     * @return whether the vectors are the same.
     */
    public boolean epsilonEquals(float x, float y, float z, float epsilon) {

        if (Math.abs(x - this.x) > epsilon) {
            return false;
        }
        if (Math.abs(y - this.y) > epsilon) {
            return false;
        }
        return Math.abs(z - this.z) <= epsilon;
    }

    /**
     * Scales the vector components by the given scalars.
     *
     * @param scalarX
     * @param scalarY
     * @param scalarZ
     */
    public Vector3f scale(float scalarX, float scalarY, float scalarZ) {

        x *= scalarX;
        y *= scalarY;
        z *= scalarZ;
        return this;
    }
}
