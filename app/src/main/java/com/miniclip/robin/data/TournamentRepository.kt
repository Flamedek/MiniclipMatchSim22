package com.miniclip.robin.data

import com.miniclip.robin.data.model.GroupStage
import com.miniclip.robin.data.model.GroupStageStats
import com.miniclip.robin.data.model.Match
import com.miniclip.robin.data.model.Team

class TournamentRepository(
    val teamsRepository: TeamsRepository
) {

    suspend fun getCurrentGroupStage(): GroupStage {
        val group = "F"
        val teams = teamsRepository.getTeams().take(6)
        val matches = generateEmptyMatches(teams)
        return GroupStage(group, teams, matches)
    }

    fun calculateTeamStats(team: Team, matches: List<Match>): GroupStageStats {
        val results = matches
            .filter { match -> match.teams.first == team || match.teams.second == team }
            .mapNotNull(Match::result)

        return GroupStageStats(
            team = team,
            wins = results.count { it.winner == team },
            draws = results.count { it.winner == null },
            losses = results.count { it.winner != null && it.winner != team },
            goalsScored = results.sumOf { result -> if (result.teams.first == team) result.score.first else result.score.second },
            goalsAgainst = results.sumOf { result -> if (result.teams.first == team) result.score.second else result.score.first },
        )
    }

    private fun generateEmptyMatches(teams: List<Team>): List<Match> {
        require(teams.size >= 2) { "Need at least 2 teams to create matches, got: ${teams.size}" }
        require(teams.size % 2 == 0) { "Only even amount of teams per group is supported, got: ${teams.size}" }

        val availableOpponents = teams.associateWith { team -> teams.toMutableList().apply { remove(team) } }

        fun nextOpponent(team: Team): Team {
            val list = availableOpponents.getValue(team)
            return list.removeAt(0)
        }

        return buildList {
            repeat(teams.size - 1) { i ->
                repeat(teams.size / 2) { round ->
                    val homeIndex = when {
                        i == 0 && round == 0 -> 0
                        i == 0 && round == 1 -> 2
                        else -> round * 2
                    }

                    val home = teams[homeIndex]
                    val away = nextOpponent(teams[homeIndex])

                    add(Match(home to away))
                }
            }
        }
    }

}