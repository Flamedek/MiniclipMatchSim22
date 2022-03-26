package com.miniclip.robin.simulation

import com.miniclip.robin.data.model.Match
import com.miniclip.robin.data.model.MatchResult


interface MatchSimulator {

    suspend fun play(match: Match): MatchResult

}