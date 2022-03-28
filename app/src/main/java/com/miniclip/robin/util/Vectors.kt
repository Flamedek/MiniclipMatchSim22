package com.miniclip.robin.util

import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Vec2(val x: Int, val y: Int) {

    fun add(x: Int, y: Int) = Vec2(this.x + x, this.y + y)

    fun add(other: Vec2) = Vec2(x + other.x, y + other.y)

    fun subtract(x: Int, y: Int) = Vec2(this.x - x, this.y - y)

    fun subtract(other: Vec2) = Vec2(x - other.x, y - other.y)

    fun distance(other: Vec2) = subtract(other).length()

    fun distanceSquared(other: Vec2) = subtract(other).lengthSquared()

    fun length() = sqrt(lengthSquared().toDouble())

    fun lengthSquared() = x * x + y * y

    fun multiply(scale: Int) = Vec2(x * scale, y * scale)

    operator fun plus(other: Vec2) = add(other)

    operator fun minus(other: Vec2) = subtract(other)

    operator fun times(value: Int) = multiply(value)

}

data class Vec2F(val x: Float, val y: Float) {

    fun add(x: Float, y: Float) = Vec2F(this.x + x, this.y + y)

    fun add(other: Vec2F) = Vec2F(x + other.x, y + other.y)

    fun subtract(x: Float, y: Float) = Vec2F(this.x - x, this.y - y)

    fun subtract(other: Vec2F) = Vec2F(x - other.x, y - other.y)

    fun distance(other: Vec2F) = subtract(other).length()

    fun distanceSquared(other: Vec2F) = subtract(other).lengthSquared()

    fun length() = sqrt(lengthSquared())

    fun lengthSquared() = x * x + y * y

    fun multiply(scale: Float) = Vec2F(x * scale, y * scale)

    fun divide(factor: Float) = Vec2F(x / factor, y / factor)

    fun normalize() = divide(length().takeIf { it != 0f } ?: 1f)

    operator fun plus(other: Vec2F) = add(other)

    operator fun minus(other: Vec2F) = subtract(other)

    operator fun times(value: Float) = multiply(value)

    operator fun div(value: Float) = divide(value)

}


fun Vec2.toFloats() = Vec2F(x.toFloat(), y.toFloat())

fun Vec2F.toInts() = Vec2(x.toInt(), y.toInt())

fun Vec2F.roundToInts() = Vec2(x.roundToInt(), y.roundToInt())

fun Vec2F.ceilToInts() = Vec2(ceil(x).toInt(), ceil(y).toInt())