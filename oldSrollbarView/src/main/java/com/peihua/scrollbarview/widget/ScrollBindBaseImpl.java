package com.peihua.scrollbarview.widget;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;


public class ScrollBindBaseImpl implements IScrollBind {

    @Override
    public void onBindView(AbsListView absListView, ScrollbarView scrollbarView, boolean setOnTouchListener) {
        if (absListView == null || scrollbarView == null) {
            return;
        }
        scrollbarView.bindScrollableView(absListView, setOnTouchListener);
        absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                scrollbarView.onScroll();
            }
        });
    }

    @Override
    public void onBindView(RecyclerView recyclerView, ScrollbarView scrollbarView, boolean setOnTouchListener) {
        if (recyclerView == null || scrollbarView == null) {
            return;
        }
        scrollbarView.bindScrollableView(recyclerView, setOnTouchListener);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                scrollbarView.onScroll();
            }
        });
    }

    @Override
    public void onBindView(ScrollView scrollView, ScrollbarView scrollbarView, boolean setOnTouchListener) {
    }

    @Override
    public void onBindView(NestedScrollView scrollView, ScrollbarView scrollbarView, boolean z) {
        if (scrollView == null || scrollbarView == null) {
            return;
        }
        scrollbarView.bindScrollableView(scrollView, z);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> scrollbarView.onScroll());
    }

    @Override
    public void onScrollableViewTouchEvent(View view, ScrollbarView scrollbarView, MotionEvent event) {
        if (view != null && scrollbarView != null && event != null) {
            scrollbarView.scrollableViewTouchEvent(view, event);
        }
    }
}
