package com.miniclip.robin.simulation.behaviour

import com.miniclip.robin.simulation.model.MatchEventData
import com.miniclip.robin.simulation.model.PitchState
import kotlin.random.Random

/**
 * A GameBehaviour encapsulates any specific set of events that can happen in a game
 */
interface GameBehaviour {

    /**
     * This is ran every simulation step and can be used to update the pitch state.
     * For example players move towards their target positions.
     * Mutations are applied sequentially, so each behaviour sees the state of the behaviour before it.
     * Note that state is immutable. You may create and return a copy with the applied changes.
     */
    fun applyFixedMutations(state: PitchState): PitchState = state

    /**
     * Generate _possible_ actions to happen this simulation step.
     * Out of all the behaviours, a single action is picked and applied based on it's weight..
     * @see [WeightedAction]
     */
    fun getWeightedActions(state: PitchState): List<WeightedAction> = emptyList()

}

interface WeightedAction {

    companion object {
        /** Default weight for an event */
        const val WEIGHT_NORMAL = 10

        const val MIN_WEIGHT_SUM = 50
    }

    /**
     * Get the relative chance this action will happen among all possible actions this step.
     * A target value for a reasonable chance should be [WEIGHT_NORMAL], rare events can have a lower weight or very common events higher.
     */
    fun getEventWeight(): Int

    /**
     * Called when this event is picked and is being executed.
     * Implementations can do a success calculation based on rng and relevant circumstances and player stats.
     * @return true if the event is a success
     */
    fun isSuccess(random: Random): Boolean

    /**
     * Always called after [isSuccess]. Create the [MatchEventData] for this event.
     */
    fun getEventData(random: Random, success: Boolean): MatchEventData

    /**
     * Always called after [isSuccess] and [getEventData]. Chance to update the state to 'apply' the event.
     * Note that state is immutable. You may create and return a copy with the applied changes.
     */
    fun applyEvent(state: PitchState, event: MatchEventData, success: Boolean): PitchState

}