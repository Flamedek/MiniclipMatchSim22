package com.miniclip.robin.simulation.behaviour

import com.miniclip.robin.simulation.model.MatchEventData
import com.miniclip.robin.simulation.model.PitchState

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
     * Out of all the behaviours, a single action is picked based on it's weight and then applied.
     * @see [WeightedAction]
     */
    fun getWeightedActions(state: PitchState): List<WeightedAction> = emptyList()

}

/**
 * Represents a possible main event to happen in a single simulation step.
 */
interface WeightedAction {

    companion object {
        /** Default weight for an event */
        const val WEIGHT_NORMAL = 10
        /** Minimal weight sum used to avoid low weighted actions still being common */
        const val MIN_WEIGHT_SUM = 50
    }

    /**
     * Get the relative chance this action will happen among all possible actions this step.
     * A target value for a reasonable chance should be [WEIGHT_NORMAL], rare events can have a lower weight or very common events higher.
     */
    fun getEventWeight(): Int

    /**
     * Called when this event is picked and is being executed.
     * Create the [MatchEventData] for this event.
     */
    fun getEventData(): MatchEventData

    /**
     * Always called after [getEventData]. Chance to update the state to 'apply' the event.
     * Note that state is immutable. You may create and return a copy with the applied changes.
     */
    fun applyEvent(state: PitchState, event: MatchEventData): PitchState

}