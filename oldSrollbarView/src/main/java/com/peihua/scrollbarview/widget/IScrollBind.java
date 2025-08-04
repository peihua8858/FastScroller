package com.peihua.scrollbarview.widget;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public interface IScrollBind {
    void onBindView(AbsListView absListView, boolean z);

    void onBindView(RecyclerView recyclerView, boolean z);

    void onBindView(ScrollView scrollView, boolean z);

    void onBindView(NestedScrollView scrollView, boolean z);


    void onScrollableViewTouchEvent(View var1, MotionEvent var3);
}
