/**
 * 
 */
package com.funzio.pure2D.gl.gl10;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.PointF;

/**
 * @author long
 */
public class QuadBuffer extends VertexBuffer {
    protected PointF mSize = new PointF(0, 0);

    public QuadBuffer() {
        super(GL10.GL_TRIANGLE_STRIP, 4);
    }

    public QuadBuffer(final float x, final float y, final float width, final float height) {
        super(GL10.GL_TRIANGLE_STRIP, 4);

        setXYWH(x, y, width, height);
    }

    public void setXYWH(final float x, final float y, final float width, final float height) {
        mSize.set(width, height);

        setValues(//
                x, y + height, // TL
                x, y, // BL
                x + width, y + height, // TR
                x + width, y // BR
        );
    }

    public PointF getSize() {
        return mSize;
    }

    public boolean isSet() {
        return mSize.x != 0 && mSize.y != 0;
    }
}
