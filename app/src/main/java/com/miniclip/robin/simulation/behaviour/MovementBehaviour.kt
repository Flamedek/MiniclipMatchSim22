package com.miniclip.robin.simulation.behaviour

import com.miniclip.robin.data.model.PlayerRole
import com.miniclip.robin.simulation.PITCH_LENGTH
import com.miniclip.robin.simulation.PITCH_WIDTH
import com.miniclip.robin.simulation.findClosestToPoint
import com.miniclip.robin.simulation.model.PitchState
import com.miniclip.robin.simulation.model.PositionedPlayer
import com.miniclip.robin.util.Vec2
import com.miniclip.robin.util.roundToInts
import com.miniclip.robin.util.toFloats
import kotlin.random.Random

/** Base distance to travel for a player per simulation step */
private val DEFAULT_MOVEMENT = 4f
/** Distance from the goal that defenders will move towards when being attacked */
private val DEFENCE_DISTANCE = 20f

class MovementBehaviour(private val random: Random) : GameBehaviour {

    /**
     * Applies the default player movement behaviour.
     * Rules consist of:
     * - if the ball is free, the closest player of both teams moves to take possession.
     * - attacker with the ball moves towards goal
     * - attackers move to supporting positions
     * - defenders move to defencive positions
     */
    override fun applyFixedMutations(state: PitchState): PitchState {
        val attackingTeam = state.possession?.team
        var ballPos = state.ballPosition

        if (attackingTeam == null) {
            // closest players move to ball
            val closestHome = state.homePlayers.findClosestToPoint(ballPos)
            val closestAway = state.awayPlayers.findClosestToPoint(ballPos)

            closestHome.moveTowards(ballPos, playerSpeed(closestHome))
            closestAway.moveTowards(ballPos, playerSpeed(closestAway))

            if (closestHome.position == ballPos && closestAway.position == ballPos) {
                val roll = rollDuelCheck(random, closestHome.data.quality, closestAway.data.quality)
                val tackleWinner = if (roll) closestHome else closestAway
                return state.copy(possession = tackleWinner)
            }  else if (closestHome.position == ballPos) {
                return state.copy(possession = closestHome)
            } else if (closestAway.position == ballPos) {
                return state.copy(possession = closestAway)
            }

            // TODO all other players move back into formation

        } else {
            val attackingPlayers = if (attackingTeam == state.homePlayers.first().team) state.homePlayers else state.awayPlayers
            val defendingPlayers = if (attackingTeam == state.awayPlayers.first().team) state.homePlayers else state.awayPlayers

            val targetGoal = if (attackingTeam == state.homePlayers.first().team) {
                Vec2(PITCH_WIDTH / 2, PITCH_LENGTH)
            } else {
                Vec2(PITCH_WIDTH / 2, 0)
            }

            attackingPlayers.forEach { player ->
                // TODO consult team tactics
                if (player == state.possession) {
                    // attacker with ball moves towards goal
                    player.moveTowards(targetGoal, playerSpeed(state.possession))
                    ballPos = player.position
                } else if (player.data.role == PlayerRole.ATTACK || player.data.role == PlayerRole.MIDFIELD) {
                    // his team moves forwards
                    val target = Vec2(player.position.x, targetGoal.y)
                    player.moveTowards(target, playerSpeed(player))
                } else if (player.data.role == PlayerRole.DEFENCE) {
                    // TODO move up defenders up to a certain line
                }
            }

            // defenders move backwards towards goal.
            defendingPlayers.forEach { player ->
                // TODO consult team tactics
                if (player.data.role == PlayerRole.DEFENCE) {
                    // Defenders move to a distance from goal. Unless the attacker is closer to the goal than themselves, then they move straight into goal.
                    val target = if (player.position.distanceSquared(targetGoal) < state.possession.position.distanceSquared(targetGoal)) {
                        player.position.subtract(targetGoal).toFloats().normalize().multiply(DEFENCE_DISTANCE).roundToInts()
                    } else {
                        targetGoal
                    }
                    player.moveTowards(target, playerSpeed(player))
                }
                if (player.data.role == PlayerRole.ATTACK || player.data.role == PlayerRole.MIDFIELD) {
                    val target = Vec2(player.position.x, targetGoal.y)
                    player.moveTowards(target, playerSpeed(player))
                }
            }
        }
        return state.copy(ballPosition = ballPos)
    }

    private fun playerSpeed(player: PositionedPlayer): Float {
        // TODO account for speed, acceleration, dribbling etc.
        return DEFAULT_MOVEMENT
    }
}
