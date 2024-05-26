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

public class NewScrollbarHelper {
    private static final String TAG = "NewScrollbarHelper";

    private NewScrollbarHelper() {
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
        }  else if (view instanceof NestedScrollView) {
            NewScrollbarHelper.bindNestedScrollView((NestedScrollView) view, scrollbarView);
        }else {
            Log.d("CustomScrollbarView", "bind view warning");
        }
    }
    private static boolean check(View var0, NewScrollbarView var1) {
        if (var0 != null && var1 != null) {
            return var1.getScrollableView() == null;
        } else {
            return false;
        }
    }
    public static boolean bindListView(AbsListView var0, NewScrollbarView var1) {
        return bindListView(var0, var1, true);
    }
    public static boolean bindListView(AbsListView absListView, NewScrollbarView scrollbarView, boolean z) {
        if (!check(absListView, scrollbarView)) {
            return false;
        } else {
            NewScrollbarView.getScrollBindImpl().onBindView(absListView, scrollbarView, z);
            scrollbarView.setOnTouchOffsetListener((var11, var21, var3) -> {
                ListAdapter var4 = absListView.getAdapter();
                if (var4 != null) {
                    int var5 = absListView.getFirstVisiblePosition();
                    int var6 = absListView.getLastVisiblePosition() - var5;
                    int var7 = var4.getCount() - var6;
                    int var8 = Math.round((float) var21 / (float) var11 * (float)var7);
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
                        Log.i("HwScrollbarHelper", "other widget do nothing");
                    }

                }
            });
            return true;
        }
    }
    public static boolean bindRecyclerView(RecyclerView var0, NewScrollbarView var1) {
        return bindRecyclerView(var0, var1, true);
    }
    public static boolean bindRecyclerView(RecyclerView recyclerView, NewScrollbarView scrollbarView, boolean z) {
        if (!check(recyclerView, scrollbarView)) {
            return false;
        } else {
            NewScrollbarView.getScrollBindImpl().onBindView(recyclerView, scrollbarView, z);
            scrollbarView.setOnTouchOffsetListener((var11, var21, var3) -> {
                RecyclerView.Adapter<?> var4 = recyclerView.getAdapter();
                if (var4 != null) {
                    RecyclerView.LayoutManager var5 = recyclerView.getLayoutManager();
                    if (var5 != null && var5.getItemCount() > 0) {
                        int var6 = var5.getItemCount() - var5.getChildCount();
                        if (var6 >= 0) {
                            int var7 = (int)((float) var21 / (float) var11 * (float)var6);
                            if (var3) {
                                var7 += var5.getItemCount() - var6;
                                if (var7 >= var5.getItemCount()) {
                                    var7 = var5.getItemCount() - 1;
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
    public static boolean bindScrollView(ScrollView scrollView, NewScrollbarView scrollbarView) {
        return bindScrollView(scrollView, scrollbarView, true);
    }
    public static boolean bindScrollView(ScrollView scrollView, NewScrollbarView scrollbarView, boolean z) {
        if (!check(scrollView, scrollbarView)) {
            return false;
        } else {
            NewScrollbarView.getScrollBindImpl().onBindView(scrollView, scrollbarView, z);
            scrollbarView.setOnTouchOffsetListener((var11, var21, var3) -> {
                View var4 = scrollView.getChildAt(0);
                if (var4 != null) {
                    int var5 = var4.getHeight() - (scrollView.getHeight() - scrollView.getPaddingTop() - scrollView.getPaddingBottom());
                    int var6 = (int)((float) var5 / (float) var11 * (float) var21);
                    scrollView.smoothScrollTo(0, var6);
                }
            });
            return true;
        }
    }
    public static boolean bindNestedScrollView(NestedScrollView scrollView, NewScrollbarView scrollbarView) {
        return bindNestedScrollView(scrollView, scrollbarView, true);
    }

    public static boolean bindNestedScrollView(final NestedScrollView var0, NewScrollbarView var1, boolean var2) {
        if (!check(var0, var1)) {
            return false;
        } else {
            NewScrollbarView.getScrollBindImpl().onBindView(var0, var1, var2);
            var1.setOnTouchOffsetListener((var11, var21, var3) -> {
                View var4 = var0.getChildAt(0);
                if (var4 != null) {
                    int var5 = var4.getHeight() - (var0.getHeight() - var0.getPaddingTop() - var0.getPaddingBottom());
                    int var6 = (int)((float) var5 / (float) var11 * (float) var21);
                    var0.smoothScrollTo(0, var6);
                }
            });
            return true;
        }
    }


    public static void onScrollableViewTouchEvent(View var0, NewScrollbarView var1, MotionEvent var2) {
        ScrollbarView.getScrollBindImpl().onScrollableViewTouchEvent(var0, var1, var2);
    }
}
