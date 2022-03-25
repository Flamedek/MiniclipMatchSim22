package com.miniclip.robin.data.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random


fun main() {
    val random = Random

    val teams = listOf(
        generateTeam(random, "Ajax", "icon_ajax", targetQuality = 85),
        generateTeam(random, "Sporting CP", "icon_sporting", targetQuality = 70),
        generateTeam(random, "Borussia Dortmund", "icon_dortmund", targetQuality = 80),
        generateTeam(random, "Beşiktaş", "icon_besiktas", targetQuality = 60),
    )

    val prettyJson = Json {
        prettyPrint = true
    }

    try {
        val json = prettyJson.encodeToString(teams)
        val file = File("./app/src/main/assets/teams.json")
        file.writeText(json)
        println("Teams written to assets/teams.json!")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun generateTeam(random: Random, name: String, icon: String, targetQuality: Int): Team {
    val id = UUID.randomUUID().toString()
    val formations = TeamFormation.values()
    val formation = formations[random.nextInt(formations.size)]

    val roles = buildList(11) {
        add(PlayerRole.GOALIE)
        repeat(formation.defenders) {
            add(PlayerRole.DEFENCE)
        }
        repeat(formation.midfielders) {
            add(PlayerRole.MIDFIELD)
        }
        repeat(formation.attackers) {
            add(PlayerRole.ATTACK)
        }
    }

    val players = roles.map { role ->
        generatePlayer(random, role, targetQuality)
    }

    return Team(id, name, icon, players, formation)
}

private fun generatePlayer(random: Random, role: PlayerRole, targetQuality: Int): Player {
    val id = UUID.randomUUID().toString()
    val name = ""

    val qualityOffset = random.nextDouble(-2.5, 2.5).pow(3.0)
    val quality = (targetQuality + qualityOffset).roundToInt()

    return Player(id, name, role, quality)
}