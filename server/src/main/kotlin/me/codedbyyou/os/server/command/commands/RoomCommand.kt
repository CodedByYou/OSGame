package me.codedbyyou.os.server.command.commands

import me.codedbyyou.os.core.interfaces.server.PacketType
import me.codedbyyou.os.core.interfaces.server.toPacket
import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.enums.impl.toGameRoomInfo
import me.codedbyyou.os.server.player.GamePlayer
import me.codedbyyou.os.server.player.manager.PlayerManager
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

@Command("room", "r")
@Description("A command to manage rooms and their settings.")
class RoomCommand : ICommand {

    @MainCommand
    @Description("Main command for room management.")
    @Permission("room")
    @Usage("/room")
    fun main(player: CommandSender) {
        player.sendMessage("Room command.")
    }

    @SubCommand("create", "c")
    @Description("Create a new room.")
    @Permission("room.create")
    @Usage("/room create <name> <minPlayers> <maxPlayers> <rounds> <chances>")
    fun createRoom(player: CommandSender, name: String, minPlayers: Int, maxPlayers: Int, rounds: Int, chances: Int) {
        Server.gameManager.addRoom(
            name,
            "Game Room",
            rounds,
            "4",
            maxPlayers,
            minPlayers,
            0,
            chances
        )
        val games = Server.gameManager.getRooms()
            .map { it.toGameRoomInfo() }
        player.sendMessage("Room created: $name")
        PlayerManager.getOnlinePlayers().forEach {
            Executors.newSingleThreadExecutor().submit {
                if (Server.gameManager.getRoomByPlayer(it.uniqueName) == null) {
                    it as GamePlayer
                    it.addPacket(
                        PacketType.GAMES_LIST.toPacket(
                            mapOf(
                            "games" to games
                            )
                        )
                    )
                }
            }
        }

    }
    @SubCommand("generate", "g")
    @Description("Create a new room.")
    @Permission("room.create")
    @Usage("/room generate <name>")
    fun createDefaultRoom(player: CommandSender){
        createRoom(player, "Room"+ThreadLocalRandom.current().nextInt(1000), 2, 4, 3, 5)
    }


    @SubCommand("delete", "d")
    @Description("Delete a room.")
    @Permission("room.delete")
    @Usage("/room delete <id>")
    fun deleteRoom(sender: CommandSender, id: Int) {
        Server.gameManager.removeRoom(id)
        sender.sendMessage("Room deleted: $id")
    }

    @SubCommand("join", "j")
    @Description("Join a room.")
    @Permission("room.join")
    @Usage("/room join <id>")
    fun joinRoom(player: GamePlayer, id: Int) {
        val room = Server.gameManager.getRoom(id)
        if (room == null) {
            player.sendMessage("Room not found.")
            return
        }
        room.addPlayer(player)
        player.sendMessage("Joined room: ${room.roomName}")
    }

    @SubCommand("list", "l")
    @Description("List all rooms.")
    @Permission("room.list")
    @Usage("/room list")
    fun listRooms(sender: CommandSender) {
        val rooms = Server.gameManager.getRooms()
        sender.sendMessage("Rooms:")
        rooms.forEach {
            sender.sendMessage("${it.roomNumber} - ${it.roomName} - ${it.roomStatus} - ${it.roomPlayerCount}/${it.roomMaxPlayers} players")
        }
    }

    @SubCommand("clear", "cl")
    @Description("Clears all rooms.")
    @Permission("room.clear")
    @Usage("/room clear")
    fun clearRooms(sender: CommandSender) {
        Server.gameManager.clearRooms()
        Server.gameManager.addRoom("Default Room", "Game Room", 5, "4", 10, 2, 0, 3)
        sender.sendMessage("Rooms cleared, default room created.")
    }

    @SubCommand("leaderboard", "lb")
    @Description("Shows the leaderboard of the room that the player is in")
    @Permission("room.leaderboard")
    @Usage("/room leaderboard")
    fun leaderboard(player: GamePlayer) {
        val room = Server.gameManager.getRoomByPlayer(player.uniqueName)
        if (room == null) {
            player.sendMessage("You are not in a room.")
            return
        }
        player.sendMessage("Leaderboard:")
        room.leaderboard()
    }

}