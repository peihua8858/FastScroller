package com.peihua.scrollbarview.widget;

import android.os.Build.VERSION;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public class ScrollbarHelper {
    private static final String TAG = "ScrollbarHelper";

    private ScrollbarHelper() {
    }

    public static void bindScrollableView(View view, ScrollbarView scrollbarView) {
        if (view == null) {
            Log.d("ScrollbarView", "bindScrollView is null");
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
            Log.d("ScrollbarView", "bind view warning");
        }
    }

    private static boolean check(View var0, ScrollbarView var1) {
        if (var0 != null && var1 != null) {
            return var1.getScrollableView() == null;
        } else {
            return false;
        }
    }

    public static boolean bindListView(AbsListView var0, ScrollbarView var1) {
        return bindListView(var0, var1, true);
    }

    public static boolean bindListView(AbsListView absListView, ScrollbarView scrollbarView, boolean z) {
        if (!check(absListView, scrollbarView)) {
            return false;
        } else {
            ScrollbarView.getScrollBindImpl().onBindView(absListView, scrollbarView, z);
            scrollbarView.setOnTouchOffsetListener((var11, var21, var3) -> {
                ListAdapter var4 = absListView.getAdapter();
                if (var4 != null) {
                    int var5 = absListView.getFirstVisiblePosition();
                    int var6 = absListView.getLastVisiblePosition() - var5;
                    int var7 = var4.getCount() - var6;
                    int var8 = Math.round((float) var21 / (float) var11 * (float) var7);
                    if (var3) {
                        if (var8 <= var5) {
                            return;
                        }
                    } else if (var8 >= var5) {
                        return;
                    }

                    if (VERSION.SDK_INT >= 21) {
                        absListView.setSelectionFromTop(var8, 0);
                    } else if (absListView instanceof ListView) {
                        absListView.setSelectionFromTop(var8, 0);
                    } else if (absListView instanceof GridView) {
                        absListView.setSelection(var8);
                    } else {
                        Log.i("ScrollbarHelper", "other widget do nothing");
                    }

                }
            });
            return true;
        }
    }

    public static boolean bindRecyclerView(RecyclerView var0, ScrollbarView var1) {
        return bindRecyclerView(var0, var1, true);
    }

    public static boolean bindRecyclerView(RecyclerView recyclerView, ScrollbarView scrollbarView, boolean z) {
        if (!check(recyclerView, scrollbarView)) {
            return false;
        } else {
            ScrollbarView.getScrollBindImpl().onBindView(recyclerView, scrollbarView, z);
            scrollbarView.setOnTouchOffsetListener((var11, var21, var3) -> {
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) {
                    RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                    if (manager != null && manager.getItemCount() > 0) {
                        int itemCount = manager.getItemCount() - manager.getChildCount();
                        if (itemCount >= 0) {
                            int var7 = (int) ((float) var21 / (float) var11 * (float) itemCount);
                            if (var3) {
                                var7 += manager.getItemCount() - itemCount;
                                if (var7 >= manager.getItemCount()) {
                                    var7 = manager.getItemCount() - 1;
                                }
                            }

                            recyclerView.scrollToPosition(var7);
                        }
                    }
                }
            });
            return true;
        }
    }

    public static boolean bindScrollView(ScrollView scrollView, ScrollbarView scrollbarView) {
        return bindScrollView(scrollView, scrollbarView, true);
    }

    public static boolean bindScrollView(ScrollView scrollView, ScrollbarView scrollbarView, boolean z) {
        if (!check(scrollView, scrollbarView)) {
            return false;
        } else {
            ScrollbarView.getScrollBindImpl().onBindView(scrollView, scrollbarView, z);
            scrollbarView.setOnTouchOffsetListener((var11, var21, var3) -> {
                View var4 = scrollView.getChildAt(0);
                if (var4 != null) {
                    int var5 = var4.getHeight() - (scrollView.getHeight() - scrollView.getPaddingTop() - scrollView.getPaddingBottom());
                    int var6 = (int) ((float) var5 / (float) var11 * (float) var21);
                    scrollView.smoothScrollTo(0, var6);
                }
            });
            return true;
        }
    }

    public static boolean bindNestedScrollView(NestedScrollView scrollView, ScrollbarView scrollbarView) {
        return bindNestedScrollView(scrollView, scrollbarView, true);
    }

    public static boolean bindNestedScrollView(final NestedScrollView var0, ScrollbarView var1, boolean var2) {
        if (!check(var0, var1)) {
            return false;
        } else {
            ScrollbarView.getScrollBindImpl().onBindView(var0, var1, var2);
            var1.setOnTouchOffsetListener((var11, var21, var3) -> {
                View child = var0.getChildAt(0);
                if (child != null) {
                    int var5 = child.getHeight() - (var0.getHeight() - var0.getPaddingTop() - var0.getPaddingBottom());
                    int var6 = (int) ((float) var5 / (float) var11 * (float) var21);
                    var0.smoothScrollTo(0, var6);
                }
            });
            return true;
        }
    }


    public static void onScrollableViewTouchEvent(View var0, ScrollbarView var1, MotionEvent var2) {
        ScrollbarView.getScrollBindImpl().onScrollableViewTouchEvent(var0, var1, var2);
    }
}
