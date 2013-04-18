/**
 * 
 */
package com.funzio.pure2D.particles.nova;

import java.util.Properties;

import android.graphics.PointF;
import android.util.SparseArray;

import com.funzio.pure2D.animators.Animator;
import com.funzio.pure2D.animators.Timeline;
import com.funzio.pure2D.animators.Timeline.Action;
import com.funzio.pure2D.containers.Container;
import com.funzio.pure2D.containers.DisplayGroup;
import com.funzio.pure2D.particles.Particle;
import com.funzio.pure2D.particles.RectangularEmitter;
import com.funzio.pure2D.particles.nova.vo.AnimatorVO;
import com.funzio.pure2D.particles.nova.vo.EmitterVO;
import com.funzio.pure2D.particles.nova.vo.ParticleVO;
import com.funzio.pure2D.utils.Reusable;

/**
 * @author long
 */
public class NovaEmitter extends RectangularEmitter implements Reusable, Timeline.Listener {

    protected final Timeline mTimeline;
    protected final NovaFactory mFactory;

    protected EmitterVO mEmitterVO;
    protected Properties mProperties;
    protected Animator mAnimator;

    // layers for particles
    protected SparseArray<DisplayGroup> mLayers;

    public NovaEmitter(final NovaFactory factory, final EmitterVO vo, final PointF pos, final Properties properties) {
        super();

        mFactory = factory;
        mEmitterVO = vo;
        mProperties = properties;
        // auto remove
        mRemoveOnFinish = true;

        // main timeline
        mTimeline = new Timeline(mEmitterVO.lifespan, this);

        // define the area size
        setSize(vo.width, vo.height);
        setOriginAtCenter();
        // initial position
        if (pos != null) {
            mPosition.x = pos.x;
            mPosition.y = pos.y;
        }
        // offset position
        mPosition.x += vo.x;
        mPosition.y += vo.y;

        createManipulators();
        createLayers();
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.utils.Reusable#reset(java.lang.Object[])
     */
    @Override
    public void reset(final Object... params) {
        // stop and reset timeline
        mTimeline.reset();

        // stop animator
        if (mAnimator != null) {
            // stop animator first
            mAnimator.stop();
            // reset the animator
            if (mAnimator.getData() instanceof AnimatorVO) {
                ((AnimatorVO) mAnimator.getData()).resetAnimator(this, mAnimator);
            }
        }
    }

    protected void createManipulators() {
        // emitting action for particles
        int size = mEmitterVO.particles.size();
        for (int i = 0; i < size; i++) {
            mTimeline.addAction(new EmitAction(this, mEmitterVO.particles.get(i)));
        }
        // add timeline
        addManipulator(mTimeline);
        // auto start
        mTimeline.start();

        // optional emitter animator
        if (mEmitterVO.animator != null && mEmitterVO.animator != "") {
            mAnimator = mFactory.createAnimator(this, mEmitterVO.animator);
            if (mAnimator != null) {
                addManipulator(mAnimator);
                // auto start
                mAnimator.start();
            }
        }
    }

    protected void createLayers() {
        int size = mEmitterVO.particles.size();
        for (int i = 0; i < size; i++) {
            final ParticleVO particle = mEmitterVO.particles.get(i);
            if (particle.layer > 0) {
                if (mLayers == null) {
                    mLayers = new SparseArray<DisplayGroup>();
                }
                mLayers.put(particle.layer, new DisplayGroup());
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.particles.RectangularEmitter#onParticleFinish(com.funzio.pure2D.particles.Particle)
     */
    @Override
    public void onParticleFinish(final Particle particle) {
        particle.queueEvent(new Runnable() {

            @Override
            public void run() {
                // auto remove
                particle.removeFromParent();

                // add to pool
                if (mFactory.mParticlePool != null) {
                    mFactory.mParticlePool.release((NovaParticle) particle);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.BaseDisplayObject#onAdded(com.funzio.pure2D.containers.Container)
     */
    @Override
    public void onAdded(final Container parent) {
        super.onAdded(parent);

        // add the layers
        if (mLayers != null) {
            final int size = mLayers.size();
            for (int i = 0; i < size; i++) {
                mParent.addChild(mLayers.get(mLayers.keyAt(i)));
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.particles.RectangularEmitter#onRemoved()
     */
    @Override
    public void onRemoved() {
        // remove the layers
        if (mLayers != null) {
            final int size = mLayers.size();
            for (int i = 0; i < size; i++) {
                mLayers.get(mLayers.keyAt(i)).removeFromParent();
            }
            mLayers.clear();
        }

        super.onRemoved();
    }

    @Override
    public void onTimelineComplete(final Timeline timeline) {
        // timeline is done, finish now
        queueFinish();
    }

    /**
     * Timeline Action for emitting paricles
     * 
     * @author long
     */
    private static class EmitAction extends Action {
        private NovaEmitter mEmitter;
        private ParticleVO mParticleVO;

        public EmitAction(final NovaEmitter emitter, final ParticleVO vo) {
            super(vo.start_delay, vo.step_delay, vo.duration);

            mEmitter = emitter;
            mParticleVO = vo;
        }

        @Override
        public void run() {
            // mEmitter.queueEvent(new Runnable() {
            //
            // @Override
            // public void run() {
            // null check
            if (mEmitter.mParent != null) {
                // emit the particles
                Container layer;
                for (int n = 0; n < mParticleVO.step_quantity; n++) {
                    // find the layer
                    layer = mParticleVO.layer > 0 ? mEmitter.mLayers.get(mParticleVO.layer) : mEmitter.mParent;
                    // add to the layer
                    layer.addChild(mEmitter.mFactory.createParticle(mEmitter, mParticleVO));
                }
            }
            // }
            // });

        }
    }

}
