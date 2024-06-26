package com.peihua.fastscroller;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class responsible to animate and provide a fast scroller.
 */
class CustomFastScroller extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {
    @IntDef({STATE_HIDDEN, STATE_VISIBLE, STATE_DRAGGING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    // Scroll thumb not showing
    private static final int STATE_HIDDEN = 0;
    // Scroll thumb visible and moving along with the scrollbar
    private static final int STATE_VISIBLE = 1;
    // Scroll thumb being dragged by user
    private static final int STATE_DRAGGING = 2;

    @IntDef({DRAG_X, DRAG_Y, DRAG_NONE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DragState {
    }

    private static final int DRAG_NONE = 0;
    private static final int DRAG_X = 1;
    private static final int DRAG_Y = 2;

    @IntDef({ANIMATION_STATE_OUT, ANIMATION_STATE_FADING_IN, ANIMATION_STATE_IN,
            ANIMATION_STATE_FADING_OUT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface AnimationState {
    }

    private static final int ANIMATION_STATE_OUT = 0;
    private static final int ANIMATION_STATE_FADING_IN = 1;
    private static final int ANIMATION_STATE_IN = 2;
    private static final int ANIMATION_STATE_FADING_OUT = 3;

    private static final int SHOW_DURATION_MS = 500;
    private static final int HIDE_DELAY_AFTER_VISIBLE_MS = 1500;
    private static final int HIDE_DELAY_AFTER_DRAGGING_MS = 1200;
    private static final int HIDE_DURATION_MS = 500;
    private static final int SCROLLBAR_FULL_OPAQUE = 255;

    private static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    private static final int[] EMPTY_STATE_SET = new int[]{};

    private final int mScrollbarMinimumRange;
    private final int mMargin;

    // Final values for the vertical scroll bar
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    final GradientDrawable mVerticalThumbDrawable;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    final GradientDrawable mVerticalTrackDrawable;
    private final int mVerticalThumbWidth;
    private final int mVerticalTrackWidth;
    private final int mDefaultWidth;
    // Final values for the horizontal scroll bar
    private final GradientDrawable mHorizontalThumbDrawable;
    private final GradientDrawable mHorizontalTrackDrawable;
    private final int mHorizontalThumbHeight;
    private final int mHorizontalTrackHeight;

    // Dynamic values for the vertical scroll bar
    @VisibleForTesting
    int mVerticalThumbHeight;
    @VisibleForTesting
    int mVerticalThumbCenterY;
    @VisibleForTesting
    float mVerticalDragY;

    // Dynamic values for the horizontal scroll bar
    @VisibleForTesting
    int mHorizontalThumbWidth;
    @VisibleForTesting
    int mHorizontalThumbCenterX;
    @VisibleForTesting
    float mHorizontalDragX;

    private int mRecyclerViewWidth = 0;
    private int mRecyclerViewHeight = 0;

    private RecyclerView mRecyclerView;
    /**
     * Whether the document is long/wide enough to require scrolling. If not, we don't show the
     * relevant scroller.
     */
    private boolean mNeedVerticalScrollbar = false;
    private boolean mNeedHorizontalScrollbar = false;
    @State
    private int mState = STATE_HIDDEN;
    @DragState
    private int mDragState = DRAG_NONE;

    private final int[] mVerticalRange = new int[2];
    private final int[] mHorizontalRange = new int[2];
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    final ValueAnimator mShowHideAnimator = ValueAnimator.ofFloat(0, 1);
    final ValueAnimator mScaleAnimator = ValueAnimator.ofFloat(0, 1);
    float mScaleValue = 1f;
    final float mRadius;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    @AnimationState
    int mAnimationState = ANIMATION_STATE_OUT;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide(HIDE_DURATION_MS);
        }
    };
    private final RecyclerView.OnScrollListener
            mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            updateScrollPosition(recyclerView.computeHorizontalScrollOffset(),
                    recyclerView.computeVerticalScrollOffset());
        }
    };

    CustomFastScroller(RecyclerView recyclerView, GradientDrawable verticalThumbDrawable,
                       GradientDrawable verticalTrackDrawable, GradientDrawable horizontalThumbDrawable,
                       GradientDrawable horizontalTrackDrawable, int defaultWidth, int scrollbarMinimumRange,
                       int margin, float radius) {
        mRadius = radius;
        mDefaultWidth = defaultWidth;
        mVerticalThumbDrawable = verticalThumbDrawable;
        mVerticalTrackDrawable = verticalTrackDrawable;
        mHorizontalThumbDrawable = horizontalThumbDrawable;
        mHorizontalTrackDrawable = horizontalTrackDrawable;
        mVerticalThumbWidth = Math.max(defaultWidth, verticalThumbDrawable.getIntrinsicWidth());
        mVerticalTrackWidth = Math.max(defaultWidth, verticalTrackDrawable.getIntrinsicWidth());
        mHorizontalThumbHeight = Math
                .max(defaultWidth, horizontalThumbDrawable.getIntrinsicWidth());
        mHorizontalTrackHeight = Math
                .max(defaultWidth, horizontalTrackDrawable.getIntrinsicWidth());
        mScrollbarMinimumRange = scrollbarMinimumRange;
        mMargin = margin;
        mVerticalThumbDrawable.setAlpha(SCROLLBAR_FULL_OPAQUE);
        mVerticalTrackDrawable.setAlpha(SCROLLBAR_FULL_OPAQUE);

        mShowHideAnimator.addListener(new AnimatorListener());
        mShowHideAnimator.addUpdateListener(new AnimatorUpdater());
        mScaleAnimator.addUpdateListener(new ScaleAnimatorUpdater());
        attachToRecyclerView(recyclerView);
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (mRecyclerView != null) {
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(this);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(this);
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        cancelHide();
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    void requestRedraw() {
        mRecyclerView.invalidate();
    }

    void setState(@State int state) {
        if (state == STATE_DRAGGING && mState != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(PRESSED_STATE_SET);
            cancelHide();
        }

        if (state == STATE_HIDDEN) {
            requestRedraw();
        } else {
            show();
        }

        if (mState == STATE_DRAGGING && state != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(EMPTY_STATE_SET);
            resetHideDelay(HIDE_DELAY_AFTER_DRAGGING_MS);
        } else if (state == STATE_VISIBLE) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS);
        }
        mState = state;
    }

    private boolean isLayoutRTL() {
        return ViewCompat.getLayoutDirection(mRecyclerView) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public boolean isDragging() {
        return mState == STATE_DRAGGING;
    }

    @VisibleForTesting
    boolean isVisible() {
        return mState == STATE_VISIBLE;
    }

    public void show() {
        switch (mAnimationState) {
            case ANIMATION_STATE_FADING_OUT:
                mShowHideAnimator.cancel();
                // fall through
            case ANIMATION_STATE_OUT:
                mAnimationState = ANIMATION_STATE_FADING_IN;
                mShowHideAnimator.setFloatValues((float) mShowHideAnimator.getAnimatedValue(), 1);
                mShowHideAnimator.setDuration(SHOW_DURATION_MS);
                mShowHideAnimator.setStartDelay(0);
                mShowHideAnimator.start();
                break;
        }
    }

    @VisibleForTesting
    void hide(int duration) {
        switch (mAnimationState) {
            case ANIMATION_STATE_FADING_IN:
                mShowHideAnimator.cancel();
                // fall through
            case ANIMATION_STATE_IN:
                mAnimationState = ANIMATION_STATE_FADING_OUT;
                mShowHideAnimator.setFloatValues((float) mShowHideAnimator.getAnimatedValue(), 0);
                mShowHideAnimator.setDuration(duration);
                mShowHideAnimator.start();
                break;
        }
    }

    private void cancelHide() {
        mRecyclerView.removeCallbacks(mHideRunnable);
    }

    private void resetHideDelay(int delay) {
        cancelHide();
        mRecyclerView.postDelayed(mHideRunnable, delay);
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (mRecyclerViewWidth != mRecyclerView.getWidth()
                || mRecyclerViewHeight != mRecyclerView.getHeight()) {
            mRecyclerViewWidth = mRecyclerView.getWidth();
            mRecyclerViewHeight = mRecyclerView.getHeight();
            // This is due to the different events ordering when keyboard is opened or
            // retracted vs rotate. Hence to avoid corner cases we just disable the
            // scroller when size changed, and wait until the scroll position is recomputed
            // before showing it back.
            setState(STATE_HIDDEN);
            return;
        }

        if (mAnimationState != ANIMATION_STATE_OUT) {
            if (mNeedVerticalScrollbar) {
                drawVerticalScrollbar(canvas);
            }
            if (mNeedHorizontalScrollbar) {
                drawHorizontalScrollbar(canvas);
            }
        }
    }

    private void drawVerticalScrollbar(Canvas canvas) {
        int viewWidth = mRecyclerViewWidth;

        int left = (int) (viewWidth - mVerticalThumbWidth * mScaleValue);
        int top = mVerticalThumbCenterY - mVerticalThumbHeight / 2;
        mVerticalThumbDrawable.setCornerRadius(mRadius * mScaleValue);
        mVerticalThumbDrawable.setBounds(0, 0, (int) (mVerticalThumbWidth * mScaleValue), mVerticalThumbHeight);
        mVerticalTrackDrawable.setCornerRadius(mRadius * mScaleValue);
        mVerticalTrackDrawable
                .setBounds(0, 0, (int) (mVerticalTrackWidth * mScaleValue), mRecyclerViewHeight);

        if (isLayoutRTL()) {
            mVerticalTrackDrawable.draw(canvas);
            canvas.translate(mVerticalThumbWidth, top);
            canvas.scale(-1, 1);
            mVerticalThumbDrawable.draw(canvas);
            canvas.scale(-1, 1);
            canvas.translate(-mVerticalThumbWidth, -top);
        } else {
            canvas.translate(left, 0);
            mVerticalTrackDrawable.draw(canvas);
            canvas.translate(0, top);
            mVerticalThumbDrawable.draw(canvas);
            canvas.translate(-left, -top);
        }
    }

    private void drawHorizontalScrollbar(Canvas canvas) {
        int viewHeight = mRecyclerViewHeight;

        int top = (int) (viewHeight - mHorizontalThumbHeight * mScaleValue);
        int left = mHorizontalThumbCenterX - mHorizontalThumbWidth / 2;
        mHorizontalThumbDrawable.setCornerRadius(mRadius * mScaleValue);
        mHorizontalThumbDrawable.setBounds(0, 0, mHorizontalThumbWidth, (int) (mHorizontalThumbHeight* mScaleValue));
        mHorizontalTrackDrawable.setCornerRadius(mRadius * mScaleValue);
        mHorizontalTrackDrawable
                .setBounds(0, 0, mRecyclerViewWidth, (int) (mHorizontalTrackHeight* mScaleValue));

        canvas.translate(0, top);
        mHorizontalTrackDrawable.draw(canvas);
        canvas.translate(left, 0);
        mHorizontalThumbDrawable.draw(canvas);
        canvas.translate(-left, -top);
    }

    /**
     * Notify the scroller of external change of the scroll, e.g. through dragging or flinging on
     * the view itself.
     *
     * @param offsetX The new scroll X offset.
     * @param offsetY The new scroll Y offset.
     */
    void updateScrollPosition(int offsetX, int offsetY) {
        int verticalContentLength = mRecyclerView.computeVerticalScrollRange();
        int verticalVisibleLength = mRecyclerViewHeight;
        mNeedVerticalScrollbar = verticalContentLength - verticalVisibleLength > 0
                && mRecyclerViewHeight >= mScrollbarMinimumRange;

        int horizontalContentLength = mRecyclerView.computeHorizontalScrollRange();
        int horizontalVisibleLength = mRecyclerViewWidth;
        mNeedHorizontalScrollbar = horizontalContentLength - horizontalVisibleLength > 0
                && mRecyclerViewWidth >= mScrollbarMinimumRange;

        if (!mNeedVerticalScrollbar && !mNeedHorizontalScrollbar) {
            if (mState != STATE_HIDDEN) {
                setState(STATE_HIDDEN);
            }
            return;
        }

        if (mNeedVerticalScrollbar) {
            float middleScreenPos = offsetY + verticalVisibleLength / 2.0f;
            mVerticalThumbCenterY =
                    (int) ((verticalVisibleLength * middleScreenPos) / verticalContentLength);
            mVerticalThumbHeight = Math.min(verticalVisibleLength,
                    (verticalVisibleLength * verticalVisibleLength) / verticalContentLength);
        }

        if (mNeedHorizontalScrollbar) {
            float middleScreenPos = offsetX + horizontalVisibleLength / 2.0f;
            mHorizontalThumbCenterX =
                    (int) ((horizontalVisibleLength * middleScreenPos) / horizontalContentLength);
            mHorizontalThumbWidth = Math.min(horizontalVisibleLength,
                    (horizontalVisibleLength * horizontalVisibleLength) / horizontalContentLength);
        }

        if (mState == STATE_HIDDEN || mState == STATE_VISIBLE) {
            setState(STATE_VISIBLE);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView,
                                         @NonNull MotionEvent ev) {
        final boolean handled;
        if (mState == STATE_VISIBLE) {
            boolean insideVerticalThumb = isPointInsideVerticalThumb(ev.getX(), ev.getY());
            boolean insideHorizontalThumb = isPointInsideHorizontalThumb(ev.getX(), ev.getY());
            if (ev.getAction() == MotionEvent.ACTION_DOWN
                    && (insideVerticalThumb || insideHorizontalThumb)) {
                if (insideHorizontalThumb) {
                    mDragState = DRAG_X;
                    mHorizontalDragX = (int) ev.getX();
                } else if (insideVerticalThumb) {
                    mDragState = DRAG_Y;
                    mVerticalDragY = (int) ev.getY();
                }

                setState(STATE_DRAGGING);
                handled = true;
            } else {
                handled = false;
            }
        } else if (mState == STATE_DRAGGING) {
            handled = true;
        } else {
            handled = false;
        }
        return handled;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent me) {
        if (mState == STATE_HIDDEN) {
            return;
        }

        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            boolean insideVerticalThumb = isPointInsideVerticalThumb(me.getX(), me.getY());
            boolean insideHorizontalThumb = isPointInsideHorizontalThumb(me.getX(), me.getY());
            if (insideVerticalThumb || insideHorizontalThumb) {
                if (insideHorizontalThumb) {
                    mDragState = DRAG_X;
                    mHorizontalDragX = (int) me.getX();
                } else if (insideVerticalThumb) {
                    mDragState = DRAG_Y;
                    mVerticalDragY = (int) me.getY();
                }
                setState(STATE_DRAGGING);
                showScaleAnimation();
            }
        } else if (me.getAction() == MotionEvent.ACTION_UP && mState == STATE_DRAGGING) {
            mVerticalDragY = 0;
            mHorizontalDragX = 0;
            setState(STATE_VISIBLE);
            mDragState = DRAG_NONE;
            hideScaleAnimation();
        } else if (me.getAction() == MotionEvent.ACTION_MOVE && mState == STATE_DRAGGING) {
            show();
            if (mDragState == DRAG_X) {
                horizontalScrollTo(me.getX());
            }
            if (mDragState == DRAG_Y) {
                verticalScrollTo(me.getY());
            }
        }
    }

    private void hideScaleAnimation() {
        mScaleAnimator.setFloatValues((Float) mScaleAnimator.getAnimatedValue(), 1);
        mScaleAnimator.setDuration(SHOW_DURATION_MS);
        mScaleAnimator.start();
    }

    private void showScaleAnimation() {
        mScaleAnimator.setFloatValues(1, 3);
        mScaleAnimator.setDuration(SHOW_DURATION_MS);
        mScaleAnimator.start();
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private void verticalScrollTo(float y) {
        final int[] scrollbarRange = getVerticalRange();
        y = Math.max(scrollbarRange[0], Math.min(scrollbarRange[1], y));
        if (Math.abs(mVerticalThumbCenterY - y) < 2) {
            return;
        }
        int scrollingBy = scrollTo(mVerticalDragY, y, scrollbarRange,
                mRecyclerView.computeVerticalScrollRange(),
                mRecyclerView.computeVerticalScrollOffset(), mRecyclerViewHeight);
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(0, scrollingBy);
        }
        mVerticalDragY = y;
        requestRedraw();
    }

    private void horizontalScrollTo(float x) {
        final int[] scrollbarRange = getHorizontalRange();
        x = Math.max(scrollbarRange[0], Math.min(scrollbarRange[1], x));
        if (Math.abs(mHorizontalThumbCenterX - x) < 2) {
            return;
        }

        int scrollingBy = scrollTo(mHorizontalDragX, x, scrollbarRange,
                mRecyclerView.computeHorizontalScrollRange(),
                mRecyclerView.computeHorizontalScrollOffset(), mRecyclerViewWidth);
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(scrollingBy, 0);
        }

        mHorizontalDragX = x;
    }

    private int scrollTo(float oldDragPos, float newDragPos, int[] scrollbarRange, int scrollRange,
                         int scrollOffset, int viewLength) {
        int scrollbarLength = scrollbarRange[1] - scrollbarRange[0];
        if (scrollbarLength == 0) {
            return 0;
        }
        float percentage = ((newDragPos - oldDragPos) / (float) scrollbarLength);
        int totalPossibleOffset = scrollRange - viewLength;
        int scrollingBy = (int) (percentage * totalPossibleOffset);
        int absoluteOffset = scrollOffset + scrollingBy;
        if (absoluteOffset < totalPossibleOffset && absoluteOffset >= 0) {
            return scrollingBy;
        } else {
            return 0;
        }
    }

    @VisibleForTesting
    boolean isPointInsideVerticalThumb(float x, float y) {
        return (isLayoutRTL() ? x <= mVerticalThumbWidth
                : x >= mRecyclerViewWidth - mVerticalThumbWidth)
                && y >= mVerticalThumbCenterY - mVerticalThumbHeight / 2f
                && y <= mVerticalThumbCenterY + mVerticalThumbHeight / 2f;
    }

    @VisibleForTesting
    boolean isPointInsideHorizontalThumb(float x, float y) {
        return (y >= mRecyclerViewHeight - mHorizontalThumbHeight)
                && x >= mHorizontalThumbCenterX - mHorizontalThumbWidth / 2f
                && x <= mHorizontalThumbCenterX + mHorizontalThumbWidth / 2f;
    }

    @VisibleForTesting
    Drawable getHorizontalTrackDrawable() {
        return mHorizontalTrackDrawable;
    }

    @VisibleForTesting
    Drawable getHorizontalThumbDrawable() {
        return mHorizontalThumbDrawable;
    }

    @VisibleForTesting
    Drawable getVerticalTrackDrawable() {
        return mVerticalTrackDrawable;
    }

    @VisibleForTesting
    Drawable getVerticalThumbDrawable() {
        return mVerticalThumbDrawable;
    }

    /**
     * Gets the (min, max) vertical positions of the vertical scroll bar.
     */
    private int[] getVerticalRange() {
        mVerticalRange[0] = mMargin;
        mVerticalRange[1] = mRecyclerViewHeight - mMargin;
        return mVerticalRange;
    }

    /**
     * Gets the (min, max) horizontal positions of the horizontal scroll bar.
     */
    private int[] getHorizontalRange() {
        mHorizontalRange[0] = mMargin;
        mHorizontalRange[1] = mRecyclerViewWidth - mMargin;
        return mHorizontalRange;
    }

    private class AnimatorListener extends AnimatorListenerAdapter {

        private boolean mCanceled = false;

        AnimatorListener() {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // Cancel is always followed by a new directive, so don't update state.
            if (mCanceled) {
                mCanceled = false;
                return;
            }
            if ((float) mShowHideAnimator.getAnimatedValue() == 0) {
                mAnimationState = ANIMATION_STATE_OUT;
                setState(STATE_HIDDEN);
            } else {
                mAnimationState = ANIMATION_STATE_IN;
                requestRedraw();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mCanceled = true;
        }
    }

    private class AnimatorUpdater implements ValueAnimator.AnimatorUpdateListener {
        AnimatorUpdater() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int alpha = (int) (SCROLLBAR_FULL_OPAQUE * ((float) valueAnimator.getAnimatedValue()));
            mVerticalThumbDrawable.setAlpha(alpha);
            mVerticalTrackDrawable.setAlpha(alpha);
            requestRedraw();
        }
    }

    private class ScaleAnimatorUpdater implements ValueAnimator.AnimatorUpdateListener {
        ScaleAnimatorUpdater() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mScaleValue = (float) valueAnimator.getAnimatedValue();
            requestRedraw();
        }
    }

    public static void initFastScroller(RecyclerView recyclerView) {
        initFastScroller(recyclerView, Color.parseColor("#0ff000"), Color.parseColor("#80ffffff"), 20);
    }

    public static void initFastScroller(RecyclerView recyclerView, int thumbColor, int trackColor, float radius) {

        GradientDrawable thumbDrawable = DrawableUtil.getGradientDrawable(thumbColor, radius);
        GradientDrawable trackDrawable = DrawableUtil.getGradientDrawable(trackColor, radius);
        initFastScroller(recyclerView, thumbDrawable, trackDrawable, radius);
    }
//    public static void initFastScroller(RecyclerView recyclerView) {
//
//        Context context = recyclerView.getContext();
//        StateListDrawable thumbDrawable = (StateListDrawable) ContextCompat.getDrawable(context, R.drawable.fast_scroll_thumb);
//        Drawable trackDrawable = ContextCompat.getDrawable(context, R.drawable.fast_scroll_track);
//        initFastScroller(recyclerView, thumbDrawable, trackDrawable);
//    }

    public static void initFastScroller(RecyclerView recyclerView, GradientDrawable thumbDrawable, GradientDrawable trackDrawable, float radius) {
        if (thumbDrawable == null || trackDrawable == null) {
            throw new IllegalArgumentException(
                    "Trying to set fast scroller without both required drawables.");
        }
        initFastScroller(recyclerView, thumbDrawable, trackDrawable, thumbDrawable, trackDrawable, radius);
    }

    public static void initFastScroller(RecyclerView recyclerView,
                                        GradientDrawable verticalThumbDrawable,
                                        GradientDrawable verticalTrackDrawable,
                                        GradientDrawable horizontalThumbDrawable,
                                        GradientDrawable horizontalTrackDrawable, float radius) {
        if (verticalThumbDrawable == null || verticalTrackDrawable == null
                || horizontalThumbDrawable == null || horizontalTrackDrawable == null) {
            throw new IllegalArgumentException(
                    "Trying to set fast scroller without both required drawables.");
        }

        Resources resources = recyclerView.getContext().getResources();
        new CustomFastScroller(recyclerView, verticalThumbDrawable, verticalTrackDrawable,
                horizontalThumbDrawable, horizontalTrackDrawable,
                resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(R.dimen.fastscroll_margin), radius);
    }
}

