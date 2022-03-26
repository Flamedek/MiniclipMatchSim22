package com.miniclip.robin.simulation

import com.miniclip.robin.data.model.Match
import com.miniclip.robin.data.model.MatchResult
import com.miniclip.robin.data.model.Player
import com.miniclip.robin.data.model.Team
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Quick v1 demo engine.
 */
class MatchEngineV1 : MatchSimulator {

    private val random = Random

    /**
     * Randomized result based on quality.
     * Events are not simulated. Do not use.
     */
    override suspend fun play(match: Match): MatchResult {
        val homeQuality = averageQuality(match.teams.first)
        val awayQuality = averageQuality(match.teams.second)

        val goalCount = random.nextDouble(0.75, 2.0).pow(3.0).toInt()
        val byTeam = (0 until goalCount).groupBy {
            if (random.nextInt(homeQuality) >= random.nextInt(awayQuality)) "home" else "away"
        }

        return MatchResult(
            teams = match.teams,
            score = byTeam["home"].orEmpty().size to byTeam["away"].orEmpty().size,
            events = emptyList()
        )
    }

    private fun averageQuality(team: Team): Int {
        return (team.players.sumOf(Player::quality) / team.players.size.toFloat()).roundToInt()
    }

}