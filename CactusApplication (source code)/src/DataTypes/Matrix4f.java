/*
 * The MIT License (MIT)
 *
 * Copyright © 2015-2017, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package DataTypes;

import java.nio.FloatBuffer;

/**
 * This class represents a 4x4-Matrix. GLSL equivalent to mat4.
 *
 * @author Heiko Brumme
 */
public class Matrix4f {

    private float m00, m01, m02, m03;
    private float m10, m11, m12, m13;
    private float m20, m21, m22, m23;
    private float m30, m31, m32, m33;

    /**
     * Creates a 4x4 identity matrix.
     */
    public Matrix4f() {
        setIdentity();
    }

    /**
     * Creates a 4x4 matrix with specified columns.
     *
     * @param col1 Vector with values of the first column
     * @param col2 Vector with values of the second column
     * @param col3 Vector with values of the third column
     * @param col4 Vector with values of the fourth column
     */
    public Matrix4f(Vector4f col1, Vector4f col2, Vector4f col3, Vector4f col4) {
        m00 = col1.x;
        m10 = col1.y;
        m20 = col1.z;
        m30 = col1.w;

        m01 = col2.x;
        m11 = col2.y;
        m21 = col2.z;
        m31 = col2.w;

        m02 = col3.x;
        m12 = col3.y;
        m22 = col3.z;
        m32 = col3.w;

        m03 = col4.x;
        m13 = col4.y;
        m23 = col4.z;
        m33 = col4.w;
    }

    /**
     * Sets this matrix to the identity matrix.
     */
    public final void setIdentity() {
        m00 = 1f;
        m11 = 1f;
        m22 = 1f;
        m33 = 1f;

        m01 = 0f;
        m02 = 0f;
        m03 = 0f;
        m10 = 0f;
        m12 = 0f;
        m13 = 0f;
        m20 = 0f;
        m21 = 0f;
        m23 = 0f;
        m30 = 0f;
        m31 = 0f;
        m32 = 0f;
    }

    /**
     * Adds this matrix to another matrix.
     *
     * @param other The other matrix
     *
     * @return Sum of this + other
     */
    public Matrix4f add(Matrix4f other) {
        Matrix4f result = new Matrix4f();

        result.m00 = this.m00 + other.m00;
        result.m10 = this.m10 + other.m10;
        result.m20 = this.m20 + other.m20;
        result.m30 = this.m30 + other.m30;

        result.m01 = this.m01 + other.m01;
        result.m11 = this.m11 + other.m11;
        result.m21 = this.m21 + other.m21;
        result.m31 = this.m31 + other.m31;

        result.m02 = this.m02 + other.m02;
        result.m12 = this.m12 + other.m12;
        result.m22 = this.m22 + other.m22;
        result.m32 = this.m32 + other.m32;

        result.m03 = this.m03 + other.m03;
        result.m13 = this.m13 + other.m13;
        result.m23 = this.m23 + other.m23;
        result.m33 = this.m33 + other.m33;

        return result;
    }


    /**
     * Stores the matrix in a given Buffer.
     *
     * @param buffer The buffer to store the matrix data
     */
    public void toBuffer(FloatBuffer buffer) {
        buffer.put(m00).put(m10).put(m20).put(m30);
        buffer.put(m01).put(m11).put(m21).put(m31);
        buffer.put(m02).put(m12).put(m22).put(m32);
        buffer.put(m03).put(m13).put(m23).put(m33);
        buffer.flip();
    }

    /**
     * Creates a orthographic projection matrix. Similar to
     * <code>glOrtho(left, right, bottom, top, near, far)</code>.
     *
     * @param left   Coordinate for the left vertical clipping pane
     * @param right  Coordinate for the right vertical clipping pane
     * @param bottom Coordinate for the bottom horizontal clipping pane
     * @param top    Coordinate for the bottom horizontal clipping pane
     * @param near   Coordinate for the near depth clipping pane
     * @param far    Coordinate for the far depth clipping pane
     *
     * @return Orthographic matrix
     */
    public static Matrix4f orthographic(float left, float right, float bottom, float top, float near, float far) {
        Matrix4f ortho = new Matrix4f();

        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        ortho.m00 = 2f / (right - left);
        ortho.m11 = 2f / (top - bottom);
        ortho.m22 = -2f / (far - near);
        ortho.m03 = tx;
        ortho.m13 = ty;
        ortho.m23 = tz;

        return ortho;
    }

}
