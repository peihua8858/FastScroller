package com.peihua.fastscroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.peihua.scrollbarview.widget.NewScrollbarHelper;
import com.peihua.scrollbarview.widget.NewScrollbarView;


public class CustomScrollbarView extends NewScrollbarView {

    private int mBandScroll;

    public CustomScrollbarView(Context context) {
        this(context, null);
    }

    public CustomScrollbarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomScrollbarView(Context context, AttributeSet attributeSet, int defStyleAttr) {
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
        layoutParams.width = -2;
        layoutParams.gravity = GravityCompat.END;
        view2.setLayoutParams(layoutParams);
    }

    public static void bindScrollableView(Context context, View view, ViewGroup viewGroup) {
        if (context == null || view == null) {
            Log.d("CustomScrollbarView", "params null");
            return;
        }
        NewScrollbarView scrollbarView = new NewScrollbarView(context);
        setLayoutParams(view, scrollbarView);
        bindScrollableView(view,scrollbarView);
        if (viewGroup != null) {
            viewGroup.addView(scrollbarView);
            return;
        }
        ViewParent parent = view.getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).addView(scrollbarView);
        }
    }
//    protected void drawableStateChanged() {
//        super.drawableStateChanged();
//        if (getVisibility() == View.VISIBLE) {
//            return;
//        }
//        setVisibility(View.VISIBLE);
//    }

    @Override
    protected void onScroll(View view, int var2, int var3, int var4, int var5) {
        super.onScroll(view, var2, var3, var4, var5);
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        setVisibility(View.VISIBLE);
    }

//    @Override
//    public void onScroll() {
//        super.onScroll();
//        if (getVisibility() == View.VISIBLE) {
//            return;
//        }
//        setVisibility(View.VISIBLE);
//    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("CustomScrollbarView", "onAttachedToWindow mScrollViewId is mBandScroll:"+mBandScroll);
        if (this.mBandScroll == 0) {
            Log.d("CustomScrollbarView", "onAttachedToWindow mScrollViewId is null");
            return;
        }
        ViewParent parent = getParent();
        if (parent == null || !(parent instanceof View)) {
            Log.d("CustomScrollbarView", "parent is null");
            return;
        }
        View view = ((View) parent).findViewById(this.mBandScroll);
        if (view == null) {
            Log.d("CustomScrollbarView", "bindScrollView is null");
        }
        bindScrollableView(view,this);
    }
    public static void bindScrollableView(View view, NewScrollbarView scrollbarView) {
        if (view == null) {
            Log.d("CustomScrollbarView", "bindScrollView is null");
        }
        if (view instanceof AbsListView) {
            NewScrollbarHelper.bindListView((AbsListView) view, scrollbarView);
        } else if (view instanceof RecyclerView) {
            NewScrollbarHelper.bindRecyclerView((RecyclerView) view, scrollbarView);
        } else if (view instanceof ScrollView) {
            NewScrollbarHelper.bindScrollView((ScrollView) view, scrollbarView);
        } else {
            Log.d("CustomScrollbarView", "bind view warning");
        }
    }
}
