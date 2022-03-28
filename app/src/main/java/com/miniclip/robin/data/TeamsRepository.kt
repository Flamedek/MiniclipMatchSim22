package com.miniclip.robin.data

import com.miniclip.robin.data.model.Team
import kotlinx.coroutines.*

class TeamsRepository(private val dataSource: DataSource<List<Team>>) {

    private var teams: Deferred<List<Team>>? = null

    private val scope = CoroutineScope(SupervisorJob())

    /**
     * Loads the list of all teams from the current [dataSource].
     * It is safe to call this function multiple times, the source is only bothered once.
     */
    suspend fun getTeams(): List<Team> {
        teams?.let { return it.await() }

        val value = scope.async(Dispatchers.IO) {
            dataSource.getData()
        }
        teams = value
        return value.await()
    }

    /**
     * Convenience to find a team by its [guid].
     */
    suspend fun findTeamById(guid: String): Team? {
        return getTeams().find { team -> team.id == guid }
    }

}