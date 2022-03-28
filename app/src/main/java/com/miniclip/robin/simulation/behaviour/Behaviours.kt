package com.miniclip.robin.simulation.behaviour

import com.miniclip.robin.simulation.model.PositionedPlayer
import com.miniclip.robin.util.Vec2
import com.miniclip.robin.util.roundToInts
import com.miniclip.robin.util.toFloats
import kotlin.math.min
import kotlin.random.Random


/**
 * Rolls a random chance based on a skills quality.
 * @param quality the relevant quality of the player executing the event
 * @param difficulty indication how hard it is to pass. With a difficulty of 1, and a quality of 100 the check will always pass.
 * A lower quality or difficulty will decrease the passing chance linearly.
 */
fun GameBehaviour.rollSkillCheck(random: Random, quality: Int, difficulty: Float): Boolean {
    /* This could be much better with a curved function */
    /* Or a dnd-system where a value is rolled with a given bonus and checked against a threshold */
    return random.nextInt(quality) / 100f < difficulty
}

/**
 * Rolls a random chance to pick one of two options based on relevant qualities.
 * The chance is based on the difference in quality.
 * @param qualityOne quality of player one
 * @param qualityTwo quality of player two
 * @param referenceDif The amount of difference in quality that will cause the higher player ot have double the chance of success as the other.
 * @return true if player one won, false for player two.
 */
fun GameBehaviour.rollDuelCheck(random: Random, qualityOne: Int, qualityTwo: Int, referenceDif: Int = 12): Boolean {
    fun scoringValue(quality: Int, othersQuality: Int): Int {
        val minQuality = min(quality, othersQuality)
        val adjustment = min(0, referenceDif - minQuality)
        return random.nextInt(quality + adjustment)
    }
    return scoringValue(qualityOne, qualityTwo) > scoringValue(qualityTwo, qualityOne)
}

/**
 * Update position to move towards a target with a max delta of [speed]
 */
fun PositionedPlayer.moveTowards(target: Vec2, speed: Float) {
    position = moveTowards(position, target, speed)
}

private fun moveTowards(position: Vec2, target: Vec2, speed: Float): Vec2 {
    if (position.distanceSquared(target) < speed * speed) {
        return target
    }
    val direction = target.subtract(position).toFloats()
    val delta = direction.normalize().multiply(speed).roundToInts()
    return position + delta
}