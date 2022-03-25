package com.miniclip.robin.data

import com.miniclip.robin.data.model.Team
import kotlinx.coroutines.*

class TeamsRepository(val dataSource: DataSource<List<Team>>) {

    private var teams: Deferred<List<Team>>? = null

    private val scope = CoroutineScope(SupervisorJob())

    suspend fun getTeams(): List<Team> {
        teams?.let { return it.await() }

        return scope.async(Dispatchers.IO) {
            delay(200) // simulate potentially long running process
            dataSource.getData()
        }.also {
            teams = it
        }.await()
    }

    suspend fun findTeamById(guid: String): Team? {
        return getTeams().find { team -> team.id == guid }
    }

}