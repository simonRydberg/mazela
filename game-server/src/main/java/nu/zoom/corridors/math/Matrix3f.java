package nu.zoom.corridors.math;

/**
 * *****************************************************************************
 * Originally from libGDX package com.badlogic.gdx.math; Modified by Johan Maasing
 *
 * Copyright 2011 See NOTICE.txt file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 *****************************************************************************
 */

import java.io.Serializable;

/**
 * A 3x3 <a href="http://en.wikipedia.org/wiki/Row-major_order">column major</a>
 * matrix; useful for 2D transforms.
 *
 * @author mzechner
 */
public class Matrix3f implements Serializable {

	private static final long serialVersionUID = 7907569533774959788L;
	public static final int M00 = 0;
	public static final int M01 = 3;
	public static final int M02 = 6;
	public static final int M10 = 1;
	public static final int M11 = 4;
	public static final int M12 = 7;
	public static final int M20 = 2;
	public static final int M21 = 5;
	public static final int M22 = 8;
	public float[] val = new float[9];
	private final float[] tmp = new float[9];

	public Matrix3f() {

		idt();
	}

	public Matrix3f(Matrix3f matrix) {

		set(matrix);
	}

	/**
	 * Sets this matrix to the identity matrix
	 *
	 * @return This matrix for the purpose of chaining operations.
	 */
	public final Matrix3f idt() {

		val[M00] = 1;
		val[M10] = 0;
		val[M20] = 0;
		val[M01] = 0;
		val[M11] = 1;
		val[M21] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = 1;
		return this;
	}

	/**
	 * Multiplies this matrix with the provided matrix and stores the result in this matrix. For example:
	 * <p/>
	 * <
	 * pre>
	 * A.mul(B) results in A := AB
	 * </pre>
	 *
	 * @param m Matrix to multiply by.
	 * @return This matrix for the purpose of chaining operations together.
	 */
	public Matrix3f mul(Matrix3f m) {

		float v00 = val[M00] * m.val[M00] + val[M01] * m.val[M10] + val[M02] * m.val[M20];
		float v01 = val[M00] * m.val[M01] + val[M01] * m.val[M11] + val[M02] * m.val[M21];
		float v02 = val[M00] * m.val[M02] + val[M01] * m.val[M12] + val[M02] * m.val[M22];

		float v10 = val[M10] * m.val[M00] + val[M11] * m.val[M10] + val[M12] * m.val[M20];
		float v11 = val[M10] * m.val[M01] + val[M11] * m.val[M11] + val[M12] * m.val[M21];
		float v12 = val[M10] * m.val[M02] + val[M11] * m.val[M12] + val[M12] * m.val[M22];

		float v20 = val[M20] * m.val[M00] + val[M21] * m.val[M10] + val[M22] * m.val[M20];
		float v21 = val[M20] * m.val[M01] + val[M21] * m.val[M11] + val[M22] * m.val[M21];
		float v22 = val[M20] * m.val[M02] + val[M21] * m.val[M12] + val[M22] * m.val[M22];

		val[M00] = v00;
		val[M10] = v10;
		val[M20] = v20;
		val[M01] = v01;
		val[M11] = v11;
		val[M21] = v21;
		val[M02] = v02;
		val[M12] = v12;
		val[M22] = v22;

		return this;
	}

	/**
	 * Transform the given 2D vector (assumes the third component is 1) in place. The argument vector is modified and
	 * returned.
	 *
	 * @param v The vector to translate, modifies the given vector in place.
	 * @return The given vector (for chaining method calls).
	 */
	public Tuple2f mul(Tuple2f v) {

		float x1 = v.x * this.val[M00] + v.y * this.val[M01] + this.val[M02];
		v.y = v.x * this.val[M10] + v.y * this.val[M11] + this.val[M12];
		v.x = x1;
		return v;
	}

	/**
	 * Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise order around the z-axis.
	 *
	 * @param angle the angle in radians.
	 * @return This matrix for the purpose of chaining operations.
	 */
	public Matrix3f setToRotation(float angle) {

		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		this.val[M00] = cos;
		this.val[M10] = sin;
		this.val[M20] = 0;

		this.val[M01] = -sin;
		this.val[M11] = cos;
		this.val[M21] = 0;

		this.val[M02] = 0;
		this.val[M12] = 0;
		this.val[M22] = 1;

		return this;
	}

	/**
	 * Sets this matrix to a translation matrix.
	 *
	 * @param x the translation in x
	 * @param y the translation in y
	 * @return This matrix for the purpose of chaining operations.
	 */
	public Matrix3f setToTranslation(float x, float y) {

		this.val[M00] = 1;
		this.val[M10] = 0;
		this.val[M20] = 0;

		this.val[M01] = 0;
		this.val[M11] = 1;
		this.val[M21] = 0;

		this.val[M02] = x;
		this.val[M12] = y;
		this.val[M22] = 1;

		return this;
	}

	/**
	 * Sets this matrix to a translation matrix.
	 *
	 * @param translation The translation vector.
	 * @return This matrix for the purpose of chaining operations.
	 */
	public Matrix3f setToTranslation(Tuple2f translation) {

		this.val[M00] = 1;
		this.val[M10] = 0;
		this.val[M20] = 0;

		this.val[M01] = 0;
		this.val[M11] = 1;
		this.val[M21] = 0;

		this.val[M02] = translation.x;
		this.val[M12] = translation.y;
		this.val[M22] = 1;

		return this;
	}

	/**
	 * Sets this matrix to a scaling matrix.
	 *
	 * @param scaleX the scale in x
	 * @param scaleY the scale in y
	 * @return This matrix for the purpose of chaining operations.
	 */
	public Matrix3f setToScaling(float scaleX, float scaleY) {

		val[M00] = scaleX;
		val[M10] = 0;
		val[M20] = 0;
		val[M01] = 0;
		val[M11] = scaleY;
		val[M21] = 0;
		val[M02] = 0;
		val[M12] = 0;
		val[M22] = 1;
		return this;
	}

	@Override
	public String toString() {

		return "[" + val[0] + "|" + val[3] + "|" + val[6] + "]\n" + "[" + val[1] + "|" + val[4] + "|" + val[7] + "]\n" + "[" + val[2] + "|" + val[5] + "|" + val[8] + "]";
	}

	/**
	 * @return The determinant of this matrix
	 */
	public float det() {

		return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21] - val[M00] * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
	}

	/**
	 * Copies the values from the provided matrix to this matrix.
	 *
	 * @param mat The matrix to copy.
	 * @return This matrix for the purposes of chaining.
	 */
	public final Matrix3f set(Matrix3f mat) {

		System.arraycopy(mat.val, 0, val, 0, val.length);
		return this;
	}

	/**
	 * Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 *
	 * @param vector The translation vector.
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f trn(Tuple2f vector) {

		val[M02] += vector.x;
		val[M12] += vector.y;
		return this;
	}

	/**
	 * Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 *
	 * @param x The x-component of the translation vector.
	 * @param y The y-component of the translation vector.
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f trn(float x, float y) {

		val[M02] += x;
		val[M12] += y;
		return this;
	}

	/**
	 * Adds a translational component to the matrix in the 3rd column. The other columns are untouched.
	 *
	 * @param vector The translation vector. (The z-component of the vector is ignored because this is a 3x3 matrix)
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f trn(Vector3f vector) {

		val[M02] += vector.x;
		val[M12] += vector.y;
		return this;
	}

	/**
	 * Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 *
	 * @param x The x-component of the translation vector.
	 * @param y The y-component of the translation vector.
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f translate(float x, float y) {

		tmp[M00] = 1;
		tmp[M10] = 0;
		tmp[M20] = 0;

		tmp[M01] = 0;
		tmp[M11] = 1;
		tmp[M21] = 0;

		tmp[M02] = x;
		tmp[M12] = y;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/**
	 * Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 *
	 * @param translation The translation vector.
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f translate(Tuple2f translation) {

		tmp[M00] = 1;
		tmp[M10] = 0;
		tmp[M20] = 0;

		tmp[M01] = 0;
		tmp[M11] = 1;
		tmp[M21] = 0;

		tmp[M02] = translation.x;
		tmp[M12] = translation.y;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/**
	 * Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL
	 * ES' 1.x glTranslate/glRotate/glScale.
	 *
	 * @param angle The angle in radians
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f rotate(float angle) {

		if (angle == 0) {
			return this;
		}
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		tmp[M00] = cos;
		tmp[M10] = sin;
		tmp[M20] = 0;

		tmp[M01] = -sin;
		tmp[M11] = cos;
		tmp[M21] = 0;

		tmp[M02] = 0;
		tmp[M12] = 0;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/**
	 * Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 *
	 * @param scaleX The scale in the x-axis.
	 * @param scaleY The scale in the y-axis.
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f scale(float scaleX, float scaleY) {

		tmp[M00] = scaleX;
		tmp[M10] = 0;
		tmp[M20] = 0;
		tmp[M01] = 0;
		tmp[M11] = scaleY;
		tmp[M21] = 0;
		tmp[M02] = 0;
		tmp[M12] = 0;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/**
	 * Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
	 * glTranslate/glRotate/glScale.
	 *
	 * @param scale The vector to scale the matrix by.
	 * @return This matrix for the purpose of chaining.
	 */
	public Matrix3f scale(Tuple2f scale) {

		tmp[M00] = scale.x;
		tmp[M10] = 0;
		tmp[M20] = 0;
		tmp[M01] = 0;
		tmp[M11] = scale.y;
		tmp[M21] = 0;
		tmp[M02] = 0;
		tmp[M12] = 0;
		tmp[M22] = 1;
		mul(val, tmp);
		return this;
	}

	/**
	 * Get the values in this matrix.
	 *
	 * @return The float values that make up this matrix in column-major order.
	 */
	public float[] getValues() {

		return val;
	}

	/**
	 * Scale the matrix in the both the x and y components by the scalar value.
	 *
	 * @param scale The single value that will be used to scale both the x and y components.
	 * @return This matrix for the purpose of chaining methods together.
	 */
	public Matrix3f scl(float scale) {

		val[M00] *= scale;
		val[M11] *= scale;
		return this;
	}

	/**
	 * Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
	 *
	 * @param scale The {@link Vector3} to use to scale this matrix.
	 * @return This matrix for the purpose of chaining methods together.
	 */
	public Matrix3f scl(Tuple2f scale) {

		val[M00] *= scale.x;
		val[M11] *= scale.y;
		return this;
	}

	/**
	 * Scale this matrix using the x and y components of the vector but leave the rest of the matrix alone.
	 *
	 * @param scale The {@link Vector3} to use to scale this matrix. The z component will be ignored.
	 * @return This matrix for the purpose of chaining methods together.
	 */
	public Matrix3f scl(Vector3f scale) {

		val[M00] *= scale.x;
		val[M11] *= scale.y;
		return this;
	}

	/**
	 * Transposes the current matrix.
	 *
	 * @return This matrix for the purpose of chaining methods together.
	 */
	public Matrix3f transpose() {
		// Where MXY you do not have to change MXX
		float v01 = val[M10];
		float v02 = val[M20];
		float v10 = val[M01];
		float v12 = val[M21];
		float v20 = val[M02];
		float v21 = val[M12];
		val[M01] = v01;
		val[M02] = v02;
		val[M10] = v10;
		val[M12] = v12;
		val[M20] = v20;
		val[M21] = v21;
		return this;
	}

	/**
	 * Multiplies matrix a with matrix b in the following manner:
	 * <p/>
	 * <
	 * pre>
	 * mul(A, B) => A := AB
	 * </pre>
	 *
	 * @param mata The float array representing the first matrix. Must have at least 9 elements.
	 * @param matb The float array representing the second matrix. Must have at least 9 elements.
	 */
	private static void mul(float[] mata, float[] matb) {

		float v00 = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20];
		float v01 = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21];
		float v02 = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22];

		float v10 = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20];
		float v11 = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21];
		float v12 = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22];

		float v20 = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20];
		float v21 = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21];
		float v22 = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22];

		mata[M00] = v00;
		mata[M10] = v10;
		mata[M20] = v20;
		mata[M01] = v01;
		mata[M11] = v11;
		mata[M21] = v21;
		mata[M02] = v02;
		mata[M12] = v12;
		mata[M22] = v22;
	}

	/**
	 * Get the smallest Axis-Aligned Bounding Box that encompasses the transformed bounding given. Will transform the
	 * given AABB with this matrix and calculate a new AABB from the transformed vertices of the given AABB.
	 *
	 * @param in The axis aligned bounding box that we want to transform.
	 * @return The smallest AABB that encompasses the transformed bounding box.
	 */
	public BoundingBox getTransformedAABB(final BoundingBox in) {
		Vector2f[] verts = new Vector2f[4];
		verts[0] = new Vector2f(in.getMin().x, in.getMin().y);
		verts[1] = new Vector2f(in.getMax().x, in.getMin().y);
		verts[2] = new Vector2f(in.getMin().x, in.getMax().y);
		verts[3] = new Vector2f(in.getMax().x, in.getMax().y);

		this.mul(verts[0]);
		float minX = verts[0].x;
		float maxX = verts[0].x;
		float minY = verts[0].y;
		float maxY = verts[0].y;
		for (int n = 1; n < verts.length; n++) {
			this.mul(verts[n]);
			minX = Math.min(minX, verts[n].x);
			maxX = Math.max(maxX, verts[n].x);
			minY = Math.min(minY, verts[n].y);
			maxY = Math.max(maxY, verts[n].y);
		}
		return new BoundingBox(new Tuple2f(minX, minY), new Tuple2f(maxX, maxY));
	}
}
