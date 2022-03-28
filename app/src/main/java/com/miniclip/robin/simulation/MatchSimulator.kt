package com.miniclip.robin.simulation

import com.miniclip.robin.simulation.model.Match
import com.miniclip.robin.simulation.model.MatchResult


interface MatchSimulator {

    /**
     * Simulate a result for the given match.
     */
    suspend fun play(match: Match): MatchResult

}