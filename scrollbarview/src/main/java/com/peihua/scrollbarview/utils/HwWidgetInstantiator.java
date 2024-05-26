package com.peihua.scrollbarview.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class HwWidgetInstantiator {
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_TELEVISION = 2;
    public static final int TYPE_CAR = 4;
    private static final String a = "HwWidgetInstantiator";
    private static final String b = "com.huawei.uikit";
    private static final String c = "com.huawei.uikit.phone";
    private static final String d = "com.huawei.uikit.tv";
    private static final String e = "com.huawei.uikit.car";
    private static final Map<String, Class<?>> f = new ArrayMap();

    private HwWidgetInstantiator() {
    }

    public static String getDeviceClassName(@NonNull Context var0, @NonNull Class<?> var1) {
        return getDeviceClassName(var0, var1, getCurrnetType(var0));
    }

    public static String getDeviceClassName(@NonNull Context var0, @NonNull Class<?> var1, int var2) {
        String var3;
        switch (var2) {
            case 1:
            case 3:
            default:
                var3 = "com.huawei.uikit.phone";
                break;
            case 2:
                var3 = "com.huawei.uikit.tv";
                break;
            case 4:
                var3 = "com.huawei.uikit.car";
        }

        String var4 = var1.getName();
        return var4.replace("com.huawei.uikit", var3);
    }

    public static int getCurrnetType(@NonNull Context var0) {
        int var1 = var0.getResources().getConfiguration().uiMode;
        byte var2;
        switch (var1 & 15) {
            case 3:
                var2 = 4;
                break;
            case 4:
                var2 = 2;
                break;
            default:
                var2 = 1;
        }

        return var2;
    }

    @Nullable
    public static Object instantiate(Context var0, String var1, Class<?> var2) {
        if (var0 != null && !TextUtils.isEmpty(var1) && var2 != null) {
            try {
                Class var3 = (Class)f.get(var1);
                if (var3 == null) {
                    var3 = var0.getClassLoader().loadClass(var1);
                    if (!var2.isAssignableFrom(var3)) {
                        Log.w("HwWidgetInstantiator", "Trying to instantiate a class " + var1 + " that is not a " + var2.getName());
                        return null;
                    }

                    f.put(var1, var3);
                }

                Constructor var4 = var3.getDeclaredConstructor(Context.class);
                return var4.newInstance(var0);
            } catch (ClassNotFoundException var5) {
                Log.w("HwWidgetInstantiator", var1 + ": make sure class name exists, is public, and has an empty constructor that is public");
            } catch (NoSuchMethodException var6) {
                Log.w("HwWidgetInstantiator", var1 + ": could not find constructor");
            } catch (InvocationTargetException var7) {
                Log.w("HwWidgetInstantiator", var1 + ": calling constructor caused an InvocationTargetException");
            } catch (IllegalAccessException var8) {
                Log.w("HwWidgetInstantiator", var1 + ": calling constructor caused an IllegalAccessException");
            } catch (InstantiationException var9) {
                Log.w("HwWidgetInstantiator", var1 + ": calling constructor caused an InstantiationException");
            }

            return null;
        } else {
            return null;
        }
    }
}
