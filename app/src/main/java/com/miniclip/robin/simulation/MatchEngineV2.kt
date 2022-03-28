package com.miniclip.robin.simulation

import com.miniclip.robin.simulation.behaviour.MovementBehaviour
import com.miniclip.robin.simulation.behaviour.ShootingBehaviour
import com.miniclip.robin.simulation.behaviour.WeightedAction
import com.miniclip.robin.simulation.behaviour.WeightedAction.Companion.MIN_WEIGHT_SUM
import com.miniclip.robin.simulation.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random

const val SIMULATION_STEPS = 90

const val MINS_PER_STEP = SIMULATION_STEPS / 90f

const val PITCH_WIDTH = 68
const val PITCH_LENGTH = 105

class MatchEngineV2(private val random: Random = Random) : MatchSimulator {

    private val behaviours = listOf(
        MovementBehaviour(),
        ShootingBehaviour(),
        /* TODO add passing, tackling, crossing behaviours */
    )

    override suspend fun play(match: Match): MatchResult {
        var state = initialState(match)
        val events = mutableListOf<MatchEvent>()

        for (i in 0 until SIMULATION_STEPS) {
            val minute = ceil(MINS_PER_STEP * i).toInt()
            state = simulateStep(minute, state, events)
        }

        val goals = events.mapNotNull { event -> event.data as? MatchEventData.Goal }
        val homeGoals = goals.count { event -> event.team == match.teams.first }
        val awayGoals = goals.count { event -> event.team == match.teams.second }

        return MatchResult(match.teams, homeGoals to awayGoals, events)
    }

    private suspend fun simulateStep(minute: Int, state: PitchState, output: MutableList<MatchEvent>): PitchState {
        return coroutineScope {
            /* The step is simulated by first letting each behaviour apply fixed mutations. Here ordering matters */
            val newState = behaviours.fold(state) { prev, behavior -> behavior.applyFixedMutations(prev) }

            /* Then each behaviour gets to propose the main event to happen this step. Behaviours are consulted in parallel */
            val actions = behaviours.map { behaviour ->
                async { behaviour.getWeightedActions(newState) }
            }.awaitAll().flatten()

            if (actions.isEmpty()) {
                output += MatchEvent(minute, MatchEventData.None)
                return@coroutineScope newState
            }

            /* A main action is picked and added to the output */
            val withWeights = actions.zip(actions.map(WeightedAction::getEventWeight))
            val weightSum = withWeights.sumOf { pair -> pair.second }
            var num = random.nextInt(max(MIN_WEIGHT_SUM, weightSum))
            if (weightSum < 1 || num > weightSum) {
                // no event this step!
                output += MatchEvent(minute, MatchEventData.None)
                return@coroutineScope newState
            }

            val pickedAction = withWeights.first { pair ->
                num -= pair.second
                num <= 0
            }.first

            val success = pickedAction.isSuccess(random)
            val event = pickedAction.getEventData(random, success)
            output += MatchEvent(minute, event)

            pickedAction.applyEvent(newState, event, success)
        }
    }

    /**
     * @return the initial pitch state. Home team gets the kickoff.
     */
    private fun initialState(match: Match): PitchState {
        return PitchBuilder().buildKickoffState(match.teams, match.teams.first)
    }

}
