package me.codedbyyou.os.server.enums.impl

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.core.models.GameRoomInfo
import me.codedbyyou.os.server.enums.Game
import me.codedbyyou.os.server.player.GamePlayer
import kotlin.math.abs

/**
 * Each game is made up of multiple rounds. In each round, every player participating in
 * that game selects an integer between 0 and 100 (inclusive) within a given time frame
 * and share with the game server.
 * • The winner of a round is the player whose selection is closest to two-thirds of the
 * average of all numbers chosen by all players for that round. For example, if four
 * players select 0, 50, 67, and 100, the player with 50 wins the round, as 50 is the
 * closest to two-thirds of the average (36.1667). There can be multiple winners per
 * round, for instance, if the numbers chosen identical.
 * • Each player starts with 5 points and loses a single point per losing round. A player is
 * eliminated from the game when they run out of points. After each round, the
 * following information is announced to all players: the round number, the players in
 * the game, the numbers chosen, the remaining points for each player, the outcome
 * (winner or loser) of that round, and which players have been eliminated, if any. The
 * game is won by the last remaining player.
 * • There is one additional rule to enhance gameplay and discourage players from
 * selecting 0 all the time: In the last round, when there are two players left in the game
 * and when one chooses 0, the other player will win the game (guess > 0).
 */
class GameRoom(
    override val roomName: String,
    override val roomNumber: Int,
    override val roomDescription: String,
    override val roundsNumber: Int,
    override var roomStatus: RoomStatus,
    override val roomVersion: String,
    override val roomMaxPlayers: Int,
    override val roomMinPlayers: Int,
    override val roomPlayers: MutableList<Player>,
    override var roomPlayerCount: Int
) : Game {

    override val roundResults:  MutableMap<Player, MutableList<Int>> = mutableMapOf()
    override val roundWinners:  MutableList<Player> = mutableListOf()
    override val spectators:    MutableList<Player> = mutableListOf()
    private  var currentRound   = 0
    private  val currentGuesses = mutableMapOf<Player, Int>()

    fun isFull(): Boolean {
        return roomPlayerCount == roomMaxPlayers
    }

    fun turnToSpectator(player: Player) {
        roomPlayers.remove(player)
        spectators.add(player)
    }

    fun addPlayer(player: Player) {
        roomPlayers.add(player)
        roomPlayerCount++
    }


    override suspend fun start() {
        coroutineScope {
            launch {
                roomStatus = RoomStatus.STARTING
                repeat(3) {
                    roomPlayers.forEach { player ->
                        player.sendMessage("Game starting in ${3 - it} seconds")
                    }
                    Thread.sleep(1000)
                }
                roomPlayers.forEach { player ->
                    // send packet of game start
                    player as GamePlayer
                    player.sendMessage("Game has started")
                    player.addPacket(PacketType.GAME_START.toPacket())
                    player.sendActionBar("Round 1")
                    // send packet of round start
                    player.addPacket(PacketType.GAME_ROUND_START.toPacket())
                }
                roomStatus = RoomStatus.STARTED
            }
        }
    }

    fun guess(player: Player, guess: Int) {
        if (player in spectators) {
            player.sendMessage("You are a spectator")
            return
        }

        if (currentGuesses.containsKey(player)) {
            player.sendMessage("You have already guessed for this round")
            return
        }
        currentGuesses[player] = guess
        if (currentGuesses.size == (roomPlayers.size-spectators.size)) {
            endRound()
        }
    }

    private fun endRound() {
        currentRound++
        val guesses = currentGuesses.values
        val average = guesses.sum() / guesses.size
        val twoThirds = (average * 2) / 3
        val closest = guesses.minByOrNull { abs(it - twoThirds) }!!
        val winners = currentGuesses.filter { it.value == closest }.keys
        winners.forEach { player ->
            player.sendMessage("You have won the round")
            player as GamePlayer
            player.addPacket(PacketType.GAME_ROUND_END.toPacket())
            roundWinners.add(player)
            roundResults[player]?.add(5) ?: run {
                roundResults[player] = mutableListOf(5)
            }
        }
        (roomPlayers.toSet() - winners.toSet()).forEach { player ->
            player.sendMessage("You have lost the round")
            player as GamePlayer
            player.addPacket(PacketType.GAME_PLAYER_LOSE.toPacket(mapOf("type" to "round")))
            roundResults[player]?.add(4) ?: run {
                roundResults[player] = mutableListOf(4)
            }
        }
        if (currentRound == roundsNumber) {
            end()
        } else {
            roomPlayers.forEach { player ->
                player.sendMessage("Round $currentRound")
                player.sendMessage("Average: $average")
                player.sendMessage("Two Thirds: $twoThirds")
                player.sendMessage("Closest: $closest")
                player.sendMessage("Winners: ${winners.joinToString { it.uniqueName }}")
                player as GamePlayer
                player.addPacket(PacketType.GAME_ROUND_INFO.toPacket(mapOf(
                    "round" to currentRound,
                    "average" to average,
                    "two thirds" to twoThirds,
                    "closest" to closest,
                    "winners" to winners.joinToString { it.uniqueName }
                )))
            }
            currentGuesses.clear()
        }
    }

    fun leaderboard() {
        val sorted = roundResults.toList().sortedByDescending { it.second.sum() }
        sorted.forEachIndexed { index, (player, scores) ->
            if (!player.isOnline)
                return
            player.sendMessage("Leaderboard")
            player.sendMessage("Position: ${index + 1}")
            player.sendMessage("Player: ${player.uniqueName}")
            player.sendMessage("Scores: ${scores.sum()}")
            player as GamePlayer
            player.addPacket(PacketType.GAME_PLAYER_INFO.toPacket(mapOf(
                "position" to (index+1),
                "player" to player.uniqueName,
                "scores" to scores.sum()
            )))
        }
    }

    override fun hasStarted(): Boolean {
        return roomStatus == RoomStatus.STARTED || roomStatus == RoomStatus.STARTING || hasEnded()
    }

    override fun hasEnded(): Boolean {
        return roomStatus == RoomStatus.ENDED
    }

    override fun end() {
        roomStatus = RoomStatus.ENDED
        // there can be multiple winners, if they are tied in rounds won
        val winners = roundWinners.groupBy { it }.maxByOrNull { it.value.size }!!.value
        winners?.forEach { player ->
            player.sendMessage("You have won the game")
            player as GamePlayer
            player.addPacket(PacketType.GAME_PLAYER_WIN.toPacket())
            player.addPacket(PacketType.GAME_END.toPacket())
        }
        (roomPlayers.toSet() - winners.toSet()).forEach { player ->
            player.sendMessage("You have lost the game")
            player as GamePlayer
            player.addPacket(PacketType.GAME_PLAYER_LOSE.toPacket())
            player.sendActionBar("Game Over")
            player.addPacket(PacketType.GAME_END.toPacket())
        }
    }
}



/**
 * Extension function to convert a GameRoom object to a GameRoomInfo object.
 */
fun GameRoom.toGameRoomInfo() = GameRoomInfo(
    roomName,
    roomNumber,
    roomDescription,
    roundsNumber,
    roomStatus,
    roomMaxPlayers,
    roomPlayerCount,
    roomPlayers.map { it.uniqueName }
)