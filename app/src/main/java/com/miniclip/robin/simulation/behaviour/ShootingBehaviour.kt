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
import kotlin.random.Random

private val BASE_SHOOTING_DISTANCE = 16f

class ShootingBehaviour : GameBehaviour {

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

        /** Band-aid to make scoring harder. Should be based on more elaborate math and stats */
        private val SHOOT_HANDICAP = 10

        private val distance = pathToGoal.length()

        private val builder = PitchBuilder()

        private lateinit var result: MatchEventData

        override fun getEventWeight(): Int {
            // TODO make event more likely based on player stats, tactics, etc.
            val maxDistance = PITCH_LENGTH / 2
            val t = 1f - ((distance - BASE_SHOOTING_DISTANCE) / (maxDistance - BASE_SHOOTING_DISTANCE))
            val lerped = lerp(0, WeightedAction.WEIGHT_NORMAL, t.coerceIn(0f, 1f))
            return lerped
        }

        private fun rollResult(): MatchEventData {
            // TODO calculate chance based on distance, stats, etc.
            // TODO check defender blocks

            val shotQuality = player.data.quality - SHOOT_HANDICAP
            val goalieQuality = goalie.data.quality

            if (!rollSkillCheck(Random, shotQuality, 1f)) {
                // missed!
                    //
                return MatchEventData.MissedShot(shooter = player.data)
            }

            return if (rollDuelCheck(Random, shotQuality, goalieQuality, 20)) {
                MatchEventData.Goal(player.team, player.data, type = GoalType.SHOT_DISTANCE)
            } else {
                MatchEventData.SavedShot(shooter = player.data, goalie = goalie.data)
            }
        }

        override fun isSuccess(random: Random): Boolean {
            result = rollResult()
            return result is MatchEventData.Goal
        }

        override fun getEventData(random: Random, success: Boolean): MatchEventData {
            return result
        }

        override fun applyEvent(state: PitchState, event: MatchEventData, success: Boolean): PitchState {
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
                    val newX = Random.nextInt(PITCH_WIDTH)
                    val newY = PITCH_LENGTH / 4 + Random.nextInt(PITCH_LENGTH / 2)
                    return state.copy(
                        ballPosition = Vec2(newX, newY),
                        possession = null
                    )
                }
                // TODO corner, defender interception etc.
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