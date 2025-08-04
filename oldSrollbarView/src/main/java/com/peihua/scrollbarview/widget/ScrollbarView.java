package com.peihua.scrollbarview.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;

import androidx.annotation.IdRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.peihua.scrollbarview.R;
import com.peihua.scrollbarview.utils.WidgetCompat;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class ScrollbarView extends View {
    private static final IScrollBind mScrollBind;
    private static Method mComputeVerticalScrollRange;
    private static Method mComputeVerticalScrollExtent;
    private static Method mComputeVerticalScrollOffset;
    private float mScrollProgress;
    private boolean mFastScrollable;
    private int mScrollableViewState;
    private View mScrollableView;
    private WeakReference<ViewTreeObserver> viewTreeObserverWeakReference;
    private OnFastScrollListener onFastScrollListener;
    private boolean isThumbShow;
    private boolean mCanScroll;
    private boolean isOverScrolling;
    private IScrollProxy mOverScrollProxy;
    private boolean isOverScroll;
    private int mOverScrollMinThumbLength;
    private int mThumbHeight;
    private int mVibrateDelayTime;
    private final ScrollbarRunnable mUpdateRunnable;
    private final OnTouchListener onTouchListener;
    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    protected int mAlpha;

    protected int mContentHeight;

    protected int scrollBarTop;

    protected int scrollBarLeft;

    protected int mThumbWidth;

    protected int mTrackWidth;

    protected int mThumbTouchHotWidth;

    protected Rect mThumbRect;

    protected Rect mTrackRect;

    protected Rect mFastScrollRegion;

    protected int mThumbType;
    protected Drawable thumbDrawable;
    protected Drawable mTrackDrawable;
    protected int mScrollThumbTint;
    protected int mScrollTrackTint;
    protected float mStartAngle;
    protected float mSweepAngle;
    protected int mMinThumbLength;
    private int mFadeTime;
    private int mState;
    private int mLastX;
    private int mLastY;
    private int mScaledTouchSlop;
    @IdRes
    private int mBandScroll;
    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mScrollBind = new ScrollBindApi23Impl();
        } else {
            mScrollBind = new ScrollBindBaseImpl();
        }
    }
    public interface OnFastScrollListener {
        void onScroll(int scrollX, int scrollY, float scrollProgress);
    }

    public static class ScrollbarRunnable implements Runnable {

        private static final float[] START_ALPHA = {255.0f};

        private static final float[] END_ALPHA = {0.0f};

        private final ScrollbarView mScrollbarView;

        private long mScheduledTime;

        private float[] mCurrentAlpha = new float[1];

        private final Interpolator mInterpolator = new Interpolator(1, 2);

        private int mFlag = 0;

        ScrollbarRunnable(ScrollbarView scrollbarView) {
            this.mScrollbarView = scrollbarView;
        }

        @Override
        public void run() {
            long currentAnimationTimeMillis = AnimationUtils.currentAnimationTimeMillis();
            if (currentAnimationTimeMillis >= this.mScheduledTime) {
                int currentTime = (int) currentAnimationTimeMillis;
                Interpolator interpolator = this.mInterpolator;
                interpolator.setKeyFrame(0, currentTime, START_ALPHA);
                interpolator.setKeyFrame(1, currentTime + this.mScrollbarView.mFadeTime, END_ALPHA);
                this.mFlag = 2;
                this.mScrollbarView.invalidate();
            }
        }
    }

    class OnTouchListenerImpl implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (ScrollbarView.this.isScrolling()) {
                return true;
            }
            ScrollbarView.this.scrollableViewTouchEvent(motionEvent);
            return false;
        }
    }

    class OnGlobalLayoutListenerImpl implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            int scrollableViewVerticalScrollRange = ScrollbarView.this.getScrollableViewVerticalScrollRange();
            int scrollableViewVerticalScrollExtent = ScrollbarView.this.getScrollableViewVerticalScrollExtent();
            ScrollbarView.this.isOverScrolling = scrollableViewVerticalScrollRange > scrollableViewVerticalScrollExtent;
        }
    }

    static {
        try {
            mComputeVerticalScrollRange = View.class.getDeclaredMethod("computeVerticalScrollRange", new Class[0]);
        } catch (NoSuchMethodException unused) {
            mComputeVerticalScrollRange = null;
            Log.w("ScrollbarView", "NoSuchMethodException computeVerticalScrollRange");
        }
        try {
            mComputeVerticalScrollExtent = View.class.getDeclaredMethod("computeVerticalScrollExtent", new Class[0]);
        } catch (NoSuchMethodException unused2) {
            mComputeVerticalScrollExtent = null;
            Log.w("ScrollbarView", "NoSuchMethodException computeVerticalScrollExtent");
        }
        try {
            mComputeVerticalScrollOffset = View.class.getDeclaredMethod("computeVerticalScrollOffset", new Class[0]);
        } catch (NoSuchMethodException unused3) {
            mComputeVerticalScrollOffset = null;
            Log.w("ScrollbarView", "NoSuchMethodException computeVerticalScrollOffset");
        }
    }

    public ScrollbarView(Context context) {
        this(context, null);
    }

    public ScrollbarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.scrollbarViewStyle);
    }

    public ScrollbarView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(WidgetCompat.wrapContext(context, defStyleAttr, R.style.ThemeScrollbarView), attributeSet, defStyleAttr);
        this.mAlpha = 255;
        this.mContentHeight = 0;
        this.scrollBarTop = 0;
        this.scrollBarLeft = 0;
        this.mThumbWidth = 0;
        this.mTrackWidth = 0;
        this.mThumbTouchHotWidth = 0;
        this.mThumbRect = new Rect();
        this.mTrackRect = new Rect();
        this.mFastScrollRegion = new Rect();
        this.mThumbType = 0;
        this.mFadeTime = 250;
        this.mState = 0;
        this.mScrollProgress = 0.0f;
        this.mFastScrollable = true;
        this.mScrollableViewState = 0;
        this.isThumbShow = true;
        this.mCanScroll = true;
        this.isOverScrolling = true;
        this.isOverScroll = false;
        this.mOverScrollMinThumbLength = 0;
        this.mThumbHeight = 0;
        this.mVibrateDelayTime = 0;
        this.onTouchListener = new OnTouchListenerImpl();
        this.onGlobalLayoutListener = new OnGlobalLayoutListenerImpl();
        Context context2 = super.getContext();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context2);
        TypedArray array = context2.obtainStyledAttributes(attributeSet, R.styleable.ScrollbarView, defStyleAttr, R.style.WidgetScrollbarView);
        this.mScaledTouchSlop = viewConfiguration.getScaledTouchSlop();
        int dimensionPixelSize = array.getDimensionPixelSize(R.styleable.ScrollbarView_minThumbLength, 48);
        this.mMinThumbLength = dimensionPixelSize;
        this.mOverScrollMinThumbLength = array.getDimensionPixelSize(R.styleable.ScrollbarView_overScrollMinThumbLength, dimensionPixelSize);
        int dimensionPixelSize2 = array.getDimensionPixelSize(R.styleable.ScrollbarView_minThumbWidth, 16);
        int dimensionPixelSize3 = array.getDimensionPixelSize(R.styleable.ScrollbarView_thumbWidth, dimensionPixelSize2);
        this.mThumbWidth = dimensionPixelSize3;
        if (dimensionPixelSize3 < dimensionPixelSize2) {
            this.mThumbWidth = dimensionPixelSize2;
        }
        this.mBandScroll = array.getResourceId(R.styleable.ScrollbarView_bandScrollView, 0);
        this.mTrackWidth = array.getDimensionPixelSize(R.styleable.ScrollbarView_trackWidth, this.mThumbWidth);
        this.mThumbTouchHotWidth = array.getDimensionPixelSize(R.styleable.ScrollbarView_thumbTouchHotWidth, 16);
        this.mScrollThumbTint = array.getColor(R.styleable.ScrollbarView_scrollThumbTint, ViewCompat.MEASURED_SIZE_MASK);
        this.mScrollTrackTint = array.getColor(R.styleable.ScrollbarView_scrollTrackTint, ViewCompat.MEASURED_SIZE_MASK);
        Drawable drawable = array.getDrawable(R.styleable.ScrollbarView_scrollThumb);
        Drawable drawable2 = drawable;
        if (drawable == null) {
            ScrollbarDrawable scrollbarDrawable = new ScrollbarDrawable();
            scrollbarDrawable.initScrollbarDrawable(context2, attributeSet, defStyleAttr);
            drawable2 = scrollbarDrawable;
        }
        setThumbDrawable(drawable2);
        setTrackDrawable(array.getDrawable(R.styleable.ScrollbarView_scrollTrack));
        this.mThumbType = array.getInt(R.styleable.ScrollbarView_thumbType, 0);
        this.mStartAngle = array.getFloat(R.styleable.ScrollbarView_startAngle, 35.0f);
        this.mSweepAngle = array.getFloat(R.styleable.ScrollbarView_sweepAngle, 110.0f);
        this.mVibrateDelayTime = array.getInteger(R.styleable.ScrollbarView_vibrateDelayTime, 0);
        array.recycle();
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        ScrollbarRunnable runnable = new ScrollbarRunnable(this);
        this.mUpdateRunnable = runnable;
        if (isInEditMode()) {
            this.mThumbRect = new Rect(0, 0, 48, 192);
        }
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(2);
        }
    }

    private void setupScrollableViewObserver() {
        ViewTreeObserver viewTreeObserver;
        View view = this.mScrollableView;
        if (view == null) {
            return;
        }
        if (this.viewTreeObserverWeakReference == null) {
            this.viewTreeObserverWeakReference = new WeakReference<>(view.getViewTreeObserver());
        }
        if (this.onGlobalLayoutListener == null || (viewTreeObserver = this.viewTreeObserverWeakReference.get()) == null || !viewTreeObserver.isAlive()) {
            return;
        }
        viewTreeObserver.addOnGlobalLayoutListener(this.onGlobalLayoutListener);
    }

    private void scheduleFadeOutAnimation(int delayMillis) {
        if (this.isThumbShow) {
            this.mUpdateRunnable.mScheduledTime = AnimationUtils.currentAnimationTimeMillis() + this.mFadeTime;
            this.mUpdateRunnable.mFlag = 1;
            removeCallbacks(this.mUpdateRunnable);
            postDelayed(this.mUpdateRunnable, delayMillis);
        }
    }

    public void scrollableViewTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return;
        }
        int action = motionEvent.getAction();
        int y = (int) motionEvent.getY();
        if (action != MotionEvent.ACTION_UP) {
            if (action == MotionEvent.ACTION_MOVE) {
                if (this.mScrollableViewState == 0) {
                    this.mLastY = y;
                    this.mScrollableViewState = 2;
                    if (canScroll()) {
                        this.mCanScroll = true;
                        updateOnTouch();
                    } else {
                        this.mCanScroll = false;
                        setThumbShow(false);
                    }
                }
                if (!this.isThumbShow || this.mLastY == y) {
                    return;
                }
                this.mLastY = y;
                updateOnTouch();
                return;
            }
            if (action != MotionEvent.ACTION_CANCEL) {
                return;
            }
        }
        this.mScrollableViewState = 0;
        scheduleFadeOutAnimation(1750);
    }

    public static IScrollBind getScrollBindImpl() {
        return mScrollBind;
    }

    private float getScrollProgress() {
        return (this.scrollBarLeft * 1.0f) / (this.mContentHeight - this.scrollBarTop);
    }

    private boolean canScroll() {
        return this.mScrollableView != null && getScrollableViewVerticalScrollRange() > getScrollableViewVerticalScrollExtent();
    }


    public boolean isScrolling() {
        return this.mState != 0;
    }

    private void updateOnTouch() {
        removeCallbacks(this.mUpdateRunnable);
        ScrollbarRunnable runnable = this.mUpdateRunnable;
        if (runnable != null) {
            runnable.mFlag = 1;
        }
        if (!this.isThumbShow) {
            setThumbShow(true);
        }
        updateScrollBarPositionAndLength();
        invalidate();
    }

    private void updateScrollBarPositionAndLength() {
        if (this.mScrollableView == null) {
            return;
        }
        int scrollRange = getScrollableViewVerticalScrollRange();
        int scrollExtent = getScrollableViewVerticalScrollExtent();
        int scrollOffset = getScrollableViewVerticalScrollOffset();
        if (scrollRange <= 0 || scrollRange <= scrollExtent) {
            this.scrollBarTop = 0;
            this.scrollBarLeft = 0;
            this.mThumbRect.setEmpty();
            setThumbShow(false);
            this.mCanScroll = false;
            return;
        }
        int contentHeight = this.mContentHeight;
        int thumbLength = (int) (((scrollExtent * 1.0f) / scrollRange) * contentHeight);
        int minThumbLength = this.mMinThumbLength;
        if (thumbLength < minThumbLength) {
            thumbLength = minThumbLength;
        }
        this.scrollBarTop = thumbLength;
        int thumbOffset = contentHeight - thumbLength;
        int thumbPosition = (int) (((scrollOffset * 1.0f) / (scrollRange - scrollExtent)) * thumbOffset);
        if (thumbPosition <= thumbOffset) {
            thumbOffset = thumbPosition;
        }
        this.scrollBarLeft = thumbOffset;
        int overScrollY = this.mOverScrollProxy.getScrollY();
        if (this.isOverScroll && overScrollY != 0) {
            int overScrollMinThumbLength = this.mOverScrollMinThumbLength;
            int abs = this.mThumbHeight - Math.abs(overScrollY);
            this.scrollBarTop = abs;
            if (abs < overScrollMinThumbLength) {
                this.scrollBarTop = overScrollMinThumbLength;
            }
            this.scrollBarLeft = overScrollY > 0 ? this.mContentHeight - this.scrollBarTop : 0;
        }
        updateScrollBarPosition();
        this.mScrollProgress = getScrollProgress();
    }

    private void updateScrollBarPosition() {
        int width = getWidth() - getPaddingRight();  //计算宽度
        int thumbLeft = width - this.mThumbWidth; // 计算左偏移
        int paddingTop = getPaddingTop() + this.scrollBarLeft; // 计算顶部偏移
        int thumbTop = this.scrollBarTop + paddingTop; // 计算滑块顶部位置
        if (isInverted()) {
            thumbLeft = getPaddingLeft(); // 如果是反向滑动，滑块左偏移为paddingLeft
            width = this.mThumbWidth + thumbLeft; // 如果是反向滑动，重新计算宽度
        }
        //设置滑块矩形区域
        this.mThumbRect.set(thumbLeft, paddingTop, width, thumbTop);
        int thumbHotRight = this.mThumbRect.right;
        int thumbHotLeft = thumbHotRight - this.mThumbTouchHotWidth; // 计算滑块触摸区域左边界
        if (isInverted()) {
            thumbHotLeft = this.mThumbRect.left; // 如果是反向滑动，重新计算触摸区域左边界
            thumbHotRight = this.mThumbTouchHotWidth + thumbHotLeft; // 计算触摸区域右边界

        }
        // 设置滑块触摸区域矩形
        this.mFastScrollRegion.set(thumbHotLeft, this.mThumbRect.top, thumbHotRight, this.mThumbRect.bottom);
    }

    private void setThumbShow(boolean thumbShow) {
        this.isThumbShow = thumbShow;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        Drawable drawable = this.thumbDrawable;
        boolean isStateChanged = false;
        if (drawable != null && drawable.isStateful()) {
            isStateChanged = drawable.setState(drawableState);
        }
        Drawable drawable2 = this.mTrackDrawable;
        if (drawable2 != null && drawable2.isStateful()) {
            isStateChanged |= drawable2.setState(drawableState);
        }
        if (isStateChanged) {
            invalidate();
        }
    }

    public int getFadeTime() {
        return this.mFadeTime;
    }

    public View getScrollableView() {
        return this.mScrollableView;
    }

    /**
     * <p>计算垂直滚动条所表示的垂直范围。</p>
     *
     * <p>范围以任意单位表示，这些单位必须与
     * {@link #computeVerticalScrollExtent（）} 和
     * {@link #computeVerticalScrollOffset（）}。</p>
     *
     * @return 垂直滚动条表示的总垂直范围
     *
     * <p>默认范围是此视图的绘图高度。</p>
     * @see #computeVerticalScrollExtent()
     * @see #computeVerticalScrollOffset()
     */
    int getScrollableViewVerticalScrollExtent() {
        Method method = mComputeVerticalScrollExtent;
        if (this.mScrollableView != null && method != null) {
            try {
                method.setAccessible(true);
                Object invoke = method.invoke(this.mScrollableView);
                if (invoke instanceof Integer) {
                    return ((Integer) invoke).intValue();
                }
            } catch (IllegalAccessException unused) {
                Log.w("ScrollbarView", "IllegalAccessException computeVerticalScrollExtent");
                return -1;
            } catch (InvocationTargetException unused2) {
                Log.w("ScrollbarView", "InvocationTargetException computeVerticalScrollExtent");
                return -1;
            }
        }
        return -1;
    }

    /**
     * <p>计算垂直滚动条拇指的垂直偏移量在水平范围内。此值用于计算位置滚动条轨道内的拇指。</p>
     *
     * <p>范围以任意单位表示，这些单位必须与
     * {@link #computeVerticalScrollRange（）} 和
     * {@link #computeVerticalScrollExtent（）}。</p>
     *
     * <p>默认偏移量是此视图的滚动偏移量。</p>
     *
     * @return 滚动条拇指的垂直偏移量
     * @see #computeVerticalScrollRange()
     * @see #computeVerticalScrollExtent()
     */
    int getScrollableViewVerticalScrollOffset() {
        Method method = mComputeVerticalScrollOffset;
        if (this.mScrollableView != null && method != null) {
            try {
                method.setAccessible(true);
                Object invoke = method.invoke(this.mScrollableView);
                if (invoke instanceof Integer) {
                    return ((Integer) invoke).intValue();
                }
            } catch (IllegalAccessException unused) {
                Log.w("ScrollbarView", "IllegalAccessException computeVerticalScrollOffset");
                return -1;
            } catch (InvocationTargetException unused2) {
                Log.w("ScrollbarView", "InvocationTargetException computeVerticalScrollOffset");
                return -1;
            }
        }
        return -1;
    }

    /**
     * <p>计算垂直滚动条所表示的垂直范围。</p>
     *
     * <p>范围以任意单位表示，这些单位必须与
     * {@link #computeVerticalScrollExtent（）} 和
     * {@link #computeVerticalScrollOffset（）}。</p>
     *
     * @return 垂直滚动条表示的总垂直范围
     *
     * <p>默认范围是此视图的绘图高度。</p>
     * @see #computeVerticalScrollExtent()
     * @see #computeVerticalScrollOffset()
     */
    int getScrollableViewVerticalScrollRange() {
        Method method = mComputeVerticalScrollRange;
        if (this.mScrollableView != null && method != null) {
            try {
                method.setAccessible(true);
                Object invoke = method.invoke(this.mScrollableView);
                if (invoke instanceof Integer) {
                    return ((Integer) invoke).intValue();
                }
            } catch (IllegalAccessException unused) {
                Log.w("ScrollbarView", "IllegalAccessException computeVerticalScrollRange");
                return -1;
            } catch (InvocationTargetException unused2) {
                Log.w("ScrollbarView", "InvocationTargetException computeVerticalScrollRange");
                return -1;
            }
        }
        return -1;
    }

    public float getStartAngle() {
        return this.mStartAngle;
    }

    public float getSweepAngle() {
        return this.mSweepAngle;
    }

    protected Drawable getThumbDrawable() {
        return this.thumbDrawable;
    }

    public int getThumbTint() {
        return this.mScrollThumbTint;
    }

    protected boolean isInverted() {
        if (getLayoutDirection() != LAYOUT_DIRECTION_RTL) {
            String language = Locale.getDefault().getLanguage();
            if (!((language.contains("ug")
                    || language.contains("ur")) | (language.contains("ar")
                    || language.contains("fa") || language.contains("iw")))) {
                return false;
            }
        }
        return true;
    }

    protected void updateThumbPosition() {
    }

    public void onScroll() {
        if (getVisibility() != View.VISIBLE || isScrolling()) {
            return;
        }
        if (!this.mCanScroll) {
            this.mCanScroll = canScroll();
        }
        if (this.mCanScroll) {
            IScrollProxy proxy = this.mOverScrollProxy;
            if (proxy != null) {
                if (proxy.isScrolling()) {
                    if (!this.isOverScroll) {
                        this.isOverScroll = true;
                        this.mThumbHeight = this.mThumbRect.height();
                    }
                } else if (this.isOverScroll) {
                    this.isOverScroll = false;
                }
            }
            updateOnTouch();
            scheduleFadeOutAnimation(1750);
            return;
        }
        if (this.isThumbShow) {
            setThumbShow(false);
            invalidate();
        }
    }

    public void scrollableViewTouchEvent(View view, MotionEvent motionEvent) {
        if (this.mScrollableView == view && !isScrolling()) {
            scrollableViewTouchEvent(motionEvent);
        }
    }

    public void bindScrollableView(View view, boolean setOnTouchListener) {
        if (this.mScrollableView != null) {
            return;
        }
        this.mScrollableView = view;
        if (setOnTouchListener) {
            view.setOnTouchListener(this.onTouchListener);
        }
        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);
        if (this.mScrollableView.isAttachedToWindow()) {
            setupScrollableViewObserver();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupScrollableViewObserver();
        scheduleFadeOutAnimation(1750);
        if (this.mBandScroll == 0) {
            Log.d("ScrollbarView", "onAttachedToWindow mScrollViewId is null");
            return;
        }
        ViewParent parent = getParent();
        if (parent == null || !(parent instanceof View)) {
            return;
        }
        View view = ((View) parent).findViewById(this.mBandScroll);
        if (view == null) {
            Log.d("ScrollbarView", "bindScrollView is null");
        }
        ScrollbarHelper.bindScrollableView(view,this);
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    @Override
    protected void onDetachedFromWindow() {
        ViewTreeObserver viewTreeObserver;
        super.onDetachedFromWindow();
        removeCallbacks(mUpdateRunnable);
        WeakReference<ViewTreeObserver> weakReference = this.viewTreeObserverWeakReference;
        if (weakReference == null || this.onGlobalLayoutListener == null || (viewTreeObserver = weakReference.get()) == null || !viewTreeObserver.isAlive()) {
            return;
        }
        viewTreeObserver.removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ScrollbarRunnable runnable;
        int flag;
        if (this.isOverScrolling) {
            Drawable drawable = this.mTrackDrawable;
            if (drawable != null && this.isThumbShow && this.mThumbType == 0) {
                drawable.setBounds(this.mTrackRect);
                drawable.draw(canvas);
            }
            if (this.isThumbShow && (flag = (runnable = this.mUpdateRunnable).mFlag) != 0) {
                boolean needInvalidate = false;
                if (flag == 2) {
                    float[] fArr = runnable.mCurrentAlpha;
                    if (runnable.mInterpolator.timeToValues(fArr) == Interpolator.Result.FREEZE_END) {
                        setThumbShow(false);
                        runnable.mFlag = 0;
                    } else {
                        this.mAlpha = Math.round(fArr[0]);
                    }
                    needInvalidate = true;
                } else {
                    this.mAlpha = 255;
                }
                if (this.mThumbType == 1) {
                    updateThumbPosition();
                } else {
                    Drawable drawable2 = this.thumbDrawable;
                    if (drawable2 != null) {
                        drawable2.setBounds(this.mThumbRect);
                        drawable2.mutate().setAlpha(this.mAlpha);
                        drawable2.draw(canvas);
                    }
                }
                if (needInvalidate) {
                    postInvalidateDelayed(50L);
                }
            }
        }
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        if (hovered) {
            updateOnTouch();
        } else {
            scheduleFadeOutAnimation(1750);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            int mTrackWidth1 = this.mTrackWidth;
            int width = getWidth() - getPaddingRight();
            int i7 = width - mTrackWidth1;
            if (isInverted()) {
                i7 = getPaddingLeft();
                width = i7 + mTrackWidth1;
            }
            this.mContentHeight = (getHeight() - getPaddingTop()) - getPaddingBottom();
            int paddingTop = getPaddingTop();
            this.mTrackRect.set(i7, paddingTop, width, this.mContentHeight + paddingTop);
            updateScrollBarPositionAndLength();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            mode = MeasureSpec.UNSPECIFIED;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(size, mode), heightMeasureSpec);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        Drawable drawable = this.thumbDrawable;
        if (drawable != null) {
            drawable.setAutoMirrored(true);
            DrawableCompat.setLayoutDirection(this.thumbDrawable, layoutDirection);
        }
        Drawable drawable2 = this.mTrackDrawable;
        if (drawable2 != null) {
            drawable2.setAutoMirrored(true);
            DrawableCompat.setLayoutDirection(this.mTrackDrawable, layoutDirection);
        }
        super.onRtlPropertiesChanged(layoutDirection);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }
        ScrollbarRunnable cVar = this.mUpdateRunnable;
        if (cVar != null && cVar.mFlag == 0) {
            return false;
        }
        if (this.mScrollableViewState != 0) {
            return false;
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (this.mFastScrollable && this.mFastScrollRegion.contains(x, y)) {
                    this.mState = 1;
                    this.mLastY = y;
                    this.isOverScroll = false;
                    setPressed(true);
                    updateOnTouch();
//                    int i7 = this.mVibrateDelayTime;
//                    if (i7 > 0) {
//                        postDelayed(new HapticFeedbackRunnable(this), i7);
//                    } else if (isHapticFeedbackEnabled()) {
//                        VibrateUtil.c(this, 9, 0);
//                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = y - this.mLastY;
                if (this.mState == 1 && Math.abs(deltaY) >= this.mScaledTouchSlop) {
                    this.mState = 2;
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    int touchSlop = this.mScaledTouchSlop;
                    if (deltaY > 0) {
                        touchSlop = -touchSlop;
                    }
                    deltaY += touchSlop;
                }
                if (this.mState == 2) {
                    this.mLastY = y;
                    if (deltaY != 0) {
                        int newScroll = this.scrollBarLeft + deltaY;
                        int maxScroll = this.mContentHeight - this.scrollBarTop;
                        if (newScroll < 0) {
                            newScroll = 0;
                        }
                        if (newScroll <= maxScroll) {
                            maxScroll = newScroll;
                        }
                        this.scrollBarLeft = maxScroll;
                        if (this.onFastScrollListener != null) {
                            int scrollRange = getScrollableViewVerticalScrollRange();
                            int scrollExtent = getScrollableViewVerticalScrollExtent();
                            if (scrollRange != -1 && scrollExtent != -1) {
                                int scrollableViewVerticalScrollOffset = getScrollableViewVerticalScrollOffset();
                                float scrollProgress = getScrollProgress();
                                int scrollDelta = ((int) ((scrollRange - scrollExtent) * scrollProgress)) - scrollableViewVerticalScrollOffset;
                                int compare = Float.compare(scrollProgress - this.mScrollProgress, 0.0f);
                                if (compare > 0 && scrollDelta < 0) {
                                    scrollDelta = 0;
                                }
                                if (compare < 0 && scrollDelta > 0) {
                                    scrollDelta = 0;
                                }
                                if (scrollDelta != 0) {
                                    this.onFastScrollListener.onScroll(0, scrollDelta, scrollProgress);
                                }
                                this.mScrollProgress = scrollProgress;
                            }
                        }
                        updateScrollBarPosition();
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                this.mState = 0;
                updateScrollBarPositionAndLength();
                invalidate();
                scheduleFadeOutAnimation(1750);
                break;
        }
        return this.mState != 0;
    }

    public void setFadeTime(int fadeTime) {
        this.mFadeTime = fadeTime;
    }

    public void setFastScrollable(boolean fastScrollable) {
        this.mFastScrollable = fastScrollable;
    }

    public void setOverScrollProxy(IScrollProxy proxy) {
        this.mOverScrollProxy = proxy;
    }

    public void setOnFastScrollListener(OnFastScrollListener listener) {
        this.onFastScrollListener = listener;
    }

    protected void setThumbDrawable(Drawable drawable) {
        Drawable drawable2 = this.thumbDrawable;
        if (drawable2 != null) {
            drawable2.setCallback(null);
        }
        this.thumbDrawable = drawable;
        if (drawable != null) {
            int i2 = this.mScrollThumbTint;
            if (i2 != Color.WHITE) {
                DrawableCompat.setTint(drawable, i2);
            }
            this.thumbDrawable.setCallback(this);
        }
    }

    public void setThumbTint(int thumbTint) {
        this.mScrollThumbTint = thumbTint;
    }

    protected void setTrackDrawable(Drawable drawable) {
        Drawable drawable2 = this.mTrackDrawable;
        if (drawable2 != null) {
            drawable2.setCallback(null);
        }
        this.mTrackDrawable = drawable;
        if (drawable != null) {
            int i2 = this.mScrollTrackTint;
            if (i2 != 16777215) {
                DrawableCompat.setTint(drawable, i2);
            }
            this.mTrackDrawable.setCallback(this);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.thumbDrawable || drawable == this.mTrackDrawable || super.verifyDrawable(drawable);
    }

    public void stopScroll() {
        removeCallbacks(mUpdateRunnable);
    }
}
