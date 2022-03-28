package com.miniclip.robin.simulation.model

import com.miniclip.robin.data.model.Player
import com.miniclip.robin.data.model.Team
import com.miniclip.robin.util.Vec2

data class PitchState(
    val homePlayers: List<PositionedPlayer>,
    val awayPlayers: List<PositionedPlayer>,
    val ballPosition: Vec2,
    val possession: PositionedPlayer?
)

data class PositionedPlayer(
    val team: Team,
    val data: Player,
    var position: Vec2,
)

