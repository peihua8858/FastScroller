package com.peihua.scrollbarview.widget;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;


public class NewScrollBindBaseImpl implements INewScrollBind {


    @Override
    public void onBindView(AbsListView absListView, NewScrollbarView scrollbarView, boolean z) {
        if (absListView == null || scrollbarView == null) {
            return;
        }
        scrollbarView.setScrollableView(absListView, z);
        absListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                scrollbarView.onScroll(view, 0, 0, 0, 0);
            }
        });
    }

    @Override
    public void onBindView(RecyclerView recyclerView, NewScrollbarView scrollbarView, boolean z) {
        if (recyclerView == null || scrollbarView == null) {
            return;
        }
        scrollbarView.setScrollableView(recyclerView, z);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                scrollbarView.onScroll(recyclerView, 0, 0, 0, 0);
            }
        });
    }

    @Override
    public void onBindView(ScrollView scrollView, NewScrollbarView scrollbarView, boolean z) {

    }

    @Override
    public void onBindView(NestedScrollView scrollView, NewScrollbarView scrollbarView, boolean z) {
        if (scrollView == null || scrollbarView == null) {
            return;
        }
        scrollbarView.setScrollableView(scrollView, z);
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) scrollbarView::onScroll);
    }

    @Override
    public void onScrollableViewTouchEvent(View var1, NewScrollbarView var2, MotionEvent var3) {
        if (var1 != null && var2 != null && var3 != null) {
            var2.onScrollableViewTouchEvent(var1, var3);
        }
    }
}
