package com.peihua.scrollbarview.widget;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public interface INewScrollBind {

    void onBindView(AbsListView absListView, NewScrollbarView scrollbarView, boolean z);

    void onBindView(RecyclerView recyclerView, NewScrollbarView scrollbarView, boolean z);

    void onBindView(ScrollView scrollView, NewScrollbarView scrollbarView, boolean z);
    void onBindView(NestedScrollView scrollView, NewScrollbarView scrollbarView, boolean z);


    void onScrollableViewTouchEvent(View var1, NewScrollbarView var2, MotionEvent var3);
}
