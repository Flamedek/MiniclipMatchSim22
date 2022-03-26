package com.miniclip.robin.data.model

data class Match(
    val teams: Pair<Team, Team>,
    val result: MatchResult? = null,
)

class MatchResult(
    val teams: Pair<Team, Team>,
    val score: Pair<Int, Int>,
    val events: List<MatchEvent>,
) {
    val winner
        get() = when {
            score.first > score.second -> teams.first
            score.second > score.first -> teams.second
            else -> null
        }
}

class MatchEvent(
    val minute: Int,
    val data: MatchEventData,
)

sealed class MatchEventData {
    object None : MatchEventData()
    object Halftime : MatchEventData()
    object End : MatchEventData()

    class Shot : MatchEventData()
    class Pass : MatchEventData()
    class Tackle : MatchEventData()
    class Movement : MatchEventData()

    class Card : MatchEventData()
    class FreeKick : MatchEventData()
    class Penalty : MatchEventData()
}