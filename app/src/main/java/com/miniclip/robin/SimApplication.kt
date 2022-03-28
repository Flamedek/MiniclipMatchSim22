package com.miniclip.robin

import android.app.Application
import com.miniclip.robin.data.TeamsRepository
import com.miniclip.robin.data.TournamentRepository
import com.miniclip.robin.data.jsonDataSource
import com.miniclip.robin.data.model.Team
import com.miniclip.robin.simulation.MatchEngineV2
import kotlinx.serialization.ExperimentalSerializationApi

class SimApplication : Application() {

    lateinit var teamsRepository: TeamsRepository
        private set

    lateinit var tournamentRepository: TournamentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        initDependencies()
    }

    @ExperimentalSerializationApi
    private fun initDependencies() {
        val teamDataSource = jsonDataSource<List<Team>> {
            assets.open("teams.json")
        }
        teamsRepository = TeamsRepository(teamDataSource)

        val matchSimulator = MatchEngineV2()
        tournamentRepository = TournamentRepository(teamsRepository, matchSimulator)
    }

}