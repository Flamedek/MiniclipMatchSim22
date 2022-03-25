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
    val defenders: Int,
    val midfielders: Int,
    val attackers: Int
) {
    FORMATION_4_3_3("4-3-3", 4, 3, 3),
    FORMATION_4_2_3_1("4-2-3-1", 4, 2, 4),
    FORMATION_3_5_2("3-5-2", 3, 5, 2);
}
