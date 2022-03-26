package com.miniclip.robin.data

import com.miniclip.robin.data.model.Team
import kotlinx.coroutines.*

class TeamsRepository(val dataSource: DataSource<List<Team>>) {

    private var teams: Deferred<List<Team>>? = null

    private val scope = CoroutineScope(SupervisorJob())

    suspend fun getTeams(): List<Team> {
        teams?.let { return it.await() }

        val value = scope.async(Dispatchers.IO) {
            dataSource.getData()
        }
        teams = value
        return value.await()
    }

    suspend fun findTeamById(guid: String): Team? {
        return getTeams().find { team -> team.id == guid }
    }

}