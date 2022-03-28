package com.miniclip.robin.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.IOException
import java.io.InputStream

/**
 * Helper to create a [DataSource] for any object that can be deserialized from an InputStream providing a json object.
 * The type [T] should be a class annotated with @[Serializable].
 *
 * Note that this functions expects that exactly one object would be present in the stream
 * and throws an exception if there are any dangling bytes after an object.
 *
 * @throws [SerializationException] if the given JSON input cannot be deserialized to the value of type [T].
 * @throws [IOException] If an I/O error occurs and stream can't be read from.
 */
@ExperimentalSerializationApi
inline fun <reified T : Any> jsonDataSource(crossinline streamProvider: () -> InputStream) = DataSource<T> {
    val stream = streamProvider()
    Json.decodeFromStream(stream)
}
