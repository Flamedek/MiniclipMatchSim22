package com.miniclip.robin.util

import android.animation.TimeInterpolator
import kotlin.math.pow

/**
 * Smooth interpolator.
 * Source by [https://easings.net](https://easings.net/#easeInOutQuart)
 */
class EaseInOutQuartInterpolator : TimeInterpolator {
    override fun getInterpolation(x: Float): Float {
        return if (x < 0.5f) 8f * x * x * x * x else 1f - (-2f * x + 2).pow(4f) / 2f
    }
}