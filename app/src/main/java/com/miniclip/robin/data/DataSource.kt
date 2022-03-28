package com.miniclip.robin.data

/**
 * Generic functional interface to retrieve some data [T].
 */
fun interface DataSource<T> {
    fun getData(): T
}