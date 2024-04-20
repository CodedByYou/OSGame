package me.codedbyyou.os.server.command.annotations

import kotlin.reflect.KClass

/**
 * File: Annotations.kt
 * Annotations used to create a command system.
 * The command system is a system that allows the creation of commands and subcommands.
 * The command system is used to create commands that can be executed by players or the console or other implementations of
 * CommandSender.
 *
 * The command system is based on annotations that are used to mark classes and methods as commands and subcommands.
 * The annotations are used to provide information about the command or subcommand such as the name, description, usage,
 * permission, aliases, and other information.
 *
 * ** The system allows the creation of nested subcommands that can be used to create complex commands with multiple levels of subcommands. **
 *
 * This system is useful for creating commands in plugins or other applications that require a command system.
 * The command system is extensible and can be used to create complex commands with multiple subcommands and arguments.
 *
 * @author Abdollah Kandrani
 * @since 1.0.0
 * @version 1.0.0
 */


/**
 * Annotation to mark a class as a command.
 * The command is a class that contains methods that are called when the command is called with the specified name.
 * The command is a string that represents the name of the command.
 * The command is required and must be unique.
 * Example:
 * ```
 * @Command("myCommand")
 * @Description("An example command.")
 * class MyCommand : ICommand {}
 * ```
 * @param name the name of the command
 * @see SubCommand
 * @see Description
 * @see Usage
 * @see Permission
 * @see Aliases
 * @see MainCommand
 * @see ICommand
 * @see ListArgumentType
 * @see ArgumentType
 * @see UnparsedArguments
 * @since 1.0.0
 * @version 1.0.0
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String,
    vararg val aliases: String = []
)

/**
 * Annotation to mark a subcommand of a command.
 * The subcommand is a method that is called when the command is called with the specified name or the aliases provided.
 * The subcommand is optional and can be used to create multiple commands within a single command class.
 * The subcommand annotation is also used to mark the name of the subcommand and its aliases for the command be it in the main command or a subcommand class.
 * Example:
 * ```
 * @Command("myCommand")
 * @Description("An example command.")
 * class MyCommand : ICommand {
 *      @SubCommand("info")
 *      @Description("Get information about a player.")
 *      @Usage("/myCommand info <player>")
 *      fun info(player: GamePlayer, target: GamePlayer) {
 *          player.sendMessage("Information about ${target.uniqueName}")
 *          // Do something
 *      }
 *  }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(val name: String, vararg val aliases: String = [])

/**
 * Annotation to mark the description of a command or subcommand.
 * The description is a string that provides information about the command or subcommand.
 * The description is optional and can be used to provide information about the command or subcommand.
 * Example:
 * ```
 * @Command("myCommand")
 * @Description("An example command.")
 * class MyCommand : ICommand {}
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(val description: String)

/**
 * Annotation to mark the usage of a command or subcommand.
 * The usage is a string that represents the correct usage of the command or subcommand.
 * The usage is optional and can be used to provide information about how to use the command or subcommand.
 * Example:
 * ```
 * @SubCommand("info")
 * @Description("Get information about a player.")
 * @Usage("/myCommand info <player>")
 * fun info(player: GamePlayer, target: GamePlayer) {
 *  player.sendMessage("Information about ${target.uniqueName}")
 *  // Do something
 *  }
 *  ```
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Usage(val usage: String)

/**
 * Annotation to mark the permission required to execute a command or subcommand.
 * The permission is a string that represents the permission node required to execute the command or subcommand.
 * The permission node is optional and can be used to restrict access to certain commands or subcommands.
 * Example:
 * ```
 * // example with base permission for the whole command and its subcommands
 * @Command("myCommand")
 * @Description("An example command.")
 * @Permission("some.permission")
 * class MyCommand : ICommand {}
 *
 * // example with permission for a subcommand
 * @SubCommand("info")
 * @Description("Get information about a player.")
 * @Permission("some.permission.info")
 * @Usage("/myCommand info <player>")
 * fun info(player: GamePlayer, target: GamePlayer) {
 *   player.sendMessage("Information about ${target.uniqueName}")
 *   // Do something
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class Permission(val permission: String)


/**
 * Annotation to mark the main command of a command class.
 * The main command is the command that is executed when the command is called without any subcommands.
 * Example:
 * ```
 * @Command("myCommand")
 * @Description("An example command.")
 * @Aliases(["m"])
 * class MyCommand : ICommand {
 *   @MainCommand
 *   @Description("Main command for math operations.")
 *   @Permission("some.permission")
 *   @Usage("/myCommand")
 *   fun main(player: GamePlayer) {
 *      player.sendMessage("Welcome to the main command.")
 *      // Do something
 *   }
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class MainCommand

/**
 * Annotation to mark a list parameter with its type.
 * This is useful for commands that have parameters with types that are not easily inferred or can be ambiguous.
 * It makes it easier to parse the arguments and convert them to the correct type.
 * The parameter must be of type List<T> where T is a type that can be converted from a string.
 * it splits the arguments by commas and converts each argument to the specified type.
 * Example:
 * ```
 * @SubCommand("info")
 * @Description("A command that takes a list of players.")
 * @Usage("/admin info [players]")
 * fun info(player: GamePlayer, @ListArgumentType(GamePlayer::class) players: List<GamePlayer>) {
 *    player.sendMessage("You sent the following players: ${players.map{it.uniqueName}.joinToString(", ")}")
 *    // Do something with the players
 * }
 *
 * @SubCommand("sum")
 * @Description("A command that takes a list of numbers.")
 * @Usage("/math sum [numbers]")
 * fun sum(player: GamePlayer, @ListArgumentType(Int::class) numbers: List<Int>) {
 *   player.sendMessage("You sent the following numbers: ${numbers.joinToString(", ")}")
 *   player.sendMessage("The sum of the numbers is: ${numbers.sum()}")
 *   // Do something with the numbers
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class ListArgumentType(val type: KClass<*>)

/**
 * Annotation to mark a parameter with its type.
 * This is useful for commands that have parameters with types that are not easily inferred or can be ambiguous.
 * It makes it easier to parse the arguments and convert them to the correct type.
 * The parameter must be of type that can be converted from a string.
 * Example:
 * ```
 * @SubCommand("myCommand")
 * @Description("A command that takes a player and a number.")
 * @Usage("/myCommand <player> <number>")
 * fun myCommand(playerSender: GamePlayer, target: GamePlayer, @ArgumentType(Int::class) number: Int) {
 *   player.sendMessage("You sent the number: $number")
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ArgumentType(val type: KClass<*>)


/**
 * Annotation to mark a parameter as unparsed arguments.
 * This is useful for commands that have a variable number of arguments.
 * The parameter must be of type List<String>
 * WARNING: Parameters after this parameter will be ignored.
 * Example:
 * ```
 * @SubCommand("myCommand")
 * @Description("A command that takes a variable number of arguments.")
 * @Usage("/myCommand <args>")
 * fun myCommand(playerSender: GamePlayer, @UnparsedArguments args: List<String>) {
 *    player.sendMessage("You sent the following arguments: $args")
 *    // Do something with the arguments
 *    // args[0] will be the first argument
 * }
 *
 * @SubCommand("privateMessage")
 * @Description("Send a private message to a player.")
 * @Usage("/privateMessage <player> <message>")
 * fun privateMessage(sender: CommandSender, GamePlayer target: Player, @UnparsedArguments message: List<String>) {
 *     target.sendMessage("[Private Message] ${sender.name}: ${message.joinToString(" ")}")
 *     sender.sendMessage("[Private Message] ${sender.name} to ${target.name}: ${message.joinToString(" ")}")
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UnparsedArguments

/**
 * Annotation to mark the class as a subcommand for a specific command.
 * This is useful for creating subcommands for a specific command.
 * The commandPath is a string that represents the path to the command that this subcommand is for.
 * The commandPath is required and must match the name of the command that this subcommand is for.
 * Example:
 * ```
 * // can also have this subcommand as a command if needed
 * // @Command("in")
 * @SubCommand("info", "i")
 * @SubCommandFor("myCommand")
 * class MySubCommand : ICommand {
 *
 *  @MainCommand
 *  @Description("Main command for info operations.")
 *  @Permission("info")
 *  @Usage("/myCommand info")
 *  fun main(player: GamePlayer) {
 *      player.sendMessage("Info command.")
 *  }
 *
 *
 *  @SubCommand("delete")
 *  @Description("Get information about a player.")
 *  @Usage("/myCommand info delete <player>")
 *  fun delete(player: GamePlayer, target: GamePlayer) {
 *    player.sendMessage("Information about ${target.uniqueName}")
 *    // Do something
 *  }
 * }
 *
 * ```
 * Example 2 for nesting subcommands:
 * ```
 * @SubCommand("info")
 * @SubCommandFor("myCommand", "info")
 * @Usage("/myCommand info info")
 * class MySubSubCommand : ICommand {
 *
 *  @MainCommand
 *  @Description("Main command for info operations.")
 *  @Permission("info")
 *  @Usage("/myCommand info info")
 *  fun main(player: GamePlayer) {
 *    player.sendMessage("Info command.")
 *    // Do something
 *  }
 * }
 * ```
 * @author Abdollah Kandrani
 * @since 1.0.0
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SubCommandFor(vararg val commandPath: String)