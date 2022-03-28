package com.miniclip.robin.simulation.behaviour

import com.miniclip.robin.data.model.PlayerRole
import com.miniclip.robin.simulation.PITCH_LENGTH
import com.miniclip.robin.simulation.PITCH_WIDTH
import com.miniclip.robin.simulation.PitchBuilder
import com.miniclip.robin.simulation.model.GoalType
import com.miniclip.robin.simulation.model.MatchEventData
import com.miniclip.robin.simulation.model.PitchState
import com.miniclip.robin.simulation.model.PositionedPlayer
import com.miniclip.robin.util.Vec2
import com.miniclip.robin.util.Vec2F
import com.miniclip.robin.util.toFloats
import kotlin.math.max
import kotlin.random.Random

/** The ideal distance to take a shot */
private val BASE_SHOOTING_DISTANCE = 16f

/**
 * Behaviour to insert shot events based on the attackers current position
 */
class ShootingBehaviour(private val random: Random) : GameBehaviour {

    override fun getWeightedActions(state: PitchState): List<WeightedAction> {
        val attackingTeam = state.possession?.team ?: return emptyList()
        val shooter = state.possession

        val targetGoal = if (attackingTeam == state.homePlayers.first().team) {
            Vec2(PITCH_WIDTH / 2, PITCH_LENGTH)
        } else {
            Vec2(PITCH_WIDTH / 2, 0)
        }

        if (shooter.position.distanceSquared(targetGoal) > ((PITCH_LENGTH / 2) * (PITCH_LENGTH / 2))) {
            // don't shoot from own half
            return emptyList()
        }

        val defendingPlayers = if (attackingTeam == state.awayPlayers.first().team) state.homePlayers else state.awayPlayers
        val goalie = defendingPlayers.find { player -> player.data.role == PlayerRole.GOALIE }!!
        val path = targetGoal.subtract(shooter.position).toFloats()

        return listOf(DistanceShotAction(shooter, goalie, path))
    }

    inner class DistanceShotAction(val player: PositionedPlayer, val goalie: PositionedPlayer, val pathToGoal: Vec2F) : WeightedAction {

        private val distance = pathToGoal.length()

        private val builder = PitchBuilder()

        override fun getEventWeight(): Int {
            // TODO make event more likely based on player stats, tactics, etc.
            val maxDistance = PITCH_LENGTH / 2
            val t = 1f - ((distance - BASE_SHOOTING_DISTANCE) / (maxDistance - BASE_SHOOTING_DISTANCE))
            return lerp(0, WeightedAction.WEIGHT_NORMAL, t.coerceIn(0f, 1f))
        }

        private fun rollResult(): MatchEventData {
            // TODO calculate chance based on distance, stats, etc.
            // TODO check if defender blocks

            val shotQuality = player.data.quality
            val goalieQuality = goalie.data.quality

            val handicap = max(0f, distance - BASE_SHOOTING_DISTANCE).toInt()
            if (!rollSkillCheck(random, shotQuality - handicap, 1f)) {
                // missed!
                return MatchEventData.MissedShot(shooter = player.data)
            }

            return if (rollDuelCheck(random, goalieQuality, shotQuality - handicap, 10)) {
                MatchEventData.SavedShot(shooter = player.data, goalie = goalie.data)
            } else {
                MatchEventData.Goal(player.team, player.data, type = GoalType.SHOT_DISTANCE)
            }
        }

        override fun getEventData(): MatchEventData {
            return rollResult()
        }

        override fun applyEvent(state: PitchState, event: MatchEventData): PitchState {
            when (event) {
                is MatchEventData.Goal -> {
                    // reset back to kickoff
                    val teams = state.homePlayers.first().team to state.awayPlayers.first().team
                    val opponentTeam = if (event.team == teams.first) teams.second else teams.first
                    return builder.buildKickoffState(teams, opponentTeam)
                }
                is MatchEventData.SavedShot,
                is MatchEventData.MissedShot -> {
                    // Keeper ball. He immediately shoots it somewhere into the field
                    // TODO chance to pass to defender instead.
                    val newX = random.nextInt(PITCH_WIDTH)
                    val newY = PITCH_LENGTH / 4 + random.nextInt(PITCH_LENGTH / 2)
                    return state.copy(
                        ballPosition = Vec2(newX, newY),
                        possession = null
                    )
                }
                // TODO caused a corner, defender interception etc.
                else -> throw NotImplementedError()
            }
        }
    }
}

fun lerp(a: Int, b: Int, t: Float): Int {
    return a + ((b - a) * t).toInt()
}

fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}