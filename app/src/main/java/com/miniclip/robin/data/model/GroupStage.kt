package com.miniclip.robin.data.model

import com.miniclip.robin.simulation.model.Match

data class GroupStage(
    val name: String,
    val teams: List<Team>,
    val matches: List<Match>,
    val scores: List<GroupStageStats>,
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
