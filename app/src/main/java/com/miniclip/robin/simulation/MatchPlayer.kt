package com.miniclip.robin.simulation

import com.miniclip.robin.simulation.model.Match
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class MatchPlayer(val match: Match) {

    init {
        require(match.result != null) { "Cannot play a match with no result"}
    }

    /**
     * Returns a coroutine [kotlinx.coroutines.flow.Flow] that can be used to 'playback' events from the match.
     * The flow emits items such that the total match takes around [matchDuration] to complete (give or take for extra time minutes).
     * To change speeds you can disregard a current Flow and get a new one with a different duration, starting at the current time using [startingMinute].
     */
    suspend fun getEvents(matchDuration: Duration, startingMinute: Int = 0) = flow {
        val delayPerGameMinute = matchDuration.inWholeMilliseconds / 90L
        val events = match.result?.events.orEmpty()
            .dropWhile { event -> event.minute < startingMinute }

        val queue = ArrayDeque(events)
        var currentMinute = startingMinute

        while (queue.isNotEmpty()) {
            val event = queue.removeFirst()
            val delay = (event.minute - currentMinute) * delayPerGameMinute
            delay(delay)
            emit(event)
            currentMinute = event.minute
        }
    }

}