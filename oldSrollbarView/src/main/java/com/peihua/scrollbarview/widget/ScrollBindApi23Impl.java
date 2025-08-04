//package com.peihua.scrollbarview.widget;
//
//import android.os.Build;
//import android.view.View;
//import android.widget.AbsListView;
//import android.widget.ScrollView;
//
//import androidx.annotation.RequiresApi;
//import androidx.core.widget.NestedScrollView;
//import androidx.recyclerview.widget.RecyclerView;
//
//@RequiresApi(api = Build.VERSION_CODES.M)
//public class ScrollBindApi23Impl extends ScrollBindBaseImpl {
//    private void realBindView(View view, ScrollbarView scrollbarView, boolean z) {
//        if (view == null || scrollbarView == null) {
//            return;
//        }
//        scrollbarView.bindScrollableView(view, z);
//        view.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> scrollbarView.onScroll());
//    }
//
//    @Override
//    public void onBindView(AbsListView absListView, ScrollbarView scrollbarView, boolean setOnTouchListener) {
//        realBindView(absListView, scrollbarView, setOnTouchListener);
//    }
//
//    @Override
//    public void onBindView(RecyclerView recyclerView, ScrollbarView scrollbarView, boolean setOnTouchListener) {
//        realBindView(recyclerView, scrollbarView, setOnTouchListener);
//    }
//
//    @Override
//    public void onBindView(ScrollView scrollView, ScrollbarView scrollbarView, boolean setOnTouchListener) {
//        realBindView(scrollView, scrollbarView, setOnTouchListener);
//    }
//
//    @Override
//    public void onBindView(NestedScrollView scrollView, ScrollbarView scrollbarView, boolean z) {
//        realBindView(scrollView, scrollbarView, z);
//    }
//}
