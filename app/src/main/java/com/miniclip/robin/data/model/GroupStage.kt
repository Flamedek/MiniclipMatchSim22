package com.miniclip.robin.data.model


data class GroupStage(
    val name: String,
    val teams: List<Team>,
    val matches: List<Match>,
)

data class GroupStageStats(
    val team: Team,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsAgainst: Int,
) {
    val gamesPlayed get() = wins + draws + losses
    val goalsDifference get() = goalsScored - goalsAgainst
    val points get() = (wins * 3) + draws
}

data class Match(
    val teams: Pair<Team, Team>,
    var result: MatchResult? = null,
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
    val type: MatchEventType,
    val data: MatchEventData,
)

enum class MatchEventType {
    NONE, HALFTIME, PASS, SHOT, TACKLE, MOVE
}

interface MatchEventData