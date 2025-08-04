package com.peihua.scrollbarview.widget;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class ScrollbarHelper {

    public static class ScrollViewOverScrollProxy<T extends ViewGroup> implements IScrollProxy{
        final T scrollView;
        ScrollViewOverScrollProxy(T scrollView) {
            this.scrollView = scrollView;
        }
        private int c() {
            View childAt = this.scrollView.getChildAt(0);
            if (childAt == null) {
                return 0;
            }
            return this.scrollView.getPaddingTop() + (childAt.getHeight() - this.scrollView.getHeight()) + this.scrollView.getPaddingBottom();
        }

        @Override
        public int getScrollY() {
            int scrollY = this.scrollView.getScrollY();
            if (scrollY <= 0) {
                return scrollY;
            }
            int c2 = c();
            if (scrollY > c2) {
                return scrollY - c2;
            }
            return 0;
        }

        @Override
        public boolean isScrolling() {
            int c2 = c();
            int scrollY = this.scrollView.getScrollY();
            return scrollY > c2 || scrollY < 0;
        }
    }


    private ScrollbarHelper() {
    }

    private static void setSelectionFromTop(AbsListView absListView, int position, int y) {
        absListView.setSelectionFromTop(position, y);
    }

    private static boolean checkView(View view, ScrollbarView scrollableView) {
        return view != null && scrollableView != null && scrollableView.getScrollableView() == null;
    }


    public static int getMeasuredHeight(View view) {
        if (view == null) {
            return 0;
        }
        int measuredHeight = view.getMeasuredHeight();
        View rootView = view.getRootView();
        return rootView != null ? rootView.getMeasuredHeight() : measuredHeight;
    }


    public static void onScroll(AbsListView listView, int scrollX, int scrollY, float scrollProgress) {
        if (Float.compare(scrollProgress, 0.0f) == 0) {
            listView.setSelection(0);
            return;
        }
        Adapter adapter = listView.getAdapter();
        if (Float.compare(scrollProgress, 1.0f) != 0 || adapter == null || adapter.getCount() <= 0) {
            int maxScrollY = getMeasuredHeight(listView) - Math.abs(scrollY);
            if (Math.abs(scrollY) < maxScrollY) {
                listView.scrollListBy(scrollY);
            } else {
                scrollToPosition(listView, scrollX, scrollY, scrollProgress);
            }
        } else {
            View lastChild = listView.getChildAt(listView.getChildCount() - 1);
            if (lastChild == null) {
                return;
            }
            int height = ((listView.getHeight() - listView.getPaddingTop()) - listView.getPaddingBottom()) - lastChild.getHeight();
            int count = adapter.getCount() - 1;
            if (height >= 0) {
                listView.setSelection(count);
            } else {
                setSelectionFromTop(listView, count, height);
            }
        }
    }


    public static void onScroll(RecyclerView recyclerView, int scrollX, int scrollY, float f2) {
        int itemCount;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null || layoutManager.getItemCount() <= 0 || (itemCount = layoutManager.getItemCount() - layoutManager.getChildCount()) < 0) {
            return;
        }
        int position = (int) (f2 * itemCount);
        if (scrollY > 0 && (position = position + (layoutManager.getItemCount() - itemCount)) >= layoutManager.getItemCount()) {
            position = layoutManager.getItemCount() - 1;
        }
        recyclerView.scrollToPosition(position);
    }

    public static void bindScrollableView(View view, ScrollbarView scrollbarView) {
        if (view == null) {
            Log.d("ScrollbarHelper", "bindScrollView is null");
        }
        if (view instanceof AbsListView) {
            ScrollbarHelper.bindListView((AbsListView) view, scrollbarView);
        } else if (view instanceof RecyclerView) {
            ScrollbarHelper.bindRecyclerView((RecyclerView) view, scrollbarView);
        } else if (view instanceof ScrollView) {
            ScrollbarHelper.bindScrollView((ScrollView) view, scrollbarView);
        } else if (view instanceof NestedScrollView) {
            ScrollbarHelper.bindNestedScrollView((NestedScrollView) view, scrollbarView);
        } else {
            Log.d("ScrollbarHelper", "bind view warning");
        }
    }

    public static boolean bindListView(AbsListView absListView, ScrollbarView scrollbarView) {
        return bindListView(absListView, scrollbarView, true);
    }

    public static boolean bindListView(AbsListView absListView, ScrollbarView scrollbarView, boolean z) {
        if (!checkView(absListView, scrollbarView)) {
            return false;
        }
        scrollbarView.onBindView(absListView, z);
        scrollbarView.setOnFastScrollListener((scrollX, scrollY, scrollProgress) -> onScroll(absListView, scrollX, scrollY, scrollProgress));
        scrollbarView.setOverScrollProxy(new IScrollProxy() {
            @Override
            public int getScrollY() {
                return absListView.getScrollY();
            }

            @Override
            public boolean isScrolling() {
                return absListView.getScrollY() != 0;
            }
        });
        return true;
    }

    public static boolean bindRecyclerView(RecyclerView recyclerView, ScrollbarView scrollbarView) {
        return bindRecyclerView(recyclerView, scrollbarView, true);
    }

    public static boolean bindRecyclerView(RecyclerView recyclerView, ScrollbarView scrollbarView, boolean z) {
        if (!checkView(recyclerView, scrollbarView)) {
            return false;
        }
        scrollbarView.onBindView(recyclerView, z);
        scrollbarView.setOnFastScrollListener((scrollX, scrollY, scrollProgress) -> {
            int measuredHeight = ScrollbarHelper.getMeasuredHeight(recyclerView);
            int abs = Math.abs(scrollY);
            if (abs < measuredHeight) {
                recyclerView.scrollBy(scrollX, scrollY);
            } else {
                onScroll(recyclerView, scrollX, scrollY, scrollProgress);
            }
        });
        scrollbarView.setOverScrollProxy(new IScrollProxy() {
            @Override
            public int getScrollY() {
                return -((int) recyclerView.getTranslationY());
            }

            @Override
            public boolean isScrolling() {
                return ((int) recyclerView.getTranslationY()) != 0;
            }
        });
        return true;
    }

    public static boolean bindScrollView(ScrollView scrollView, ScrollbarView scrollbarView) {
        return bindScrollView(scrollView, scrollbarView, true);
    }

    public static boolean bindScrollView(ScrollView scrollView, ScrollbarView scrollbarView, boolean z) {
        if (!checkView(scrollView, scrollbarView)) {
            return false;
        }
        scrollbarView.onBindView(scrollView, z);
        scrollbarView.setOnFastScrollListener((scrollX, scrollY, scrollProgress) -> scrollView.smoothScrollBy(scrollX, scrollY));
        scrollbarView.setOverScrollProxy(new ScrollViewOverScrollProxy(scrollView));
        return true;
    }

    public static boolean bindNestedScrollView(NestedScrollView scrollView, ScrollbarView scrollbarView) {
        return bindNestedScrollView(scrollView, scrollbarView, true);
    }

    public static boolean bindNestedScrollView(final NestedScrollView scrollView, ScrollbarView scrollbarView, boolean var2) {
        if (!checkView(scrollView, scrollbarView)) {
            return false;
        } else {
            scrollbarView.onBindView(scrollView,  var2);
            scrollbarView.setOnFastScrollListener((scrollX, scrollY, scrollProgress) -> scrollView.smoothScrollBy(scrollX, scrollY));
            scrollbarView.setOverScrollProxy(new ScrollViewOverScrollProxy(scrollView));
            return true;
        }
    }

    private static void scrollToPosition(AbsListView absListView, int i2, int scrollY, float f2) {
        int itemHeightInPixels;
        int count = 0;
        int firstVisiblePosition = absListView.getFirstVisiblePosition();
        int lastVisiblePosition = absListView.getLastVisiblePosition();
        int height = (absListView.getHeight() - absListView.getPaddingTop()) - absListView.getPaddingBottom();
        if (lastVisiblePosition > firstVisiblePosition && (itemHeightInPixels = height / (lastVisiblePosition - firstVisiblePosition)) > 0) {
            int position = (scrollY / itemHeightInPixels) + firstVisiblePosition;
            int y = scrollY % itemHeightInPixels;
            if (position <= 0) {
                y = 0;
                position = 0;
            } else {
                ListAdapter adapter = absListView.getAdapter();
                if ((adapter != null && position > adapter.getCount() - 1)) {
                    position = count;
                }
            }
            setSelectionFromTop(absListView, position, y);
        }
    }

    public static void onScrollableViewTouchEvent(View view, ScrollbarView scrollbarView, MotionEvent motionEvent) {
        if (view == null || scrollbarView == null || motionEvent == null) {
            return;
        }
        scrollbarView.onScrollableViewTouchEvent(view, motionEvent);
    }
}
