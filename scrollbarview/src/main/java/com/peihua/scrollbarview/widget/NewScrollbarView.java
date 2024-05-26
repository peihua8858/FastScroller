package com.peihua.scrollbarview.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Rect;
import android.graphics.Interpolator.Result;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;

import androidx.core.graphics.drawable.DrawableCompat;

import com.peihua.scrollbarview.R;
import com.peihua.scrollbarview.utils.WidgetCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class NewScrollbarView extends View {
    private static final String a = "ScrollbarView";
    private static final float DEFAULT_SCROLLBAR_THUMB_RATIO = 0.5F;
    private static final int c = 16777215;
    private static final int d = 1750;
    private static final int e = 1750;
    private static final int f = 250;
    private static final int g = 0;
    private static final int h = 1;
    private static final int i = 2;
    private static final int j = 48;
    private static final int k = 16;
    private static final int l = 255;
    private static final int m = 192;
    private static final int n = 48;
    private static final INewScrollBind scrollBindImpl;
    private boolean isFadingOut;
    private int mMinThumbHeight;
    private int mMinThumbWidth;
    private Rect mThumbBounds;
    private Rect mTrackBounds;
    private int mState;
    private int mLastY;
    private int mStartY;
    private int mScaledTouchSlop;
    private Drawable mScrollThumb;
    private Drawable mScrollTrack;
    private int mScrollThumbTint;
    private int mScrollTrackTint;
    private boolean mDraggingEnabled;
    private int mScrollableViewState;
    private View mScrollableView;
    private OnTouchOffsetListener onTouchOffsetListener;
    private boolean isThumbShow;
    private Handler mHandler;
    private ScrollbarRunnable scrollbarRunnable;
    private View.OnTouchListener onTouchListener;

    static {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollBindImpl = new NewScrollBindApi23Impl();
        } else {
            scrollBindImpl = new NewScrollBindBaseImpl();
        }
    }

    public NewScrollbarView(Context context) {
        this(context, null);
    }

    public NewScrollbarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.scrollbarViewStyle);
    }

    public NewScrollbarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(wrapContext(context, defStyleAttr), attrs, defStyleAttr);
        this.isFadingOut = true;
        this.mThumbBounds = new Rect();
        this.mTrackBounds = new Rect();
        this.mState = 0;
        this.mDraggingEnabled = true;
        this.mScrollableViewState = 0;
        this.isThumbShow = true;
        this.mHandler = new Handler();
        this.onTouchListener = (view, event) -> {
            if (!this.isDragging()) {
                this.handleScrollableViewTouchEvent(event);
            }
            return false;
        };
        this.initAttrs(super.getContext(), attrs, defStyleAttr);
    }


    private static Context wrapContext(Context context, int defStyleAttr) {
        return WidgetCompat.wrapContext(context, defStyleAttr, R.style.ThemeNewScrollbarView);
    }

    private void initAttrs(Context context, AttributeSet attributeSet, int var3) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.NewScrollbarView, var3, R.style.WidgetNewScrollbarView);
        this.mScaledTouchSlop = configuration.getScaledTouchSlop();
        this.mScrollThumb = array.getDrawable(R.styleable.NewScrollbarView_scrollThumb);
        this.mScrollTrack = array.getDrawable(R.styleable.NewScrollbarView_scrollTrack);
        this.mMinThumbHeight = array.getDimensionPixelSize(R.styleable.NewScrollbarView_minThumbHeight, 48);
        this.mMinThumbWidth = array.getDimensionPixelSize(R.styleable.NewScrollbarView_minThumbWidth, 16);
        this.mScrollThumbTint = array.getColor(R.styleable.NewScrollbarView_scrollThumbTint, 16777215);
        this.mScrollTrackTint = array.getColor(R.styleable.NewScrollbarView_scrollTrackTint, 16777215);
        array.recycle();
        this.setHorizontalScrollBarEnabled(false);
        this.setVerticalScrollBarEnabled(false);
        this.scrollbarRunnable = new ScrollbarRunnable();
        this.scrollbarRunnable.scrollbarView = this;
        if (this.mScrollThumb != null) {
            this.mScrollThumb = DrawableCompat.wrap(this.mScrollThumb);
            if (this.mScrollThumbTint != 16777215) {
                DrawableCompat.setTint(this.mScrollThumb, this.mScrollThumbTint);
            }
        }

        if (this.mScrollTrack != null) {
            this.mScrollTrack = DrawableCompat.wrap(this.mScrollTrack);
            if (this.mScrollTrackTint != 16777215) {
                DrawableCompat.setTint(this.mScrollTrack, this.mScrollTrackTint);
            }
        }

        if (this.isInEditMode()) {
            this.mThumbBounds = new Rect(0, 0, 48, 192);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            mode = MeasureSpec.UNSPECIFIED;
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(size, mode), heightMeasureSpec);
    }

    public static INewScrollBind getScrollBindImpl() {
        return scrollBindImpl;
    }

    protected void onScroll(View view, int var2, int var3, int var4, int var5) {
        if (!this.isDragging()) {
            if (!this.isScrollableViewDragging()) {
                if (!this.isFadingOut) {
                    this.reset();
                } else {
                    this.isFadingOut = false;
                }
            }

            this.updateScrollbar();
        }
    }

    public void setOnTouchOffsetListener(OnTouchOffsetListener var1) {
        this.onTouchOffsetListener = var1;
    }

    public void setScrollableView(View view, boolean flag) {
        if (view == null) {
            Log.w("HwScrollbarView", "setScrollableView: view is null");
        } else if (this.mScrollableView == null) {
            this.mScrollableView = view;
            if (flag) {
                view.setOnTouchListener(this.onTouchListener);
            }

            view.setVerticalScrollBarEnabled(false);
        }
    }

    private boolean isScrollableViewDragging() {
        return this.mScrollableViewState != 0;
    }

    public View getScrollableView() {
        return this.mScrollableView;
    }

    private void updateScrollbar() {
        this.updateThumbBounds();
        this.postInvalidate();
    }

    private void updateThumbBounds() {
        if (this.mScrollableView != null) {
            int scrollRange = this.getScrollViewViewVerticalScrollRange();
            int scrollExtent = this.getScrollableViewVerticalScrollExtent();
            int scrollOffset = this.getScrollableViewVerticalScrollOffset();
            if (scrollRange > scrollExtent) {
                this.calculateThumbBounds(scrollRange, scrollExtent, scrollOffset);
            } else {
                this.mThumbBounds.setEmpty();
            }

        }
    }
//
    private void calculateThumbBounds(int totalScrollRange, int visibleScrollExtent, int currentScrollOffset) {
        if (totalScrollRange >= visibleScrollExtent) {
            int thumbHeight = this.mTrackBounds.height() * visibleScrollExtent / totalScrollRange;
            if (thumbHeight < this.mMinThumbHeight) {
                thumbHeight = this.mMinThumbHeight;
            }

            int thumbWidth = 0;
            if (this.mScrollThumb != null) {
                thumbWidth = this.mScrollThumb.getIntrinsicWidth();
            }

            if (thumbWidth < this.mMinThumbWidth) {
                thumbWidth = this.mMinThumbWidth;
            }

            int thumbMargin = this.mTrackBounds.height() - thumbHeight;
            int thumbOffset = (int) (1.0F * (float) thumbMargin / (float) (totalScrollRange - visibleScrollExtent) * (float) currentScrollOffset);
            if (thumbOffset > thumbMargin) {
                thumbOffset = thumbMargin;
            }

            int thumbLeft = this.getPaddingTop();
            int thumbTop = this.getPaddingTop() + thumbHeight;
            int thumbRight = this.getWidth() - this.getPaddingRight();
            int thumbBottom = thumbRight - thumbWidth;
            if (this.isRtl()) {
                thumbBottom = this.getPaddingLeft();
                thumbRight = thumbBottom + thumbWidth;
            }

            this.mThumbBounds.set(thumbBottom, thumbLeft, thumbRight, thumbTop);
            this.mThumbBounds.offset(0, thumbOffset);
        }
    }

    private boolean isRtl() {
        return this.getLayoutDirection() == LAYOUT_DIRECTION_RTL || this.e();
    }

    private boolean e() {
        String var1 = Locale.getDefault().getLanguage();
        boolean var2 = var1.contains("ar") || var1.contains("fa") || var1.contains("iw");
        var2 |= var1.contains("ug") || var1.contains("ur");
        return var2;
    }

    public void onRtlPropertiesChanged(int var1) {
        this.a(var1);
        super.onRtlPropertiesChanged(var1);
    }

    private void a(int var1) {
        if (this.mScrollThumb != null) {
            DrawableCompat.setLayoutDirection(this.mScrollThumb, var1);
        }

        if (this.mScrollTrack != null) {
            DrawableCompat.setLayoutDirection(this.mScrollTrack, var1);
        }

    }

    private int getScrollableViewVerticalScrollExtent() {
        int var1 = -1;
        Class var2 = View.class;
        Method var3 = null;

        try {
            var3 = var2.getDeclaredMethod("computeVerticalScrollExtent");
        } catch (NoSuchMethodException var7) {
            Log.w("HwScrollbarView", "NoSuchMethodException computeVerticalScrollExtent");
        }

        if (var3 != null) {
            try {
                var3.setAccessible(true);
                Object var4 = var3.invoke(this.mScrollableView);
                if (var4 != null && var4 instanceof Integer) {
                    var1 = (Integer) var4;
                }
            } catch (IllegalAccessException var5) {
                Log.w("HwScrollbarView", "IllegalAccessException computeVerticalScrollExtent");
            } catch (InvocationTargetException var6) {
                Log.w("HwScrollbarView", "InvocationTargetException computeVerticalScrollExtent");
            }
        }

        return var1;
    }

    private int getScrollableViewVerticalScrollOffset() {
        int var1 = -1;
        Class var2 = View.class;
        Method var3 = null;

        try {
            var3 = var2.getDeclaredMethod("computeVerticalScrollOffset");
        } catch (NoSuchMethodException var7) {
            Log.w("HwScrollbarView", "NoSuchMethodException computeVerticalScrollOffset");
        }

        if (var3 != null) {
            try {
                var3.setAccessible(true);
                Object var4 = var3.invoke(this.mScrollableView);
                if (var4 != null && var4 instanceof Integer) {
                    var1 = (Integer) var4;
                }
            } catch (IllegalAccessException var5) {
                Log.w("HwScrollbarView", "IllegalAccessException computeVerticalScrollOffset");
            } catch (InvocationTargetException var6) {
                Log.w("HwScrollbarView", "InvocationTargetException computeVerticalScrollOffset");
            }
        }

        return var1;
    }

    private int getScrollViewViewVerticalScrollRange() {
        int var1 = -1;
        Class var2 = View.class;
        Method var3 = null;

        try {
            var3 = var2.getDeclaredMethod("computeVerticalScrollRange");
        } catch (NoSuchMethodException var7) {
            Log.w("HwScrollbarView", "NoSuchMethodException computeVerticalScrollRange");
        }

        if (var3 != null) {
            try {
                var3.setAccessible(true);
                Object var4 = var3.invoke(this.mScrollableView);
                if (var4 != null && var4 instanceof Integer) {
                    var1 = (Integer) var4;
                }
            } catch (IllegalAccessException var5) {
                Log.w("HwScrollbarView", "IllegalAccessException computeVerticalScrollRange");
            } catch (InvocationTargetException var6) {
                Log.w("HwScrollbarView", "InvocationTargetException computeVerticalScrollRange");
            }
        }

        return var1;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.scheduleScrollbarFadeout(1750);
    }

    protected void onLayout(boolean changed, int left, int var3, int right, int bottom) {
        if (changed) {
            this.f();
        }

        super.onLayout(changed, left, var3, right, bottom);
    }

    private void f() {
        this.g();
        this.updateThumbBounds();
    }

    private void g() {
        this.mTrackBounds.set(this.getPaddingLeft(), this.getPaddingTop(), this.getWidth() - this.getPaddingRight(), this.getHeight() - this.getPaddingBottom());
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.h();
    }

    private void h() {
        int[] state = this.getDrawableState();
        boolean var2 = false;
        Drawable var3 = this.mScrollThumb;
        if (var3 != null && var3.isStateful()) {
            var2 |= var3.setState(state);
        }

        Drawable var4 = this.mScrollTrack;
        if (var4 != null && var4.isStateful()) {
            var2 |= var4.setState(state);
        }

        if (var2) {
            this.invalidate();
        }

    }

    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mScrollThumb || drawable == this.mScrollTrack || super.verifyDrawable(drawable);
    }

    private boolean isDragging() {
        return this.mState != 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isScrollbarDisabled()) {
            return false;
        } else if (event == null) {
            Log.w("ScrollbarView", "onTouchEvent: event is null");
            return false;
        } else {
            int action = event.getAction();
            int eventX = (int) event.getX();
            int eventY = (int) event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (this.isTouchInsideScrollbar(eventX, eventY)) {
                        this.mState = 1;
                        this.mLastY = eventY;
                        this.setPressed(true);
                        this.handleSwipe();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    this.setPressed(false);
                    this.mState = 0;
                    this.reset();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int deltaY = eventY - this.mLastY;
                    if (this.mState == 1 && Math.abs(deltaY) >= this.mScaledTouchSlop) {
                        this.mState = 2;
                        this.mLastY = eventY;
                        ViewParent parent = this.getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }

                    if (this.mState == 2) {
                        this.mLastY = eventY;
                        this.scrollThumb(deltaY);
                    }
            }

            return this.mState != 0;
        }
    }

    private boolean isTouchInsideScrollbar(int x, int y) {
        if (!this.mDraggingEnabled) {
            return false;
        } else {
            Rect rect = new Rect(this.mThumbBounds);
            rect.left = 0;
            rect.right = this.getWidth();
            return rect.contains(x, y);
        }
    }

    public void setDraggingEnabled(boolean mDraggingEnabled) {
        this.mDraggingEnabled = mDraggingEnabled;
    }

    private void scrollThumb(int deltaY) {
        int paddingTop = this.getPaddingTop();
        int contentHeight = this.getHeight() - this.getPaddingBottom() - this.mThumbBounds.height();
        int newTop = this.mThumbBounds.top + deltaY;
        int delta = deltaY;
        if (newTop < paddingTop) {
            delta = paddingTop - this.mThumbBounds.top;
        } else if (newTop > contentHeight) {
            delta = contentHeight - this.mThumbBounds.top;
        } else {
            Log.i("ScrollbarView", "thumbScrollby: do nothing.");
        }
        if (delta != 0) {
            this.mThumbBounds.offset(0, delta);
            this.handleTouchOffset(deltaY > 0);
            this.postInvalidate();
        }
    }

    private void handleTouchOffset(boolean isScrollingDown) {
        int totalOffset = this.mTrackBounds.height() - this.mThumbBounds.height();
        int currentOffset = this.mThumbBounds.top - this.mTrackBounds.top;
        if (this.onTouchOffsetListener != null) {
            this.onTouchOffsetListener.onTouchOffset(totalOffset, currentOffset, isScrollingDown);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void drawTrack(Canvas canvas) {
        if (this.mScrollTrack != null && this.l()) {
            Rect trackBounds = this.mTrackBounds;
            if (trackBounds != null) {
                this.mScrollTrack.setBounds(trackBounds);
                boolean isRtl = this.isRtl();
                if (isRtl) {
                    canvas.save();
                    float centerX = (float) (trackBounds.left + trackBounds.right) * 0.5F;
                    float centerY = (float) (trackBounds.top + trackBounds.bottom) * 0.5F;
                    canvas.scale(-1.0F, 1.0F, centerX, centerY);
                }

                this.mScrollTrack.draw(canvas);
                if (isRtl) {
                    canvas.restore();
                }

            }
        }
    }

    private void drawThumb(Canvas canvas) {
        Drawable scrollThumb = this.mScrollThumb;
        Rect thumbBounds = this.mThumbBounds;
        if (scrollThumb != null && thumbBounds != null) {
            ScrollbarRunnable runnable = this.scrollbarRunnable;
            int flag = this.scrollbarRunnable.mFlag;
            if (flag != 0) {
                boolean invalidate = false;
                if (flag == 2) {
                    float[] currentAlpha = runnable.mCurrentAlpha;
                    if (runnable.mInterpolator.timeToValues(currentAlpha) == Result.FREEZE_END) {
                        this.j();
                        runnable.mFlag = 0;
                    } else {
                        scrollThumb.mutate().setAlpha(Math.round(currentAlpha[0]));
                    }

                    invalidate = true;
                } else {
                    scrollThumb.mutate().setAlpha(255);
                }

                scrollThumb.setBounds(thumbBounds);
                boolean isRtl = this.isRtl();
                if (isRtl) {
                    canvas.save();
                    float centerX = (float) (thumbBounds.left + thumbBounds.right) * 0.5F;
                    float centerY = (float) (thumbBounds.top + thumbBounds.bottom) * 0.5F;
                    canvas.scale(-1.0F, 1.0F, centerX, centerY);
                }

                scrollThumb.draw(canvas);
                if (isRtl) {
                    canvas.restore();
                }

                if (invalidate) {
                    this.invalidate();
                }

            }
        }
    }

    protected void onDraw(Canvas canvas) {
        this.drawTrack(canvas);
        this.drawThumb(canvas);
        super.onDraw(canvas);
    }

    private void j() {
        if (this.isThumbShow) {
            this.isThumbShow = false;
        }

    }

    private void handleSwipe() {
        this.mHandler.removeCallbacks(this.scrollbarRunnable);
        if (this.scrollbarRunnable != null) {
            this.scrollbarRunnable.mFlag = 1;
        }

        if (!this.isThumbShow) {
            this.isThumbShow = true;
            this.postInvalidate();
        }

    }

    private boolean l() {
        return this.isThumbShow;
    }

    private boolean isScrollbarDisabled() {
        if (this.scrollbarRunnable != null) {
            return this.scrollbarRunnable.mFlag == 0;
        } else {
            return false;
        }
    }


    private void scheduleScrollbarFadeout(int delayMillis) {
        if (this.l()) {
            long scheduledTime = AnimationUtils.currentAnimationTimeMillis() + 250L;
            this.scrollbarRunnable.mScheduledTime = scheduledTime;
            this.scrollbarRunnable.mFlag = 1;
            this.mHandler.removeCallbacks(this.scrollbarRunnable);
            this.mHandler.postDelayed(this.scrollbarRunnable, delayMillis);
        }

    }

    private void reset() {
        this.scheduleScrollbarFadeout(1750);
    }

    public void onScrollableViewTouchEvent(View var1, MotionEvent var2) {
        if (this.mScrollableView == var1) {
            if (!this.isDragging()) {
                this.handleScrollableViewTouchEvent(var2);
            }
        }
    }

    private void handleScrollableViewTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int eventY = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.mStartY = eventY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.mScrollableViewState = 0;
                this.reset();
                break;
            case MotionEvent.ACTION_MOVE:
                if (this.mStartY != eventY) {
                    this.mScrollableViewState = 2;
                    this.handleSwipe();
                }
        }
    }

    private static class ScrollbarRunnable implements Runnable {
        public static final int a = 0;
        public static final int b = 1;
        public static final int c = 2;
        private static final float[] START_ALPHA = new float[]{255.0F};
        private static final float[] END_ALPHA = new float[]{0.0F};
        private NewScrollbarView scrollbarView;
        private float[] mCurrentAlpha;
        private final Interpolator mInterpolator;
        private long mScheduledTime;
        private int mFlag;

        private ScrollbarRunnable() {
            this.mCurrentAlpha = new float[1];
            this.mInterpolator = new Interpolator(1, 2);
            this.mFlag = 0;
        }

        public void run() {
            long timeMillis = AnimationUtils.currentAnimationTimeMillis();
            if (timeMillis >= this.mScheduledTime) {
                int var3 = (int) timeMillis;
                int var4 = 0;
                Interpolator var5 = this.mInterpolator;
                var5.setKeyFrame(var4++, var3, START_ALPHA);
                var3 += 250;
                var5.setKeyFrame(var4, var3, END_ALPHA);
                this.mFlag = 2;
                this.scrollbarView.invalidate();
            }

        }
    }

    public interface OnTouchOffsetListener {
        void onTouchOffset(int var1, int var2, boolean var3);
    }
}
