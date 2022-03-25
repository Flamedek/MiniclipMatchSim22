package com.miniclip.robin

import android.app.Application
import com.miniclip.robin.data.TeamsRepository
import com.miniclip.robin.data.TournamentRepository
import com.miniclip.robin.data.jsonDataSource
import com.miniclip.robin.data.model.Team

class SimApplication : Application() {

    lateinit var teamsRepository: TeamsRepository
        private set

    lateinit var tournamentRepository: TournamentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        initDependencies()
    }

    private fun initDependencies() {
        val teamDataSource = jsonDataSource<List<Team>> {
            assets.open("teams.json")
        }
        teamsRepository = TeamsRepository(teamDataSource)

        tournamentRepository = TournamentRepository((teamsRepository))
    }

}