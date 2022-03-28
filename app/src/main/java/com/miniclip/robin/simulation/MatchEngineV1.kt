package com.miniclip.robin.simulation

import com.miniclip.robin.data.model.Player
import com.miniclip.robin.data.model.Team
import com.miniclip.robin.simulation.model.Match
import com.miniclip.robin.simulation.model.MatchResult
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Quick v1 demo engine.
 */
class MatchEngineV1(private val random: Random = Random) : MatchSimulator {

    /**
     * Amount of difference in quality that will cause the higher team to double it's chance to score compared to the opponent.
     */
    private val REFERENCE_QUALITY_DIFF = 14

    /**
     * Randomized result based on quality.
     * Events are not simulated. Do not use.
     */
    override suspend fun play(match: Match): MatchResult {
        val homeQuality = averageQuality(match.teams.first)
        val awayQuality = averageQuality(match.teams.second)

        val goalCount = random.nextDouble(0.75, 2.0).pow(3.0).toInt() // TODO base goal count also on quality difference

        // separate goals in home vs away
        val goalsByTeam = (0 until goalCount).groupBy {
            if (scoringValue(homeQuality, awayQuality) >= scoringValue(awayQuality, homeQuality)) "home" else "away"
        }

        return MatchResult(
            teams = match.teams,
            score = goalsByTeam["home"].orEmpty().size to goalsByTeam["away"].orEmpty().size,
            events = emptyList()
        )
    }

    private fun averageQuality(team: Team): Int {
        return (team.players.sumOf(Player::quality) / team.players.size.toFloat()).roundToInt()
    }

    private fun scoringValue(quality: Int, othersQuality: Int): Int {
        val minQuality = min(quality, othersQuality)
        val adjustment = min(0, REFERENCE_QUALITY_DIFF - minQuality)
        return random.nextInt(quality + adjustment)
    }

}