package com.miniclip.robin

import android.app.Application
import com.miniclip.robin.data.TeamsRepository
import com.miniclip.robin.data.TournamentRepository
import com.miniclip.robin.data.jsonDataSource
import com.miniclip.robin.data.model.Team
import com.miniclip.robin.simulation.MatchEngineV2
import kotlinx.serialization.ExperimentalSerializationApi

class SimApplication : Application() {

    /*
     * Manual dependency injection is set-up and retrieved from here, a global app scope.
     * If this grows it could be split into more classes/builders/factories or a service lookup
     * pattern could be implemented. Could also look at DI tools like dagger and hilt.
     */

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