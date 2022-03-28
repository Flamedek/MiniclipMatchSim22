package com.miniclip.robin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val id: String,
    val name: String,
    val icon: String,
    val players: List<Player>,
    val formation: TeamFormation
)

@Serializable
enum class TeamFormation(
    val displayName: String,
    val positionData: String,
    val defenders: Int,
    val midfielders: Int,
    val attackers: Int
) {
    FORMATION_4_3_3("4-3-3", "4d-3m-3a", 4, 3, 3),
    FORMATION_4_2_3_1("4-2-3-1", "4d-2m-3a-1a", 4, 2, 4),
    FORMATION_5_3_2("5-3-2", "5d-3m-2a", 5, 3, 2);
}
