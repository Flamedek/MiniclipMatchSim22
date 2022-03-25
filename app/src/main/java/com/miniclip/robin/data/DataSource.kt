package com.miniclip.robin.data

interface DataSource<T> {
    suspend fun getData(): T
}