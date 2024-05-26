package com.peihua.scrollbarview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.core.view.GravityCompat;

import com.peihua.scrollbarview.R;

public class NewCustomScrollbarView extends NewScrollbarView {

    private int mBandScroll;

    public NewCustomScrollbarView(Context context) {
        this(context, null);
    }

    public NewCustomScrollbarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NewCustomScrollbarView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mBandScroll = 0;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CustomScrollbarView);
        this.mBandScroll = obtainStyledAttributes.getResourceId(R.styleable.CustomScrollbarView_bandScroll, 0);
        obtainStyledAttributes.recycle();
    }
    public static void setLayoutParams(View view, View view2) {
        if (view == null || view2 == null) {
            Log.d("commlib:Utils", "listView is null or scrollbar is null");
            return;
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
        layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = GravityCompat.END;
        view2.setLayoutParams(layoutParams);
    }

    public static void bindScrollableView(Context context, View view, ViewGroup viewGroup) {
        if (context == null || view == null) {
            Log.d("CustomScrollbarView", "params null");
            return;
        }
        ScrollbarView scrollbarView = new ScrollbarView(context);
        setLayoutParams(view, scrollbarView);
        OldScrollbarHelper.bindScrollableView(view,scrollbarView);
        if (viewGroup != null) {
            viewGroup.addView(scrollbarView);
            return;
        }
        ViewParent parent = view.getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).addView(scrollbarView);
        }
    }

    @Override
    protected void onScroll(View view, int var2, int var3, int var4, int var5) {
        super.onScroll(view, var2, var3, var4, var5);
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        setVisibility(View.VISIBLE);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mBandScroll == 0) {
            Log.d("CustomScrollbarView", "onAttachedToWindow mScrollViewId is null");
            return;
        }
        ViewParent parent = getParent();
        if (parent == null || !(parent instanceof View)) {
            return;
        }
        View view = ((View) parent).findViewById(this.mBandScroll);
        if (view == null) {
            Log.d("CustomScrollbarView", "bindScrollView is null");
        }
        NewScrollbarHelper.bindScrollableView(view,this);
    }
}
