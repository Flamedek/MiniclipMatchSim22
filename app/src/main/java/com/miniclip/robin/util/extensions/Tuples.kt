package com.miniclip.robin.util.extensions

operator fun Pair<*, *>.contains(value: Any?): Boolean {
    return first == value || second == value
}

fun <T> Pair<T, T>.getOther(value: T): T? {
    return when {
        first == value -> second
        second == value -> first
        else -> null
    }
}