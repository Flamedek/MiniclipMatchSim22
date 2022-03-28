package com.miniclip.robin.simulation

import com.miniclip.robin.data.model.Player
import com.miniclip.robin.data.model.PlayerRole
import com.miniclip.robin.data.model.Team
import com.miniclip.robin.simulation.model.PitchState
import com.miniclip.robin.simulation.model.PositionedPlayer
import com.miniclip.robin.util.Vec2

/**
 * Helper to position players on the pitch
 */
class PitchBuilder {

    fun buildKickoffState(teams: Pair<Team, Team>, startingTeam: Team): PitchState {
        // position players based on formation
        val center = Vec2(PITCH_WIDTH / 2, PITCH_LENGTH / 2)
        val homePlayers = positionFormation(teams.first)
        val awayPlayers = swapPitchSide(positionFormation(teams.second))

        // closest player to center does the kickoff
        val startingPlayers = if (startingTeam == teams.first) homePlayers else awayPlayers
        val starter = startingPlayers.findClosestToPoint(center)
        starter.position = center

        return PitchState(
            homePlayers = homePlayers,
            awayPlayers = awayPlayers,
            ballPosition = center,
            possession = starter
        )
    }

    /**
     * Places players spaced evenly on one side of the pitch according to their current formation.
     */
    fun positionFormation(team: Team): List<PositionedPlayer> {
        val lines = team.formation.positionData.split('-')
        val availablePlayers: Array<Player?> = team.players.toTypedArray()

        fun nextPlayerForRole(role: PlayerRole): Player {
            val index = availablePlayers.indexOfFirst { p -> p?.role == role }
            require(index >= 0) { "Invalid team. Not enough players with role $role." }
            return availablePlayers[index]!!.also { availablePlayers[index] = null }
        }

        return buildList {

            add(PositionedPlayer(team, nextPlayerForRole(PlayerRole.GOALIE), Vec2(PITCH_WIDTH / 2, 0)))

            for ((lineIndex, line) in lines.withIndex()) {
                val playerCount = line.first().toString().toInt()
                val requiredRole = when {
                    line.endsWith("d") -> PlayerRole.DEFENCE
                    line.endsWith("m") -> PlayerRole.MIDFIELD
                    line.endsWith("a") -> PlayerRole.ATTACK
                    else -> error("Invalid team formation. Unknown line value: '$line'")
                }
                for (playerIndex in 1..playerCount) {
                    val player = nextPlayerForRole(requiredRole)
                    val posX = PITCH_WIDTH / (playerCount + 1) * playerIndex
                    val posY = PITCH_LENGTH / 2 / (lines.size + 1) * lineIndex
                    add(PositionedPlayer(team, player, Vec2(posX, posY)))
                }
            }
        }
    }

    /**
     * Swaps the y-position of every player such that he moves to the other side of the field.
     */
    fun swapPitchSide(players: List<PositionedPlayer>): List<PositionedPlayer> {
        return players.map { player -> player.copy(position = Vec2(player.position.x, PITCH_LENGTH - player.position.y)) }
    }

}


/**
 * Finds the player closest to a given point
 * @throws IllegalStateException if list is empty.
 */
fun List<PositionedPlayer>.findClosestToPoint(point: Vec2) =
    minWithOrNull(compareBy { player -> player.position.distanceSquared(point) }) ?: error("List is empty")