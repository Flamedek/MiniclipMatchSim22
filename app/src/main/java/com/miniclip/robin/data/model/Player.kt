package com.miniclip.robin.data.model

import kotlinx.serialization.Serializable

@Serializable
class Player(
    val id: String,
    val name: String,
    val role: PlayerRole,
    val quality: Int
)

@Serializable
enum class PlayerRole {
    ATTACK, MIDFIELD, DEFENCE, GOALIE
}
