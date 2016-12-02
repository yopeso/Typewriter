package com.yopeso.typewriter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

class TypewriterRefreshDrawable extends BaseRefreshDrawable {

    private TypewriterRefreshLayout parent;

    private boolean mIsAnimationStarted = false;
    private boolean skipRocketAnimation = false;

    private float mPercent;
    private int mScreenWidth;
    private int mTop;

    private static final float BACKGROUND_RATIO = 1.5f;
    private int mBackgroundHeight;

    private final ValueAnimator typewriterAnimator = new ValueAnimator();
    private float mPointCache[] = new float[2];

    private static final long FLAME_BLINKING_DURATION = 300;
    private final ValueAnimator mFlameAnimator = new ValueAnimator();

    {
        mFlameAnimator.setFloatValues(0f, 1f);
        mFlameAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mFlameAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mFlameAnimator.setDuration(FLAME_BLINKING_DURATION);
        mFlameAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFlameScale = setVariable((float) animation.getAnimatedValue());
            }
        });
    }

    private float mFlameScale = 1;

    private static final float CURVE_TARGET_POINT_VALUE_NOT_ANIMATED = Float.MAX_VALUE;
    private static final float CURVE_VERTICAL_POINT_PERCENT = 0.7f;
    //    private final ValueAnimator mCurveAnimator = new ValueAnimator();
    private final ValueAnimator mOffsetAnimator = new ValueAnimator();

    private Drawable drawable;

    TypewriterRefreshDrawable(final TypewriterRefreshLayout layout) {
        super(layout);
        parent = layout;

        layout.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    @Override
    protected void init() {
        int viewWidth = parent.getWidth();
        if (viewWidth <= 0 || viewWidth == mScreenWidth) {
            return;
        }

        setupAnimations();

        drawable = ContextCompat.getDrawable(getContext(), R.drawable.typewritter);

        mScreenWidth = viewWidth;
        mBackgroundHeight = (int) (BACKGROUND_RATIO * mScreenWidth);

        mTop = -parent.getTotalDragDistance();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mScreenWidth <= 0) return;

        final int saveCount = canvas.save();

        canvas.translate(0, mTop);
        canvas.clipRect(0, -mTop, mScreenWidth, parent.getTotalDragDistance());

//        drawFireworks(canvas);

        canvas.restoreToCount(saveCount);
    }

    private float getCurveYStart() {
        return parent.getTotalDragDistance() * (1f + CURVE_VERTICAL_POINT_PERCENT - Math.min(mPercent, 1.0f));
    }

 /*
    private void drawBackground(Canvas canvas) {
        canvas.save();

        float dragPercent = Math.min(1f, Math.abs(mPercent));
        float backgroundScale;
        float scalePercentDelta = dragPercent - SCALE_START_PERCENT;
        if (scalePercentDelta > 0) {
            float scalePercent = scalePercentDelta / (1.0f - SCALE_START_PERCENT);
            backgroundScale = BACKGROUND_INITIAL_SCALE - (BACKGROUND_INITIAL_SCALE - 1.0f) * scalePercent;
        } else {
            backgroundScale = BACKGROUND_INITIAL_SCALE;
        }

        canvas.scale(backgroundScale, backgroundScale, canvas.getWidth() / 2f, parent.getTotalDragDistance() / 2f);

//        mConfig.getBackgroundDrawable().setBounds(0, 0, canvas.getWidth(), parent.getTotalDragDistance());
//        mConfig.getBackgroundDrawable().draw(canvas);
        canvas.restore();
    }

    private void drawRocket(Canvas canvas) {
        if(isTypewriterAnimationFinished) {
            return;
        }

        canvas.save();

        float dragPercent = Math.min(1f, Math.abs(mPercent));
        float scalePercentDelta = dragPercent - SCALE_START_PERCENT;

        float scalePercent = scalePercentDelta / (1.0f - SCALE_START_PERCENT);
        float rocketScale = ROCKET_INITIAL_SCALE + (ROCKET_FINAL_SCALE - ROCKET_INITIAL_SCALE) * scalePercent;
        canvas.scale(rocketScale, rocketScale, canvas.getWidth() / 2.f, canvas.getHeight() / 2.f);

        float offsetX = canvas.getWidth() / 2.f
                - rocketDrawable.getIntrinsicWidth() / 2.f
                + (1f - rocketScale) * rocketDrawable.getIntrinsicWidth() / 2.f;

        float offsetY = mRocketTopOffset
                + (1.0f - dragPercent) * parent.getTotalDragDistance()
                - mTop;
        offsetY -= (
                Math.max(parent.getTotalDragDistance(), mScreenWidth)
                        + rocketDrawable.getIntrinsicHeight()
        ) * mRocketAnimationPercent;

        canvas.rotate((float) getRocketAngle(),
                canvas.getWidth() / 2.f,
                offsetY + rocketDrawable.getIntrinsicHeight() / 2.f);

        float offsetXDelta = 0;
        if (mIsAnimationStarted) {
            int sign = -1;
            float rocketAngle = (float) getRocketAngle();
            if (mPointerPositionX < mScreenWidth / 2.) {
                sign = 1;
            } else {
                rocketAngle = 360 - rocketAngle;
            }

            double rocketAngleRadians = rocketAngle * (Math.PI / 180.);
            double tan = Math.tan(rocketAngleRadians);


//            offsetXDelta = (float) ((parent.getTotalDragDistance() - offsetY) * tan) * sign;
//            if(mIgnoredRocketXOffset == 0) {
//                mIgnoredRocketXOffset = offsetXDelta;
//            }
//            offsetXDelta -= mIgnoredRocketXOffset;

            //rocket smoke
//            final Bubble lastSmokeBubble = mRocketSmokeBubbles.isEmpty() ? null : mRocketSmokeBubbles.get(mRocketSmokeBubbles.size() - 1);
//            int rocketDPositionSign = lastSmokeBubble == null || lastSmokeBubble.getDPosition().getX() < 0 ? 1 : -1;
//
//            float points[] = mapPoints(
//                    canvas,
//                    offsetX + offsetXDelta + rocketDrawable.getIntrinsicWidth() / 2f,
//                    offsetY + rocketDrawable.getIntrinsicHeight());
//
//            if (lastSmokeBubble == null || points[1] < (lastSmokeBubble.getYPos() - mFireworkBubbleRadius)) {
//                mRocketSmokeBuilder
//                        .position(points[0], points[1])
//                        .dPosition(0.05f * rocketDPositionSign, 0.05f)
//                        .radius(mFireworkBubbleRadius / 2f)
//                        .color(Color.WHITE);
//
//                mRocketSmokeBubbles.add(mRocketSmokeBuilder.build());
//            }
        }

        //drawing rocket
//        canvas.translate(offsetX + offsetXDelta, offsetY);
//        rocketDrawable.setBounds(0, 0, rocketDrawable.getIntrinsicWidth(), rocketDrawable.getIntrinsicHeight());
//        rocketDrawable.draw(canvas);


        //rocket flame
//        canvas.translate(
//                -rocketDrawable.getIntrinsicWidth() * rocketScale / 2f,
//                rocketDrawable.getIntrinsicHeight() * rocketScale - flameDrawable.getIntrinsicHeight() / 4f);
//        canvas.scale(mFlameScale, mFlameScale, flameDrawable.getIntrinsicWidth() / 2f, flameDrawable.getIntrinsicHeight() / 2f);


//        flameDrawable.setBounds(0, 0, flameDrawable.getIntrinsicWidth(), flameDrawable.getIntrinsicHeight());
//        flameDrawable.draw(canvas);

        canvas.restore();
    }

    @FloatRange(from = 0, to = 360)
    private double getRocketAngle() {
        double xTouch = mPointerPositionX;
        double yTouch = mPointerPositionY;
        if (xTouch == 0. && yTouch == 0.) {
            return mLastRocketAngle = 0;
        }

        int sign = 1;
        if (xTouch < (mScreenWidth / 2.)) {
            xTouch = mScreenWidth - xTouch;
            sign = -1;
        }
        double xLength = (mScreenWidth - 2. * xTouch) / 2.;
        double yLength = yTouch - mRocketTopOffset;
        double tgAlpha = yLength / xLength;
        double result = Math.atan(tgAlpha) * (180. / Math.PI);

        result = (90. - result) * sign + 180.;
        if (result > 360 - ROCKET_MAX_DEVIATION_ANGLE || result < ROCKET_MAX_DEVIATION_ANGLE) {
            return mLastRocketAngle = result;
        } else {
            return mLastRocketAngle;
        }
    }


    private void drawFireworks(final Canvas canvas) {
        if (!mIsAnimationStarted || rocketAnimationPercent < 0.95f) {
            return;
        }
    }

    private void drawRocketSmoke(Canvas canvas) {
//        boolean isSmokeInvisible = true;
//        for (Bubble b : mRocketSmokeBubbles) {
//            mPaint.setColor(b.getColor());
//            mPaint.setAlpha(b.incrementAlphaAndGet());
//            canvas.drawCircle(b.incrementXAndGet(), b.incrementYAndGet(), b.incrementRadiusAndGet(), mPaint);
//            isSmokeInvisible &= b.isInvisible();
//        }
//
//        if (isSmokeInvisible) {
//            mRocketSmokeBubbles.clear();
//        }
    }


    */

    @Override
    public void setPercent(float percent, boolean invalidate) {
        setPercent(percent);
        if (invalidate) {
            invalidateSelf();
        }
    }

    @Override
    public void setPointerPosition(float x, float y) {
        if (!mIsAnimationStarted) {
//            mPointerPositionX = setVariable(x);
//            mPointerPositionY = setVariable(y);
        }
    }

    private void setPercent(float percent) {
        mPercent = percent;
        if (percent == 0f && mFlameAnimator.isRunning()) {
            mFlameAnimator.cancel();
        } else if (!mFlameAnimator.isRunning()) {
            mFlameAnimator.start();
        }
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        invalidateSelf();
    }

    void setOffsetTopAndBottom(int offsetTop) {
        mTop = offsetTop;
        invalidateSelf();
    }

    @Override
    public void start() {
        resetOrigins();
        mIsAnimationStarted = true;
        typewriterAnimator.start();
    }

    @Override
    public void stop() {
        mIsAnimationStarted = false;
        skipRocketAnimation = false;
        typewriterAnimator.cancel();
        resetOrigins();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, mBackgroundHeight + top);
    }

    @Override
    public boolean isRunning() {
        return mIsAnimationStarted;
    }

    @Override
    protected void setupAnimations() {
        //typewriter Animator
        typewriterAnimator.cancel();
        typewriterAnimator.setDuration(1000);
        typewriterAnimator.setFloatValues(0f, 1f);
        typewriterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                rocketAnimationPercent = !skipRocketAnimation ? (float) valueAnimator.getAnimatedValue() : 1f;
                if (skipRocketAnimation) {
                    valueAnimator.cancel();
                }
            }
        });
        typewriterAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                isTypewriterAnimationFinished = true;
            }
        });

        //curve animation
//        mCurveAnimator.cancel();
//        mCurveAnimator.setDuration(mConfig.getRocketAnimDuration() * 2);
//
//        mCurveAnimator.setValues(
//                PropertyValuesHolder.ofFloat("force", 1f, 0f),
//                PropertyValuesHolder.ofFloat("value", (float) Math.PI, (float) (3f / 2f * Math.PI * 3f)));
//        mCurveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                if(mPercent == 0f) return;
//                float force = (float) valueAnimator.getAnimatedValue("force");
//                float value = (float) valueAnimator.getAnimatedValue("value");
//
//                float maxDy = parent.getTotalDragDistance() * (2f - CURVE_VERTICAL_POINT_PERCENT - Math.min(mPercent, 1.0f));
//                mCurveTargetPointAnimValue = !skipRocketAnimation ? -(float) (maxDy * Math.cos(value) * force) : 0f;
//                if (skipRocketAnimation) {
//                    valueAnimator.cancel();
//                }
//            }
//        });

        //offset animation
        mOffsetAnimator.cancel();
        mOffsetAnimator.setDuration(1000);
        mOffsetAnimator.setInterpolator(new DecelerateInterpolator());
        mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                View targetView = parent.getTargetView();
                if (targetView != null) {
                    //noinspection ResourceType
                    targetView.setTop(skipRocketAnimation ? (int) getCurveYStart() : (Integer) valueAnimator.getAnimatedValue());
                }
            }
        });

        //after curve animation starting offset animation
//        mCurveAnimator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                View targetView = parent.getTargetView();
//                if(targetView != null) {
//                    mOffsetAnimator.setIntValues(targetView.getTop(), (int) getCurveYStart());
//                    mOffsetAnimator.start();
//                }
//            }
//        });
    }


    private float setVariable(float value) {
        invalidateSelf();
        return value;
    }

    private void resetOrigins() {
        setPercent(0f);
//        mRocketSmokeBubbles.clear();
//        mIgnoredRocketXOffset = 0;
//        rocketAnimationPercent = 0;
//        mCurveTargetPointAnimValue = CURVE_TARGET_POINT_VALUE_NOT_ANIMATED;
//
//        isTypewriterAnimationFinished = false;
    }

    private float[] mapPoints(Canvas canvas, float x, float y) {
        mPointCache[0] = x;
        mPointCache[1] = y;
        canvas.getMatrix().mapPoints(mPointCache);
        return mPointCache;
    }

    void setSkipRocketAnimation(boolean skipRocketAnimation) {
        this.skipRocketAnimation = skipRocketAnimation;
    }

    boolean isSkipRocketAnimation() {
        return skipRocketAnimation;
    }
}
