package com.miniclip.robin.simulation.behaviour

import com.miniclip.robin.simulation.PITCH_LENGTH
import com.miniclip.robin.simulation.PITCH_WIDTH
import com.miniclip.robin.simulation.findClosestToPoint
import com.miniclip.robin.simulation.model.PitchState
import com.miniclip.robin.simulation.model.PositionedPlayer
import com.miniclip.robin.util.Vec2
import kotlin.random.Random

/**
 * Behaviour to check if ball is out of bounds, and applies the correct throw in, corner or keeper throws.
 */
class OutOfBoundsBehaviour(private val random: Random) : GameBehaviour {

    private lateinit var lastPlayerPossession: PositionedPlayer

    override fun applyFixedMutations(state: PitchState): PitchState {
        val pos = state.ballPosition
        var newPos = pos
        var newPossession = state.possession

        if (pos.x < 0 || pos.x > PITCH_WIDTH) {
            // ball of the sides. Give it to the other team
            newPos = Vec2(pos.x.coerceIn(0, PITCH_WIDTH), pos.y)
            val players = if (lastPlayerPossession in state.homePlayers) state.awayPlayers else state.homePlayers
            newPossession = players.findClosestToPoint(newPos)
            newPossession.moveTowards(newPos, PITCH_LENGTH.toFloat())
            // TODO actually pass the ball. currently the 'thrower' will just run off with it..
        }
        if (pos.y < 0) {
            if (lastPlayerPossession in state.homePlayers) {
                // TODO corner for away team
            } else {
                // Keeper ball. He immediately shoots it somewhere into the field
                newPos = randomKickPosition()
                newPossession = null
            }
        } else if (pos.y > PITCH_LENGTH) {
            if (lastPlayerPossession in state.awayPlayers) {
                // TODO corner for home team
            } else {
                // Keeper ball. He immediately shoots it somewhere into the field
                newPos = randomKickPosition()
                newPossession = null
            }
        }

        // save possession for next step
        state.possession?.also { lastPlayerPossession = it }
        return if (pos != newPos) state.copy(ballPosition = newPos, possession = newPossession) else state
    }

    private fun randomKickPosition(): Vec2 {
        val newX = random.nextInt(PITCH_WIDTH)
        val newY = PITCH_LENGTH / 4 + random.nextInt(PITCH_LENGTH / 2)
        return Vec2(newX, newY)
    }
}