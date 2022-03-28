package com.miniclip.robin.data

import com.miniclip.robin.data.model.GroupStage
import com.miniclip.robin.data.model.GroupStageStats
import com.miniclip.robin.data.model.Team
import com.miniclip.robin.simulation.MatchSimulator
import com.miniclip.robin.simulation.model.Match
import com.miniclip.robin.util.extensions.contains
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.random.Random

class TournamentRepository(
    val teamsRepository: TeamsRepository,
    val simulator: MatchSimulator
) {

    /**
     * Demo-ish content. Create the initial group stage object.
     */
    suspend fun getFixedGroupStage(): GroupStage {
        val teams = teamsRepository.getTeams().take(4)
        return createGroupStage("F", teams)
    }

    /**
     * Picks four random teams to generate a group stage.
     */
    suspend fun getRandomGroupStage(random: Random = Random): GroupStage {
        val teams = teamsRepository.getTeams().shuffled(random).take(4)
        val name = Char('A'.code + random.nextInt(8)).toString()
        return createGroupStage(name, teams)
    }

    fun resetGroupStage(group: GroupStage): GroupStage {
        return createGroupStage(group.name, group.teams)
    }

    /**
     * Runs the match simulation for the next rounds of games.
     * If all matches have been played, this function returns the unchanged [group] value.
     * @return a new GroupStage with the played results and updated scores, or the given [group] if all games had already been played.
     */
    suspend fun simulateNextRound(group: GroupStage): GroupStage = coroutineScope {
        val roundSize = group.teams.size / 2
        val matchesToPlay = group.matches.dropWhile { match -> match.result != null }.take(roundSize)

        if (matchesToPlay.isEmpty()) {
            return@coroutineScope group
        }

        val results = LinkedList(matchesToPlay.map { match ->
            // simulate matches in parallel
            async(Dispatchers.Default) {
                simulator.play(match)
            }
        }.awaitAll())

        val matches = group.matches.map { match ->
            if (match in matchesToPlay) {
                match.copy(result = results.pollFirst())
            } else {
                match
            }
        }

        val scores = getSortedTeamStats(group.teams, matches)

        return@coroutineScope group.copy(
            matches = matches,
            scores = scores
        )
    }

    /**
     * Generates the current stats for this group and sorts according to official rules.
     * Specifically, the returned list is sorted by:
     * 1. points
     * 2. goals difference
     * 3. goals scored
     * 4. goals against
     * 5. mutual result
     */
    fun sortTeamStats(values: List<GroupStageStats>, matches: List<Match>): List<GroupStageStats> {
        return values.sortedWith(
            compareByDescending<GroupStageStats> { it.points }
                .thenByDescending { it.goalsDifference }
                .thenByDescending { it.goalsScored }
                .thenBy { it.goalsAgainst }
                .thenDescending { t1, t2 ->
                    val match = matches.find { match -> t1 in match.teams && t2 in match.teams }
                    when (match?.result?.winner) {
                        t1.team -> 1
                        t2.team -> -1
                        else -> 0
                    }
                }
        )
    }

    /**
     * Creates a [GroupStageStats] object representing the given team based on given matches.
     */
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

    private fun createGroupStage(name: String, teams: List<Team>): GroupStage {
        require(teams.size == 4) { "Group stage must have 4 teams. Got: ${teams.size}" }

        val matches = generateEmptyMatches(teams)
        val scores = getSortedTeamStats(teams, matches)
        return GroupStage(name, teams, matches, scores)
    }

    private fun getSortedTeamStats(teams: List<Team>, matches: List<Match>) =
        sortTeamStats(teams.map { team -> calculateTeamStats(team, matches) }, matches)

    private fun generateEmptyMatches(teams: List<Team>): List<Match> {
        require(teams.size == 4) { "Need 4 teams per group, got: ${teams.size}" }

        return buildList {
            add(Match(teams[0] to teams[1]))
            add(Match(teams[2] to teams[3]))
            add(Match(teams[0] to teams[2]))
            add(Match(teams[1] to teams[3]))
            add(Match(teams[0] to teams[3]))
            add(Match(teams[1] to teams[2]))
        }
    }

}