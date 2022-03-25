package com.miniclip.robin.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

@ExperimentalSerializationApi
inline fun <reified T : Any> jsonDataSource(crossinline streamProvider: () -> InputStream) = object : DataSource<T> {
    override suspend fun getData(): T {
        val stream = streamProvider()
        return Json.decodeFromStream(stream)
    }
}