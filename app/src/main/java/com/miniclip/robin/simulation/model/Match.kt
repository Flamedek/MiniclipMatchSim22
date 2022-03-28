package com.miniclip.robin.simulation.model

import com.miniclip.robin.data.model.Player
import com.miniclip.robin.data.model.Team

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

sealed interface MatchEventData {
    /** Represents no noteworthy event, just in increase in time. */
    object None : MatchEventData
    /** Match half time */
    object Halftime : MatchEventData
    /** Match end */
    object End : MatchEventData

    data class Goal(val team: Team, val player: Player, val type: GoalType) : MatchEventData

    data class SavedShot(val shooter: Player, val goalie: Player) : MatchEventData
    data class MissedShot(val shooter: Player) : MatchEventData

    // (potential more event types)
    class Pass : MatchEventData
    class Tackle : MatchEventData
    class Movement : MatchEventData

    class Card : MatchEventData
    class FreeKick : MatchEventData
    class Penalty : MatchEventData
}

enum class GoalType {
    SHOT_DISTANCE,
    SHOT_PLACED_LOW,
    SHOT_PLACED_HIGH,
    SHOT_ROLLER,
    CROSS_HEADER,
    CROSS_TAP_IN,
    CROSS_VOLLEY,
    CROSS_BICYCLE,
    HAND_OF_GOD;
}