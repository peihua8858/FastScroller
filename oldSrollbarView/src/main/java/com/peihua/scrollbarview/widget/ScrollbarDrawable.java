package com.peihua.scrollbarview.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.peihua.scrollbarview.R;
import com.peihua.scrollbarview.interpolator.CubicBezierInterpolator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ScrollbarDrawable extends Drawable {

    private int mScrollbarWidth;

    private int mScrollbarMaxWidth;
    private Animator mAnimator;
    private Interpolator mScrollbarInterpolator;
    private int mScrollbarEndMargin = 0;

    private Paint mPaint = new Paint(1);

    private RectF mRectF = new RectF();

    private int mScrollbarNormalWidth = 8;

    private int mScrollbarActivatedWidth = 8;

    private boolean mAutoMirrored = false;

    private int mTint = 0;

    private int mScrollbarColor = 0;

    private int mAlpha = 255;
    private boolean isWidthChanged = false;
    private long mScrollbarToActivatedDuration = 150;
    private long mScrollbarToUnactivatedDuration = 150;

    public ScrollbarDrawable() {
        this.mPaint.setStyle(Paint.Style.FILL);
    }

    private void init(Context context, TypedArray typedArray) {
        int dimensionPixelOffset = typedArray.getDimensionPixelOffset(R.styleable.ScrollbarDrawable_scrollbarNormalWidth, 8);
        this.mScrollbarNormalWidth = dimensionPixelOffset;
        setScrollbarWidth(dimensionPixelOffset);
        this.mScrollbarActivatedWidth = typedArray.getDimensionPixelOffset(R.styleable.ScrollbarDrawable_scrollbarActivatedWidth, 8);
        this.mScrollbarEndMargin = typedArray.getDimensionPixelOffset(R.styleable.ScrollbarDrawable_scrollbarEndMargin, 8);
        this.mScrollbarColor = typedArray.getColor(R.styleable.ScrollbarDrawable_scrollbarColor, 0);
        int resourceId = typedArray.getResourceId(R.styleable.ScrollbarDrawable_scrollbarInterpolator, android.R.anim.linear_interpolator);
        if (resourceId > 0 && context != null) {
            this.mScrollbarInterpolator = AnimationUtils.loadInterpolator(context, resourceId);
        } else {
            this.mScrollbarInterpolator = new CubicBezierInterpolator(0.2f, 0.5f, 0.8f, 0.5f);
        }
        this.mScrollbarToActivatedDuration = typedArray.getInt(R.styleable.ScrollbarDrawable_scrollbarToActivatedDuration, 150);
        this.mScrollbarToUnactivatedDuration = typedArray.getInt(R.styleable.ScrollbarDrawable_scrollbarToUnactivatedDuration, 150);
    }

    public void initScrollbarDrawable(Context context, AttributeSet attributeSet, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ScrollbarDrawable, defStyleAttr, R.style.ScrollbarDrawableStyle);
        init(context, typedArray);
        typedArray.recycle();
    }

    @Override
    public void draw(Canvas canvas) {
        int scrollbarColor = this.mScrollbarColor;
        int tint = this.mTint;
        if (tint != 0) {
            scrollbarColor = tint;
        }
        int alpha = this.mAlpha;
        this.mPaint.setColor(((scrollbarColor << 8) >>> 8) | ((((scrollbarColor >>> 24) * (alpha + (alpha >> 7))) >> 8) << 24));
        RectF rectF = this.mRectF;
        Rect bounds = getBounds();
        if (isAutoMirrored() && getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            float scrollbarEndMargin = bounds.left + this.mScrollbarEndMargin;
            rectF.left = scrollbarEndMargin;
            rectF.right = scrollbarEndMargin + this.mScrollbarWidth;
        } else {
            float scrollbarEndMargin = bounds.right - this.mScrollbarEndMargin;
            rectF.right = scrollbarEndMargin;
            rectF.left = scrollbarEndMargin - this.mScrollbarWidth;
        }
        rectF.top = bounds.top;
        rectF.bottom = bounds.bottom;
        float radius = rectF.width() <= 0.0f ? 0.0f : rectF.width() * 0.5f;
        canvas.drawRoundRect(rectF, radius, radius, this.mPaint);
    }

    @Override
    public int getOpacity() {
        return (this.mTint >>> 24) == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
    }

    @Override
    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) throws IOException, XmlPullParserException {
        super.inflate(resources, xmlPullParser, attributeSet, theme);
        int[] iArr = R.styleable.ScrollbarDrawable;
        TypedArray obtainAttributes = theme == null ? resources.obtainAttributes(attributeSet, iArr) : theme.obtainStyledAttributes(attributeSet, iArr, 0, 0);
        init(null, obtainAttributes);
        obtainAttributes.recycle();
    }

    @Override
    public boolean isAutoMirrored() {
        return this.mAutoMirrored;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean activated = false;
        boolean widthChanged;
        boolean needsAnimating;
        if (state != null) {
            for (int stateId : state) {
                if (stateId == android.R.attr.state_pressed/*16842919*/) {
                    activated = true;
                    break;
                }
            }
        }
        if (activated) {
            this.mScrollbarMaxWidth = this.mScrollbarActivatedWidth;
            widthChanged = true;
        } else {
            this.mScrollbarMaxWidth = this.mScrollbarNormalWidth;
            widthChanged = false;
        }
        if (this.isWidthChanged != widthChanged) {
            this.isWidthChanged = widthChanged;
            needsAnimating = true;
        } else {
            needsAnimating = false;
        }
        if (!needsAnimating) {
            return false;
        }
        Animator animator = this.mAnimator;
        if (animator != null && animator.isRunning()) {
            this.mAnimator.cancel();
        }
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "scrollbarWidth", this.mScrollbarWidth, this.mScrollbarMaxWidth);
        Interpolator interpolator = this.mScrollbarInterpolator;
        if (interpolator != null) {
            ofInt.setInterpolator(interpolator);
        }
        ofInt.setDuration(widthChanged ? this.mScrollbarToActivatedDuration : this.mScrollbarToUnactivatedDuration);
        this.mAnimator = ofInt;
        ofInt.start();
        return true;
    }

    @Override
    public void setAlpha(int alpha) {
        if (this.mAlpha != alpha) {
            this.mAlpha = alpha;
        }
    }

    @Override
    public void setAutoMirrored(boolean autoMirrored) {
        this.mAutoMirrored = autoMirrored;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    public void setScrollbarWidth(int scrollbarWidth) {
        if (this.mScrollbarWidth != scrollbarWidth) {
            this.mScrollbarWidth = scrollbarWidth;
            invalidateSelf();
        }
    }

    @Override
    public void setTint(int tint) {
        this.mTint = tint;
        invalidateSelf();
    }
}
