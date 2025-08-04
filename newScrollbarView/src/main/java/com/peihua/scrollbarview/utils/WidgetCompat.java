package com.peihua.scrollbarview.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

public class WidgetCompat {
    public static Context wrapContext(Context context, int attrId, int themeResId) {
        return context.getTheme().resolveAttribute(attrId, new TypedValue(), false) ? context : new ContextThemeWrapper(context, themeResId);
    }
}
