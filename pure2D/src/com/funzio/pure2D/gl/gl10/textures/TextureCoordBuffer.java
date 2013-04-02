/**
 * 
 */
package com.funzio.pure2D.gl.gl10.textures;

import android.graphics.PointF;

import com.funzio.pure2D.gl.GLFloatBuffer;
import com.funzio.pure2D.gl.gl10.GLState;

/**
 * @author long
 */
public class TextureCoordBuffer extends GLFloatBuffer {
    final private static float[] DEFAULT_COORDS = {
            0.0f, 0.0f, // TL
            0.0f, 1.0f, // BL
            1.0f, 0.0f, // TR
            1.0f, 1.0f, // BR
    };

    private float[] mValues;

    public TextureCoordBuffer(final float... textCoords) {
        super(textCoords);
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.gl.GLFloatBuffer#setValues(float[])
     */
    @Override
    public void setValues(final float... values) {
        super.setValues(values);

        if (mValues == null) {
            mValues = new float[values.length];
        }
        // store the values
        for (int i = 0; i < mValues.length; i++) {
            mValues[i] = values[i];
        }
    }

    public float[] getValues() {
        return mValues;
    }

    public void setXYWH(final float x, final float y, final float width, final float height) {
        setValues(//
                x, y + height, // TL
                x, y, // BL
                x + width, y + height, // TR
                x + width, y // BR
        );
    }

    public void scale(final float sx, final float sy) {
        // scale the values
        if (mValues != null) {
            for (int i = 0; i < mValues.length; i++) {
                if (i % 2 == 0) {
                    if (sx != 1) {
                        mValues[i] *= sx;
                    }
                } else if (sy != 1) {
                    mValues[i] *= sy;
                }
            }
            // apply
            setValues(mValues);
        }
    }

    public void scale(final PointF value) {
        scale(value.x, value.y);
    }

    public void apply(final GLState glState) {
        if (mBuffer != null) {
            // gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            glState.setTextureCoordArrayEnabled(true);

            // gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mBuffer);
            glState.setTextureCoordBuffer(this);
        }
    }

    public void unapply(final GLState glState) {
        if (mBuffer != null) {
            // gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            glState.setTextureCoordArrayEnabled(false);
        }
    }

    public static TextureCoordBuffer getDefault() {
        return new TextureCoordBuffer(DEFAULT_COORDS);
    }

    public void flipHorizontal() {
        float x = mValues[0];
        float y = mValues[1];
        // TL <-> TR
        mValues[0] = mValues[4];
        mValues[1] = mValues[5];
        mValues[4] = x;
        mValues[5] = y;

        x = mValues[2];
        y = mValues[3];
        // BL <-> BR
        mValues[2] = mValues[6];
        mValues[3] = mValues[7];
        mValues[6] = x;
        mValues[7] = y;

        setValues(mValues);
    }

    public void flipVertical() {
        float x = mValues[0];
        float y = mValues[1];
        // TL <-> BL
        mValues[0] = mValues[2];
        mValues[1] = mValues[3];
        mValues[2] = x;
        mValues[3] = y;

        x = mValues[4];
        y = mValues[5];
        // TR <-> BR
        mValues[4] = mValues[6];
        mValues[5] = mValues[7];
        mValues[6] = x;
        mValues[7] = y;

        setValues(mValues);
    }

    public void rotateCCW() {
        float x0 = mValues[0];
        float y0 = mValues[1];
        float x1 = mValues[2];
        float y1 = mValues[3];

        // TR
        mValues[0] = mValues[4];
        mValues[1] = mValues[5];
        // TL
        mValues[2] = x0;
        mValues[3] = y0;
        // BR
        mValues[4] = mValues[6];
        mValues[5] = mValues[7];
        // BL
        mValues[6] = x1;
        mValues[7] = y1;

        setValues(mValues);
    }

    // public boolean equals(final TextureCoordBuffer buffer) {
    // return this == buffer || compare(this, buffer);
    // }

    public static void scale(final float[] values, final float sx, final float sy) {
        // scale the values
        for (int i = 0; i < values.length; i++) {
            if (i % 2 == 0) {
                if (sx != 1) {
                    values[i] *= sx;
                }
            } else if (sy != 1) {
                values[i] *= sy;
            }
        }
    }

    // public static boolean compare(final TextureCoordBuffer a, final TextureCoordBuffer b) {
    // return Arrays.equals(a == null ? null : a.mValues, b == null ? null : b.mValues);
    // }

    public static boolean compare(final TextureCoordBuffer a, final TextureCoordBuffer b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.mValues.length != b.mValues.length) {
            return false;
        }
        for (int i = 0; i < a.mValues.length; i++) {
            if (a.mValues[i] != b.mValues[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + mValues[0] + ", " + mValues[1] + ") " + "(" + mValues[2] + ", " + mValues[3] + ") " + "(" + mValues[4] + ", " + mValues[5] + ") " + "(" + mValues[6] + ", " + mValues[7] + ")";
    }
}
