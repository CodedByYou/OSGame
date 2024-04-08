package me.codedbyyou.os.core.interfaces.server

/**
 * PacketPrefix enum class
 * Shared between server and client to identify packet types, and to have a common ground for communication
 * less error-prone and more readable code, in the long run, to use enums instead of magic numbers
 * it is also easier to maintain and refactor code, it is placed in the core module to be shared between server and client
 * @param type Int the type of the packet
 * @author Abdollah Kandrani
 * @since 1.0
 * @version 1.0
 */
enum class PacketPrefix(val type: Int) {
    SERVER_INFO_PING(-1), SERVER_INFO_PONG(-2),
    MESSAGE(type = 2),  ACTION_BAR(3), TITLE(4), KICK(5),
    SERVER_CLIENT_CONNECT(6), SERVER_CLIENT_DISCONNECT(7), CLIENT_INFO(8),
    PLAYER_JOIN(9), PLAYER_LEAVE(10), PLAYER_INFO(11),
    GAME_START(12), GAME_ROUND_START(13), GAME_ROUND_END(14),
    GAME_END(15), GAME_INFO(16), GAME_PLAYER_JOIN(17),
    GAME_PLAYER_LEAVE(18), GAME_PLAYER_INFO(19), GAME_PLAYER_READY(20),
    GAME_PLAYER_GUESS(21), GAME_PLAYER_WIN(22), GAME_PLAYER_LOSE(23),
    GAMES_LIST(24), GAME_CREATE(25), GAME_JOIN(26), GAME_LEAVE(27),
    GAME_CHAT(28), GAME_CHAT_PRIVATE(29), GAME_CHAT_PUBLIC(30),
    SERVER_CHAT(31), SERVER_CHAT_PRIVATE(32), SERVER_CHAT_PUBLIC(33),
    SERVER_AUTH(34), SERVER_AUTH_SUCCESS(35), SERVER_AUTH_FAIL(36),
    SERVER_REGISTER(37), SERVER_REGISTER_SUCCESS(38), SERVER_REGISTER_FAIL(39)
}