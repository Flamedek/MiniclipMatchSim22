package com.miniclip.robin.data

import com.miniclip.robin.data.model.Team
import com.miniclip.robin.simulation.MatchSimulator
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExperimentalSerializationApi
@ExtendWith(MockKExtension::class)
internal class TournamentRepositoryTest {

    private lateinit var repository: TournamentRepository

    @BeforeEach
    fun setUp() {
        repository = createRepository()
    }

    @Test
    fun testMatchGrouping() = runBlocking {
        val stage = repository.getCurrentGroupStage()

        val teamCount = stage.teams.size
        val matchesPerTeam = teamCount - 1
        val totalMatchCount =  (teamCount * (matchesPerTeam)) / 2

        // stage is correctly filled
        assertEquals(teamCount, stage.teams.size, "Team count")
        assertEquals(totalMatchCount, stage.matches.size, "Match count")

        // each team plays enough games
        assertAll(stage.teams.map { team ->
            {
                assertEquals(matchesPerTeam, stage.matches.count { match ->
                    match.teams.first == team || match.teams.second == team
                }, "Amount of matched played per team")
            }
        })

        // team never plays itself
        assertFalse(stage.matches.any { match ->
            match.teams.first == match.teams.second
        })

        // unique opponent for every match per team
        stage.teams.forEach { team ->
            val opponents = HashSet<Team>(stage.teams.size)
            stage.matches.forEach { match ->
                if (match.teams.first == team) {
                    opponents += match.teams.second
                } else if (match.teams.second == team) {
                    opponents += match.teams.first
                }
            }
            assertEquals(matchesPerTeam, opponents.size, "Expected 3 unique opponents for team ${team.name}. Got: ${opponents.map(Team::name)}")
        }
    }
}

private fun createRepository(): TournamentRepository {
    val file = File("./src/main/assets/teams.json")
    val teamDataSource = jsonDataSource<List<Team>> {
        file.inputStream().buffered()
    }

    val teamsRepository = TeamsRepository(teamDataSource)
    val simulator = mockk<MatchSimulator>()
    return TournamentRepository(teamsRepository, simulator)
}