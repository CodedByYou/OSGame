package me.codedbyyou.os.server.enums.impl

import kotlinx.coroutines.*
import me.codedbyyou.os.core.enums.RoomStatus
import me.codedbyyou.os.core.interfaces.player.Player
import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.core.models.GameRoomInfo
import me.codedbyyou.os.core.models.Title
import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.enums.Game
import me.codedbyyou.os.server.player.GamePlayer
import java.lang.Thread.sleep
import java.util.concurrent.Executors
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
    roomPlayerCount: Int,
    roomChancesCount: Int,
) : Game {

    override val roundResults:  MutableMap<Player, MutableList<Int>> = mutableMapOf()
    override val roundWinners:  MutableList<Player> = mutableListOf()
    override val spectators:    MutableList<Player> = mutableListOf()
    private  var currentRound   = 0
    private  val currentGuesses = mutableMapOf<Player, Int>()
    private  val roomPlayerChances = mutableMapOf<Player, Int>()
    private val roomChances = roomChancesCount
    override var roomPlayerCount: Int = roomPlayerCount
        get() = roomPlayers.size + spectators.size
    /**
     * Checks if the room is full.
     */
    fun isFull(): Boolean {
        return roomPlayers.size == roomMaxPlayers
    }

    fun forceStart() {
        if (roomPlayers.size >= roomMinPlayers) {
            GlobalScope.launch {
                start()
            }
        }
    }

    /**
     * Checks if the room is empty.
     */
    fun turnToSpectator(player: Player) {
        roomPlayers.remove(player)
        spectators.add(player)
    }

    /**
     * Adds a player to the room.
     * This function will add a player to the room.
     * It will send a title message to the player that they have joined the room.
     * @param player the player to add to the room.
     */
    fun addPlayer(player: Player) {
        roomPlayers.add(player)
        roomPlayerChances[player] = roomChances
        player as GamePlayer
        player.sendTitle("Welcome to the game user", "Room: $roomName", 1f)
        GlobalScope.launch {
            roomPlayers.forEach { roomPlayer ->
                Executors.newSingleThreadExecutor().execute {
                    if (roomPlayer == player)
                        sleep(500)
                    roomPlayer.sendMessage("Player ${player.uniqueName} has joined the room")
                }
            }
        }
    }

    /**
     * Removes a player from the room.
     * This function will remove a player from the room.
     * It will send a title message to the player that they have left the room.
     * @param player the player to remove from the room.
     * @see GamePlayer
     */
    fun removePlayer(player: Player, sendTitles : Boolean = true) {
        roomPlayers.remove(player)
        spectators.remove(player)
        player as GamePlayer
        if (sendTitles)
            player.sendTitle("You have left the room", "Goodbye!", 1f)

        GlobalScope.launch {
            roomPlayers.forEach { roomPlayer ->
                Executors.newSingleThreadExecutor().execute {
                    roomPlayer.sendMessage("Player ${player.uniqueName} has left the room")
                }
            }
            spectators.forEach() { spectator ->
                spectator.sendMessage("Player ${player.uniqueName} has left the room")
            }
            delay(500)
            if (roomPlayers.size == 1 && roomStatus == RoomStatus.STARTED) {
                end()
                roomStatus = RoomStatus.NOT_STARTED
                return@launch
            }
            if (roomPlayers.isEmpty()) {
                roomStatus = RoomStatus.NOT_STARTED
            }
        }

    }

    /**
     * Starts the game.
     * This function will send a message to all players in the room that the game is starting.
     * It will start a countdown of 3 seconds before the game starts where it will send a message to all room players.
     * After the countdown, it will send a message to all room players that the game has started.
     * It will then send a packet to all room players that the game has started.
     * it will change the room status to STARTED.
     * It will then start the first round.
     * @see RoomStatus
     * @see GamePlayer
     */
    override suspend fun start() {
            GlobalScope.launch {
                roomStatus = RoomStatus.STARTING
                repeat(3) {
                    roomPlayers.forEach { player ->
                        player.sendTitle("Game starting in ${3 - it} seconds", "Be ready!",1f)
                    }
                    delay(1000)
                }
                delay(1000)
                roomPlayers.forEach { player ->
                    Executors.newSingleThreadExecutor().execute {
                        player as GamePlayer
                        player.sendMessage("Game has started")
                        player.addPacket(PacketType.GAME_START.toPacket())
                        sleep(500)
                        player.sendTitle("Game has started", "Good luck!", 1f)
                        println("Game has started was sent to ${player.uniqueName}")
                        sleep(1000)
                        player.addPacket(
                            PacketType.GAME_ROUND_START
                                .toPacket(
                                    mapOf(
                                        "data" to currentRound.toString() + ":"+ roomPlayerChances.getOrDefault(player!!, 0).toString(),
                                    )
                                )
                        )
                        println("Round has started was sent to ${player.uniqueName}")
                    }
                }
                roomStatus = RoomStatus.STARTED
            }
    }

    /**
     * the method to register a player's guess for the current round.
     * if the player is a spectator, it will send a message to the player that they are a spectator.
     * if the player has already guessed for this round, it will send a message to the player that they have already guessed.
     * if all players have guessed for this round, it will end the round and calculate all results.
     * @param player the player who is guessing
     * @param guess the guess of the player
     * @see endRound
     * @see Player
     */
    fun guess(player: Player, guess: Int) {
        if (player in spectators) {
            player.sendMessage("You are a spectator")
            return
        }

        if (currentGuesses.containsKey(player)) {
            player.sendMessage("You have already guessed for this round")
            return
        }
        println("Player ${player.uniqueName} guessed $guess")

        currentGuesses[player] = guess
        println("Current guesses size: ${currentGuesses.size}")
        println("Room players size: ${roomPlayers.size}")
        println("Spectators size: ${spectators.size}")
        println("Room player chances size: ${roomPlayerChances.size}")
        println((roomPlayers.size-spectators.size) == currentGuesses.size)
        if (currentGuesses.size == (roomPlayers.size-spectators.size)) {
            endRound()
        }
    }


    /**
     * Ends the current round.
     * This function will calculate the average of all guesses for the current round.
     * It will then calculate two-thirds of the average.
     * It will then find the closest guess to two-thirds of the average.
     * It will then find the winners of the round.
     * It will then send a message to all room players with the round information.
     * It will then send a packet to all room players with the round information.
     * It will then clear the current guesses.
     * If the current round is the last round, it will end the game.
     * @see GamePlayer
     * @see PacketType
     * @see end
     */
    private fun endRound() {
        currentRound++
        val guesses = currentGuesses.values
        val average = guesses.sum() / guesses.size
        val twoThirds = (average * 2) / 3
        val closest = guesses.minByOrNull { abs(it - twoThirds) }!!
        val winners = currentGuesses.filter { it.value == closest }.keys


        Executors.newSingleThreadExecutor().execute {
            val winnerJobs = winners.map { player ->
                Executors.newSingleThreadExecutor().execute {
                    player as GamePlayer
                    player.sendMessage("You have won the round")
                    player.addPacket(PacketType.GAME_ROUND_END.toPacket())
                    sleep(1000)
                    player.addPacket(PacketType.GAME_PLAYER_WIN.toPacket(mapOf("type" to "round")))
                    player.sendTitle("You have won the round", "Congratulations", 1f)
                    roundWinners.add(player)
                    roundResults[player]?.add(5) ?: run {
                        roundResults[player] = mutableListOf(5)
                    }
                }
            }

            val loserJobs = (roomPlayers.toSet() - winners.toSet()).map { player ->
                Executors.newSingleThreadExecutor().execute {
                    player.sendMessage("You have lost the round")
                    player as GamePlayer
                    player.addPacket(PacketType.GAME_ROUND_END.toPacket(mapOf("type" to "round")))
                    sleep(500)
                    player.sendTitle("You have lost the round", "Better luck next time", 1f)

                    roundResults[player]?.add(4) ?: run {
                        roundResults[player] = mutableListOf(4)
                    }
                    roomPlayerChances[player] = roomPlayerChances[player]!!.dec()
                    if (roomPlayerChances[player] == 0) {
                        sleep(500)
                        player.sendTitle("You have been eliminated", "You have run out of chances", 1f)
//                        player.addPacket(PacketType.GAME_PLAYER_LOSE.toPacket(mapOf("type" to "game")))
                        turnToSpectator(player)
                    }
                }
            }

            sleep(1500)

            if (currentRound == roundsNumber) {
                end()
            } else {
                currentGuesses.clear()
                roomPlayers.forEach { player ->
                    Executors.newSingleThreadExecutor().execute {
                        sleep(1000)
                        player as GamePlayer
                        player.addPacket(
                            PacketType.GAME_ROUND_START
                                .toPacket(
                                    mapOf(
                                        "data" to currentRound.toString() + ":"+ roomPlayerChances.getOrDefault(player!!, 0).toString(),
                                    )
                                )
                        )
                    }
                }
            }
        }
    }

    /**
     * Displays the leaderboard of the game.
     * This function will sort the players by their scores and display the leaderboard.
     * It will send a message to each player with their position, name, and scores.
     * Currently, it is sending a message to each player with their position, name, and scores.
     */
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

    /**
     * Checks if the game has started.
     * @return true if the game has started, false otherwise.
     */
    override fun hasStarted(): Boolean {
        return roomStatus == RoomStatus.STARTED || roomStatus == RoomStatus.STARTING || hasEnded()
    }

    /**
     * Checks if the game has ended.
     * @return true if the game has ended, false otherwise.
     */
    override fun hasEnded(): Boolean {
        return roomStatus == RoomStatus.ENDED
    }

    /**
     * Ends the game.
     * This function will send a message to all room players that the game has ended.
     * It will then send a packet to all room players that the game has ended.
     * It will then change the room status to ENDED.
     * It will then send a message to the winners that they have won the game.
     * It will then send a packet to the winners that they have won the game.
     * It will then send a message to the losers that they have lost the game.
     * It will then send a packet to the losers that they have lost the game.
     * @see GamePlayer
     * @see PacketType
     */
    override fun end() {
        roomStatus = RoomStatus.ENDED
        val sorted = roundResults.toList().sortedByDescending { it.second.sum() }
        Server.updateLeaderboard(sorted.map { it.first.uniqueName to it.second.sum() })
        val leaderboard = sorted.joinToString { "${it.first.uniqueName}:${it.second.sum()}" }
        Executors.newSingleThreadExecutor().execute {
            val winners = roundWinners.groupBy { it }.maxByOrNull { it.value.size }!!.value

            // Launching coroutines for winners
            winners.forEach { player ->
                Executors.newSingleThreadExecutor().execute {
                    player.sendMessage("You have won the game")
                    sleep(500)
                    player as GamePlayer
                    player.sendTitle("Game Over", "You have won the game, legend!", 1f)
                    player.addPacket(PacketType.GAME_PLAYER_WIN.toPacket())
                    sleep(1000)
                    player.addPacket(PacketType.GAME_END.toPacket(
                        mapOf(
                            "leaderboard" to leaderboard
                        )
                    ))
                }
            }

            // Sending losing packets
            val losers = roomPlayers.toSet() - winners.toSet()
            losers.forEach { player ->
                Executors.newSingleThreadExecutor().execute {
                    player.sendMessage("You have lost the game")
                    player as GamePlayer
                    sleep(500)
                    player.sendTitle("Game Over", "You have lost the game", 1f)
                    sleep(1000)
                    player.addPacket(PacketType.GAME_END.toPacket(
                        mapOf(
                            "leaderboard" to leaderboard
                        )
                    ))
                }
            }

            sleep(2000)
            // Clearing players and spectators after all coroutines are completed
            roomPlayers.clear()
            spectators.clear()
            currentRound = 0
            currentGuesses.clear()
            roundResults.clear()
            roundWinners.clear()
            sleep(4000)
            roomStatus = RoomStatus.NOT_STARTED
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