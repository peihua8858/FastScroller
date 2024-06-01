package com.peihua.scrollbarview.widget;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public interface IScrollBind {

    void onBindView(AbsListView absListView, ScrollbarView scrollbarView, boolean z);

    void onBindView(RecyclerView recyclerView, ScrollbarView scrollbarView, boolean z);

    void onBindView(ScrollView scrollView, ScrollbarView scrollbarView, boolean z);
    void onBindView(NestedScrollView scrollView, ScrollbarView scrollbarView, boolean z);


    void onScrollableViewTouchEvent(View var1, ScrollbarView var2, MotionEvent var3);
}
