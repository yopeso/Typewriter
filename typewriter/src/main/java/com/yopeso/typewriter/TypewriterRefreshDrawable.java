package com.yopeso.typewriter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import static android.view.Gravity.TOP;

class TypewriterRefreshDrawable extends BaseRefreshDrawable {

    private TypewriterRefreshLayout parent;

    private boolean hasAnimationStarted = false;
    private boolean skipAnimation = false;

    private float percent;
    private float carriageOffsetPercent;
    private float handOffsetPercent;
    private float pageOffsetPercent;

    private int screenWidth;
    private int top;

    private static final float BACKGROUND_RATIO = 1.5f;
    private static final float HAND_OFFSET_FROM_TYPEWRITER = 5f;
    private int backgroundHeight;

    private final ValueAnimator carriageAnimator = new ValueAnimator();
    private final ValueAnimator handAnimator = new ValueAnimator();

    private Drawable coffee;
    private Drawable leftHand;
    private Drawable rightHand;
    private ClipDrawable page;
    private Drawable typewriter;
    private Drawable carriage;
    private Canvas canvas;

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
        if (viewWidth <= 0 || viewWidth == screenWidth) {
            return;
        }

        setupDrawables();
        setupAnimations();

        screenWidth = viewWidth;
        backgroundHeight = (int) (BACKGROUND_RATIO * screenWidth);

        top = -parent.getTotalDragDistance();
    }

    private void setupDrawables() {
        coffee = ContextCompat.getDrawable(getContext(), R.drawable.coffee);
        leftHand = ContextCompat.getDrawable(getContext(), R.drawable.left_hand);
        rightHand = ContextCompat.getDrawable(getContext(), R.drawable.right_hand);
        page = new ClipDrawable(ContextCompat.getDrawable(getContext(), R.drawable.page),
                TOP, ClipDrawable.VERTICAL);
        page.setLevel(4000);
        typewriter = ContextCompat.getDrawable(getContext(), R.drawable.typewriter);
        carriage = ContextCompat.getDrawable(getContext(), R.drawable.carriage);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (screenWidth <= 0) return;

        final int saveCount = canvas.save();

        this.canvas = canvas;

        canvas.translate(0, top);
        canvas.clipRect(0, -top, screenWidth, parent.getTotalDragDistance());

        drawCoffee();
        drawPage();
        drawCarriage();
        drawTypewriter();
        drawLeftHand();
        drawRightHand();

        canvas.restoreToCount(saveCount);
    }

    private void drawTypewriter() {
        int centerXForTypewriter = getCenterXWithTranslation(typewriter.getIntrinsicWidth());
        int centerYForTypewriter = getCenterYWithTranslation(typewriter.getIntrinsicHeight());

        typewriter.setBounds(centerXForTypewriter, centerYForTypewriter,
                centerXForTypewriter + typewriter.getIntrinsicWidth(),
                centerYForTypewriter + typewriter.getIntrinsicHeight());
        typewriter.draw(canvas);
    }

    private void drawCoffee() {
        int coffeeX = getCenterXWithTranslation(coffee.getIntrinsicWidth())
                - (int) (typewriter.getIntrinsicWidth() * 1.5f);
        int coffeeY = getCenterYWithTranslation(coffee.getIntrinsicHeight()
                + coffee.getIntrinsicWidth() * 2);

        coffee.setBounds(coffeeX, coffeeY,
                coffeeX + coffee.getIntrinsicWidth(),
                coffeeY + coffee.getIntrinsicHeight());
        coffee.draw(canvas);
    }


    private void drawLeftHand() {
        int leftHandX = getCenterXWithTranslation(leftHand.getIntrinsicWidth())
                - (int) (typewriter.getIntrinsicWidth() / HAND_OFFSET_FROM_TYPEWRITER);
        int leftHandY = getCenterYWithTranslation(leftHand.getIntrinsicHeight()
                - parent.getTotalDragDistance());
        leftHandY += getHandYOffset();

        if (hasAnimationStarted && percent > 0.95f) {
            leftHandY -= leftHand.getIntrinsicWidth() / 4 * handOffsetPercent;
        }

        leftHand.setBounds(leftHandX, leftHandY,
                leftHandX + leftHand.getIntrinsicWidth(),
                leftHandY + leftHand.getIntrinsicHeight());
        leftHand.draw(canvas);
    }

    private void drawRightHand() {
        int rightHandX = getCenterXWithTranslation(rightHand.getIntrinsicWidth())
                + (int) (typewriter.getIntrinsicWidth() / HAND_OFFSET_FROM_TYPEWRITER);
        int rightHandY = getCenterYWithTranslation((rightHand.getIntrinsicHeight()
                - parent.getTotalDragDistance()));
        rightHandY += getHandYOffset();

        if (hasAnimationStarted && percent > 0.95f) {
            rightHandY += rightHand.getIntrinsicWidth() / 4 * handOffsetPercent;
        }


        rightHand.setBounds(rightHandX, rightHandY,
                rightHandX + rightHand.getIntrinsicWidth(),
                rightHandY + rightHand.getIntrinsicHeight());
        rightHand.draw(canvas);
    }

    private int getHandYOffset() {
        float percent = Math.min(1f, this.percent);
        int offsetY = (getCenterYWithTranslation((int) ((rightHand.getIntrinsicHeight() +
                typewriter.getIntrinsicHeight() * (1f / 3) +
                parent.getTotalDragDistance()) * percent)));
        return offsetY;
    }


    private void drawPage() {
        int pageX = getCenterXWithTranslation(page.getIntrinsicWidth()
                - typewriter.getIntrinsicWidth() * 2 / 3);
        int pageY = getCenterYWithTranslation(page.getIntrinsicHeight()
                + typewriter.getIntrinsicHeight());

        if (hasAnimationStarted && percent > 0.95f) {
            pageX -= typewriter.getIntrinsicWidth() * 2 / 3 * carriageOffsetPercent;
            pageY -= page.getIntrinsicHeight() * 2 / 3 * pageOffsetPercent;
            page.setLevel((int) (4000 + 6000 * pageOffsetPercent));
        }

        page.setBounds(pageX, pageY,
                pageX + page.getIntrinsicWidth(),
                pageY + page.getIntrinsicHeight());
        page.draw(canvas);
    }

    private void drawCarriage() {
        int carriageX = getCenterXWithTranslation(carriage.getIntrinsicWidth()
                - typewriter.getIntrinsicWidth() * 2 / 3);
        int carriageY = getCenterYWithTranslation(carriage.getIntrinsicHeight() * 2
                + typewriter.getIntrinsicHeight());

        if (hasAnimationStarted && percent > 0.95f) {
            carriageX -= typewriter.getIntrinsicWidth() * 2 / 3 * carriageOffsetPercent;
        }

        carriage.setBounds(carriageX, carriageY,
                carriageX + carriage.getIntrinsicWidth(),
                carriageY + carriage.getIntrinsicHeight());
        carriage.draw(canvas);
    }


    private int getCenterXWithTranslation(int width) {
        return canvas.getWidth() / 2 - width / 2;
    }

    private int getCenterYWithTranslation(int height) {
        return parent.getTotalDragDistance() / 2 - height / 2;
    }

    @Override
    protected void setupAnimations() {
        carriageAnimator.cancel();
        carriageAnimator.setDuration(3 * 1000);
        carriageAnimator.setFloatValues(0f, 1f);
        carriageAnimator.setRepeatCount(ValueAnimator.INFINITE);
        carriageAnimator.setRepeatMode(ValueAnimator.RESTART);
        carriageAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                carriageOffsetPercent = !skipAnimation ?
                        setVariable((float) valueAnimator.getAnimatedValue()) : 1f;
                if (skipAnimation) {
                    valueAnimator.cancel();
                }
            }
        });

        carriageAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                if (pageOffsetPercent < 1) {
                    pageOffsetPercent += 0.2f;
                } else {
                    pageOffsetPercent = 0;
                }
            }
        });

        handAnimator.cancel();
        handAnimator.setDuration(200);
        handAnimator.setFloatValues(0f, 1f);
        handAnimator.setRepeatCount(ValueAnimator.INFINITE);
        handAnimator.setRepeatMode(ValueAnimator.REVERSE);
        handAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                handOffsetPercent = !skipAnimation ?
                        setVariable((float) valueAnimator.getAnimatedValue()) : 1f;
                if (skipAnimation) {
                    valueAnimator.cancel();
                }
            }
        });
    }


    @Override
    public void setPercent(float percent, boolean invalidate) {
        setPercent(percent);
        if (invalidate) {
            invalidateSelf();
        }
    }

    @Override
    public void setPointerPosition(float x, float y) {
        if (!hasAnimationStarted) {
        }
    }

    private void setPercent(float percent) {
        this.percent = percent;
        if (percent == 0f && carriageAnimator.isRunning()) {
            cancelAnimation();
        } else if (!carriageAnimator.isRunning()) {
            startAnimation();
        }
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        top += offset;
        invalidateSelf();
    }

    void setOffsetTopAndBottom(int offsetTop) {
        top = offsetTop;
        invalidateSelf();
    }

    @Override
    public void start() {
        resetOrigins();
        hasAnimationStarted = true;
        startAnimation();
    }

    @Override
    public void stop() {
        hasAnimationStarted = false;
        skipAnimation = false;
        cancelAnimation();
        resetOrigins();
    }

    private void startAnimation() {
        carriageAnimator.start();
        handAnimator.start();
    }

    private void cancelAnimation() {
        carriageAnimator.cancel();
        handAnimator.cancel();
    }


    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, backgroundHeight + top);
    }

    @Override
    public boolean isRunning() {
        return hasAnimationStarted;
    }

    private float setVariable(float value) {
        invalidateSelf();
        return value;
    }

    private void resetOrigins() {
        setPercent(0f);
    }

    void setSkipAnimation(boolean skipAnimation) {
        this.skipAnimation = skipAnimation;
    }

    boolean isSkipAnimation() {
        return skipAnimation;
    }
}
