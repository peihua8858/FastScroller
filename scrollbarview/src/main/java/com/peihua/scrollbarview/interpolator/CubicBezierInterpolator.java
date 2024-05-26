package com.peihua.scrollbarview.interpolator;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.Interpolator;

import com.peihua.scrollbarview.R;

public class CubicBezierInterpolator implements Interpolator {
    float mControlPoint1x;

    float mControlPoint1y;

    float mControlPoint2x;

    float mControlPoint2y;

    public CubicBezierInterpolator(float fromXDelta, float fromYDelta, float toXDelta, float toYDelta) {
        this.mControlPoint1x = fromXDelta;
        this.mControlPoint1y = fromYDelta;
        this.mControlPoint2x = toXDelta;
        this.mControlPoint2y = toYDelta;
    }

    public CubicBezierInterpolator(Context context, AttributeSet attributeSet) {
        Resources resources = context.getResources();
        Resources.Theme theme = context.getTheme();
        this.mControlPoint1x = 0.0f;
        this.mControlPoint1y = 0.0f;
        this.mControlPoint2x = 0.0f;
        this.mControlPoint2y = 0.0f;
        TypedArray typedArray = theme != null ? theme.obtainStyledAttributes(attributeSet, R.styleable.TranslateAnimation, 0, 0) : resources.obtainAttributes(attributeSet, R.styleable.TranslateAnimation);
        this.mControlPoint1x = getFloatValueFromTypedValue(typedArray.peekValue(R.styleable.TranslateAnimation_fromXDelta));
        this.mControlPoint1y = getFloatValueFromTypedValue(typedArray.peekValue(R.styleable.TranslateAnimation_fromYDelta));
        this.mControlPoint2x = getFloatValueFromTypedValue(typedArray.peekValue(R.styleable.TranslateAnimation_toXDelta));
        this.mControlPoint2y = getFloatValueFromTypedValue(typedArray.peekValue(R.styleable.TranslateAnimation_toYDelta));
        typedArray.recycle();
    }

    private float getFloatValueFromTypedValue(TypedValue typedValue) {
        if (typedValue == null) {
            return 1.0f;
        }
        int type = typedValue.type;
        if (type == TypedValue.TYPE_FLOAT) {
            return typedValue.getFloat();
        } else if (type == TypedValue.TYPE_FRACTION) {
            return TypedValue.complexToFloat(typedValue.data);
        } else if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT) {
            return typedValue.data;
        }
        return 1.0f;
    }

    public long calculateDuration(float progress) {
        long low = 0;
        long high = 4000;
        while (low <= high) {
            long mid = (low + high) >>> 1;
            float time = ((float) mid) * 2.5E-4f;
            float timeLeft = 1.0f - time;
            float timeCubed = time * time * time;
            float timeLeftCubedTimesThree = 3.0f * timeLeft;
            float progressAtMid = timeCubed + (timeLeftCubedTimesThree * time * time * this.mControlPoint2x) + (timeLeft * timeLeftCubedTimesThree * time * this.mControlPoint1x);
            if (progressAtMid < progress) {
                low = mid + 1;
            } else {
                if (progressAtMid <= progress) {
                    return mid;
                }
                high = mid - 1;
            }
        }
        return low;
    }

    public float calculateBezierCurveY(float progress) {
        float timeLeft = 1.0f - progress;
        float timeLeftCubedTimesThree = 3.0f * timeLeft;
        return (progress * progress * progress) + (timeLeftCubedTimesThree * progress * progress * this.mControlPoint2y) + (timeLeft * timeLeftCubedTimesThree * progress * this.mControlPoint1y);
    }

    @Override
    public float getInterpolation(float progress) {
        return calculateBezierCurveY(((float) calculateDuration(progress)) * 2.5E-4f);
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("CubicBezierInterpolator");
        stringBuffer.append("  mControlPoint1x = ");
        stringBuffer.append(this.mControlPoint1x);
        stringBuffer.append(", mControlPoint1y = ");
        stringBuffer.append(this.mControlPoint1y);
        stringBuffer.append(", mControlPoint2x = ");
        stringBuffer.append(this.mControlPoint2x);
        stringBuffer.append(", mControlPoint2y = ");
        stringBuffer.append(this.mControlPoint2y);
        return stringBuffer.toString();
    }
}
