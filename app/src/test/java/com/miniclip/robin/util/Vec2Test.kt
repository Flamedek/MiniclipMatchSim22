package com.miniclip.robin.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Vec2Test {

    @Test
    fun add() {
        val vec = Vec2(3, 2)
        assertEquals(Vec2(5, 5), vec.add(2, 3))
    }

    @Test
    fun subtract() {
        val vec = Vec2(5, 5)
        assertEquals(Vec2(3, 2), vec.subtract(2, 3))
    }

    @Test
    fun distance() {
        val p1 = Vec2(14, 23)
        val p2 = Vec2(10, 20)
        assertEquals(5, p1.distance(p2).toInt())
        assertEquals(25, p1.distanceSquared(p2))
    }

    @Test
    fun length() {
        val vec = Vec2(4, 3)
        assertEquals(5, vec.length().toInt())
        assertEquals(25, vec.lengthSquared())
    }
}