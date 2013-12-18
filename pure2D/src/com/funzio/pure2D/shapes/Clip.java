/**
 * 
 */
package com.funzio.pure2D.shapes;

import android.graphics.PointF;
import android.graphics.RectF;

import org.xmlpull.v1.XmlPullParser;

import com.funzio.pure2D.Playable;
import com.funzio.pure2D.atlas.AtlasFrameSet;
import com.funzio.pure2D.ui.UIConfig;
import com.funzio.pure2D.ui.UIManager;

/**
 * @author long
 */
public class Clip extends Sprite implements Playable {
    // XML attributes
    protected static final String ATT_PLAY_AT = "playAt";
    protected static final String ATT_STOP_AT = "stopAt";

    private int mLoop = LOOP_REPEAT;
    private boolean mPlaying = true;
    private int mCurrentFrame = 0;
    private int mPreviousFrame = -1;
    private int mNumFrames = 0;
    private AtlasFrameSet mFrameSet;
    private int mPendingTime = 0;
    private int mAccumulatedFrames = 0;

    public Clip() {
        super();

        setSizeToTexture(false);
    }

    public Clip(final AtlasFrameSet frameSet) {
        super();

        setSizeToTexture(false);
        setAtlasFrameSet(frameSet);
    }

    public void setAtlasFrameSet(final AtlasFrameSet frameSet) {
        mFrameSet = frameSet;

        if (frameSet != null) {
            mNumFrames = frameSet.getNumFrames();
            setFps(frameSet.getFps());
            // if there is a loop mode
            final int loopMode = frameSet.getLoopMode();
            if (loopMode >= 0) {
                setLoop(loopMode);
            }

            // check current frame
            if (mCurrentFrame >= mNumFrames) {
                // start from first frame
                mCurrentFrame = 0;
            }

            setAtlasFrame(mNumFrames > 0 ? frameSet.getFrame(mCurrentFrame) : null);
        } else {
            mNumFrames = 0;
        }
    }

    public AtlasFrameSet getAtlasFrameSet() {
        return mFrameSet;
    }

    @Override
    public RectF getFrameRect(final int frame) {
        if (mFrameSet == null) {
            return null;
        } else {
            return new RectF(mFrameSet.getFrame(frame).getRect());
        }
    }

    @Override
    public void setOriginAtCenter() {
        if (mFrameSet != null) {
            final PointF maxSize = mFrameSet.getFrameMaxSize();
            super.setOrigin(maxSize.x * 0.5f - mOffsetX, maxSize.y * 0.5f - mOffsetY);
        } else {
            super.setOriginAtCenter();
        }
    }

    @Override
    public boolean update(final int deltaTime) {

        // async support: do diff check
        if (mFrameSet != null && mFrameSet.getNumFrames() != mNumFrames) {
            setAtlasFrameSet(mFrameSet);
        }

        // update current frame
        if (mCurrentFrame != mPreviousFrame && mFrameSet != null) {
            mPreviousFrame = mCurrentFrame;
            setAtlasFrame(mFrameSet.getFrame(mCurrentFrame));
        }

        // get next frame
        if (mNumFrames > 1 && mPlaying) {
            int frames = 1;
            // if there is specific fps
            if (getFps() > 0) {
                mPendingTime += deltaTime;
                frames = mPendingTime / (int) mFrameDuration;
                if (frames > 0) {
                    mPendingTime %= (int) mFrameDuration;
                }
            }

            if (frames > 0) {
                mAccumulatedFrames += frames;
                mCurrentFrame += frames;
                if (mLoop == LOOP_REPEAT) {
                    if (mCurrentFrame >= mNumFrames) {
                        mCurrentFrame %= mNumFrames;
                    }
                } else if (mLoop == LOOP_REVERSE) {
                    final int cycle = (mNumFrames - 1) * 2;
                    mCurrentFrame = mAccumulatedFrames % cycle;
                    if (mCurrentFrame >= mNumFrames) {
                        mCurrentFrame = cycle - mCurrentFrame;
                    }
                } else {
                    if (mCurrentFrame >= mNumFrames) {
                        // done, stop at last frame
                        mCurrentFrame = mNumFrames - 1;
                        stop();

                        // callback
                        onClipEnd(mFrameSet);
                    }
                }
            }
        }

        return super.update(deltaTime);
    }

    public void play() {
        mPlaying = true;
    }

    public void playAt(final int frame) {
        mCurrentFrame = frame;
        mPendingTime = 0;
        play();
    }

    public void stop() {
        mPlaying = false;
        mPendingTime = 0;
    }

    public void stopAt(final int frame) {
        mCurrentFrame = frame;
        stop();
    }

    /**
     * @return the Looping
     */
    public int getLoop() {
        return mLoop;
    }

    /**
     * @param Looping can be NONE, REPEAT, CIRCLE
     */
    public void setLoop(final int type) {
        mLoop = type;
    }

    /**
     * @return the currentFrame
     */
    public int getCurrentFrame() {
        return mCurrentFrame;
    }

    /**
     * @return the total number of frames
     */
    public int getNumFrames() {
        return mNumFrames;
    }

    public boolean isPlaying() {
        return mPlaying;
    }

    protected void onClipEnd(final AtlasFrameSet frameSet) {
        // TODO
    }

    @Override
    public void setXMLAttributes(final XmlPullParser xmlParser, final UIManager manager) {
        super.setXMLAttributes(xmlParser, manager);

        final String source = xmlParser.getAttributeValue(null, ATT_SOURCE);
        if (source != null && source.endsWith(UIConfig.FILE_JSON)) {
            final String async = xmlParser.getAttributeValue(null, ATT_ASYNC);
            setAtlasFrameSet(manager.getTextureManager().getUriAtlas(manager.evalString(source), async != null ? Boolean.valueOf(async) : UIConfig.DEFAULT_ASYNC));

            final String playAt = xmlParser.getAttributeValue(null, ATT_PLAY_AT);
            if (playAt != null) {
                playAt(Integer.valueOf(playAt));
            } else {

                final String stopAt = xmlParser.getAttributeValue(null, ATT_STOP_AT);
                if (stopAt != null) {
                    stopAt(Integer.valueOf(stopAt));
                }
            }
        }
    }
}
