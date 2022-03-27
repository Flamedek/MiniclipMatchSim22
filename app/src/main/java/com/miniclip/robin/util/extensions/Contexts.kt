package com.miniclip.robin.util.extensions

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.TypedValue
import androidx.fragment.app.Fragment


fun Context.dpToPx(value: Int): Int {
    return dpToPx(value.toFloat()).toInt()
}

fun Context.dpToPx(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}

fun Fragment.dpToPx(value: Int): Int {
    return dpToPx(value.toFloat()).toInt()
}

fun Fragment.dpToPx(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}

fun Resources.getResourceUri(resourceId: Int): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(getResourcePackageName(resourceId))
        .appendPath(getResourceTypeName(resourceId))
        .appendPath(getResourceEntryName(resourceId))
        .build()
}

val Any.TAG get() = javaClass.simpleName