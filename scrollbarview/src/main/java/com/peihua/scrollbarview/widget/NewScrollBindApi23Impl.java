package com.peihua.scrollbarview.widget;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(api = 23)
public class NewScrollBindApi23Impl extends NewScrollBindBaseImpl {

    private void onBind(View view, NewScrollbarView scrollbarView, boolean z) {
        if (view == null || scrollbarView == null) {
            return;
        }
        scrollbarView.setScrollableView(view, z);
        view.setOnScrollChangeListener(scrollbarView::onScroll);
    }

    @Override
    public void onBindView(AbsListView absListView, NewScrollbarView scrollbarView, boolean setOnTouchListener) {
        onBind(absListView, scrollbarView, setOnTouchListener);
    }

    @Override
    public void onBindView(RecyclerView recyclerView, NewScrollbarView scrollbarView, boolean setOnTouchListener) {
        onBind(recyclerView, scrollbarView, setOnTouchListener);
    }

    @Override
    public void onBindView(ScrollView scrollView, NewScrollbarView scrollbarView, boolean setOnTouchListener) {
        onBind(scrollView, scrollbarView, setOnTouchListener);
    }

    private void onBindView(View var1, final NewScrollbarView var2, boolean var3) {
        if (var1 != null && var2 != null) {
            var2.setScrollableView(var1, var3);
            var1.setOnScrollChangeListener(var2::onScroll);
        }
    }
}
