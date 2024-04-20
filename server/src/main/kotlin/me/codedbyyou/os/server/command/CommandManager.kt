package me.codedbyyou.os.server.command

import me.codedbyyou.os.server.Server
import me.codedbyyou.os.server.command.annotations.*
import me.codedbyyou.os.server.command.interfaces.CommandSender
import me.codedbyyou.os.server.command.interfaces.ICommand
import me.codedbyyou.os.server.player.GamePlayer
import me.codedbyyou.os.server.player.manager.PlayerManager
import org.reflections.Reflections
import java.lang.reflect.Method
import java.util.logging.Logger
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.kotlinFunction

/**
 * Command Manager
 * Manages all commands and executes them.
 * @author Abdollah Kandrani
 * @since 1.0.0
 * @version 1.0.0
 * @see ICommand
 * @see CommandHolder, Command, SubCommand, SubCommandFor, Description, Usage, Permission, MainCommand, ListArgumentType, annotations
 * @see CommandSender : GamePlayer, ConsoleCommandSender`
 **/
object CommandManager {

    private val logger = Logger.getLogger(CommandManager::class.java.name)
    private val command  = mutableMapOf<String, CommandHolder>()
    private val subCommands = mutableListOf<Triple<List<String>, CommandHolder, SubCommand>>()

    init {
        val commandPackage =
            Reflections("me.codedbyyou.os.server.command.commands")
                .getSubTypesOf(ICommand::class.java)
        commandPackage.forEach { cmd  ->
            val command = cmd.kotlin
            val annotations = command.annotations
            val commandAnnotation = annotations.find { it is Command } as Command?
            val subCommand = annotations.find { it is SubCommand } as SubCommand?
            val aliases: Array<out String> = commandAnnotation?.aliases ?: emptyArray()
            val basePermission = (annotations.find { it is Permission } as Permission?)?.permission ?: ""
            var commandName = command.simpleName!!

            if (commandAnnotation == null && subCommand == null) {
                Server.logger.warning("Command ${command.simpleName} does not have a @Command annotation.")
                Server.logger.warning("Used class name as command name, please add command name using @Command annotation.")
            } else {
                if(commandAnnotation != null)
                    commandName = commandAnnotation.name
                else
                    null
            }

            val methods = cmd.declaredMethods
            val mainMethod = methods.find { it.isAnnotationPresent(MainCommand::class.java) }
            val subMethods = methods.filter { it.isAnnotationPresent(SubCommand::class.java) }
            val mainCommand: CommandMethod?
            if (mainMethod == null) {
                logger.warning("Command ${command.simpleName} does not have a main command.")
                logger.warning("Please add a main command using @MainCommand annotation.")
                mainCommand = null
            } else {
                mainCommand = try {
                    prepareCommandMethod(mainMethod)
                } catch (e: IllegalArgumentException) {
                    logger.warning("Main command for ${command.simpleName} is invalid.")
                    logger.warning(e.message)
                    null
                }
            }

            val subCommands = mutableListOf<CommandMethod>()
            val subCommandNames = mutableSetOf<String>()
            for (method in subMethods) {
                val subCommand = prepareCommandMethod(method)
                if (subCommandNames.contains(subCommand.name)) {
                    logger.warning("Ambiguous subcommand ${subCommand.name} for command ${command.simpleName}")
                    logger.warning("A subcommand with the same name exists in the same command.")
                    logger.warning("Please use a different name for the subcommand.")
                    logger.warning("TIP: You can use aliases to create subcommands with the same name.")
                    logger.warning("TIP: For subcommands with optional parameters, mark them as nullable.")
                    logger.warning("Subcommands of subcommands are not supported yet.")
                    continue
                }
                subCommands.add(subCommand)
                logger.info("Subcommand $commandName registered.")
            }

            val holder = CommandHolder(
                instance = Class.forName(cmd.name).kotlin.createInstance() as ICommand,
                commandName, mainCommand, subCommands, mutableListOf(), basePermission,
                aliases.toList()
            )

            if (commandName != null) {
                this.command[commandName] = holder
                logger.info("Command $commandName registered.")
            }
            // means class has subcommand name
            if (subCommand != null) {
                val subCommandFor = annotations.find { it is SubCommandFor } as SubCommandFor?
                if (subCommandFor == null) {
                    logger.warning("Subcommand ${subCommand.name} for ${command.simpleName} does not have a @SubCommandFor annotation.")
                    logger.warning("Used class name as command name, please add command name using @SubCommandFor annotation.")
                    return@forEach
                }

                this.subCommands.add(Triple(subCommandFor.commandPath.toList(), this.command[commandName]!!, subCommand))
            }
        }

        for ((path, commandHolder, subCommand) in subCommands) {
            var currentCommandHolder = command[path[0]]!!
            for (subCommand in path.subList(1,path.size)) {
                val commandHolder = currentCommandHolder.subCommandHolder.find { it.name == subCommand }
                if (commandHolder == null) {
                    logger.warning("Subcommand $subCommand for command ${currentCommandHolder.name} not found.")
                    continue
                }
                currentCommandHolder = commandHolder
            }
            val newCommandHolder = CommandHolder(
                instance = commandHolder.instance,
                name = subCommand.name,
                mainMethod = commandHolder.mainMethod,
                subCommands = commandHolder.subCommands,
                subCommandHolder = commandHolder.subCommandHolder,
                basePermission = commandHolder.basePermission,
                aliases = subCommand.aliases.toList()
            )
            currentCommandHolder.subCommandHolder.add(newCommandHolder)
        }
        subCommands.clear()
    }

    @Throws(IllegalArgumentException::class)
    private fun prepareCommandMethod(method: Method) : CommandMethod {
        if (CommandSender::class.java.isAssignableFrom(method.parameters[0].type.javaClass))
            throw IllegalArgumentException("First parameter of method ${method.name} in command ${method.declaringClass.simpleName} is not a CommandSender or any of it's implementations.")
        val commandParams = mutableMapOf<String, Any>()
        if (method.isAnnotationPresent(SubCommand::class.java)) {
            val subCommandAnnon = (method.getAnnotation(SubCommand::class.java) as SubCommand)
            commandParams["name"] = subCommandAnnon.name
            commandParams["aliases"] = subCommandAnnon.aliases.toList()
        } else {
            commandParams["name"] = method.name
            commandParams["aliases"] = emptyList<String>()
        }
        if (method.isAnnotationPresent(Permission::class.java))
            commandParams["permission"] = (method.getAnnotation(Permission::class.java) as Permission).permission
        if (method.isAnnotationPresent(Description::class.java))
            commandParams["description"] = (method.getAnnotation(Description::class.java) as Description).description
        if (method.isAnnotationPresent(Usage::class.java))
            commandParams["usage"] = (method.getAnnotation(Usage::class.java) as Usage).usage

        return CommandMethod(
            permission = commandParams.getOrDefault("permission", "") as String,
            name = commandParams["name"] as String,
            method = method,
            aliases = commandParams["aliases"] as List<String>,
            parametersType = method.parameters.map { it.type },
            parameterCount = method.parameterCount - 1,
            optionalParameterCount = method.kotlinFunction!!.parameters.count { it.type.isMarkedNullable },
            usage = commandParams["usage"] as String,
            description = commandParams["description"] as String,
            executor = method.parameters[0].type
        )
    }


    fun executeCommand(commandSender: CommandSender, commandName: String, args: List<String>){
        logger.info("Executing command $commandName with args $args")
        if (commandName.equals("help", true) || commandName.equals("?", true)) {
            handleHelp(commandSender, args)
            return
        }
        var commandHolder = command[commandName]
        if (commandHolder == null){
            for (entry in command) {
                if (entry.value.subCommands.any { it.aliases.contains(commandName)}){
                    commandHolder = entry.value
                    break
                }
            }
            if (commandHolder == null){
                commandSender.sendMessage("[Server] Unknown command $commandName. Type /help for more info.")
                return
            }
        }

        if (commandHolder.basePermission.isNotEmpty() && !commandSender.hasPermission(commandHolder.basePermission)){
            commandSender.sendMessage("[Server] You do not have permission to execute this command.")
            return
        }

        if (args.isEmpty()){
            if (commandHolder.mainMethod == null){
                commandSender.sendMessage("[Server] Unknown command $commandName. Type /help $commandName for more info.")
                return
            }
            executeCommand(commandHolder.instance, commandSender, commandHolder.mainMethod!!, args)
            return
        }

        val (lastCommandHolder, subCommand, newArgs) = findSubCommand(commandHolder, args)

        if (subCommand == null){
            if (lastCommandHolder.mainMethod == null){
                commandSender.sendMessage("[Server] Unknown command $commandName. Type /help $commandName for more info.")
                return
            }
            executeCommand(lastCommandHolder.instance, commandSender, lastCommandHolder.mainMethod!!, args)
            return
        }

        executeCommand(lastCommandHolder.instance, commandSender, subCommand, newArgs)
    }

    private fun findSubCommand(subCommandHolder: CommandHolder, args: List<String>): Triple<CommandHolder, CommandMethod?, List<String>> {
        var prevCurrentHolder: CommandHolder? = subCommandHolder
        var currentHolder: CommandHolder? = subCommandHolder
        var latestArgs: List<String> = args
        var latestMethod: CommandMethod? = null

        for (argIndex in args.indices) {
            val arg = args[argIndex]
            var foundSubCommand: CommandMethod? = null
            var foundSubCommandHolder: CommandHolder? = null

            foundSubCommand = currentHolder?.subCommands?.find {
                it.name.equals(arg, true) || it.aliases.any { alias -> alias.equals(arg, true) }
            }

            if (foundSubCommand == null) {
                foundSubCommandHolder = currentHolder?.subCommandHolder?.find {
                    it.name.equals(arg, true) || it.aliases.any { alias -> alias.equals(arg, true) }
                }
            }

            if (foundSubCommand == null && foundSubCommandHolder == null) {
                return Triple(prevCurrentHolder!!, latestMethod, latestArgs)
            }

            if (foundSubCommandHolder != null) {
                prevCurrentHolder = currentHolder
                currentHolder = foundSubCommandHolder
                latestArgs = args.subList(argIndex + 1, args.size)
                continue
            }

            // If subcommand found, but it's the last argument, return the latest valid pair
            if (argIndex == args.size - 1) {
                return Triple(currentHolder!!, foundSubCommand, latestArgs.drop(1))
            }

            latestArgs = args.subList(argIndex + 1, args.size)
            latestMethod = foundSubCommand
        }

        return Triple(currentHolder!!, latestMethod, latestArgs)
    }

    private fun executeCommand(instance: Any, commandSender: CommandSender, commandMethod: CommandMethod, args: List<String>){
        if (args.size < commandMethod.parameterCount - commandMethod.optionalParameterCount){
            commandSender.sendMessage("[Server] Usage: ${commandMethod.usage}")
            return
        }
        val argsList = mutableListOf<Any>()
        argsList.add(commandSender)
        var isParameterCountOpen = false
        var failedAtParameter = -1
        for ((index, argument) in args.withIndex()){
            if (argsList.size > commandMethod.parameterCount)
                break
            failedAtParameter = index
            val parameter = commandMethod.method.parameters[index+1]
            if(parameter.isAnnotationPresent(ListArgumentType::class.java)){
                val annon = parameter.getAnnotation(ListArgumentType::class.java)
                val listType = annon.type
                val list = mutableListOf<Any>()
                for (arg in argument.split(",")){
                    val parsedArg = parseArg(listType.java, arg) ?: break
                    list.add(parsedArg)
                }
                if (parameter.type.isAssignableFrom(List::class.java)){
                    argsList.add(list)
                } else {
                    argsList.add(list.toTypedArray())
                }
                continue
            }

            if(parameter.isAnnotationPresent(ArgumentType::class.java)){
                val annon = parameter.getAnnotation(ArgumentType::class.java)
                val parsedArg = parseArg(annon.type.java, argument) ?: break
                argsList.add(parsedArg)
                isParameterCountOpen = true
                continue
            }

            if(parameter.isAnnotationPresent(UnparsedArguments::class.java)){
                val list = args.subList(index, args.size)
                argsList.add(list)
                isParameterCountOpen = true
                break
            }

            val parameterType = parameter.type
            if (parameterType.isAssignableFrom(String::class.java)){
                argsList.add(argument)
                continue
            }

            val parsedArg = parseArg(parameterType, argument) ?: break
            argsList.add(parsedArg)
        }

        logger.info("Failed at parameter $failedAtParameter with args ${argsList.size} and parameter count ${commandMethod.parameterCount} and optional parameter count ${commandMethod.optionalParameterCount}")
        if (failedAtParameter != -1 &&
            (argsList.size - 1) < commandMethod.parameterCount - commandMethod.optionalParameterCount ){
            val usage = commandMethod.method.getAnnotation(Usage::class.java)
            commandSender.sendMessage("[Server] Invalid argument for parameter ${commandMethod.method.kotlinFunction!!.parameters[failedAtParameter+1].name}")
            if (usage != null)
                commandSender.sendMessage("[Server] Usage: ${usage.usage}")
            return
        }

        if ((argsList.size - 1) < commandMethod.parameterCount - commandMethod.optionalParameterCount){
            commandSender.sendMessage("[Server] Usage: ${commandMethod.usage}")
            commandSender.sendMessage("[Server] Missing number of arguments.")
            return
        }

        if ((argsList.size - 1) > commandMethod.parameterCount && !isParameterCountOpen){
            argsList.dropLast(argsList.size - commandMethod.parameterCount)
        }

        try {
            commandMethod.method.invoke(instance, *argsList.toTypedArray())
        } catch (e: Exception){
            commandSender.sendMessage("[Server] An error occurred while executing the command ${commandMethod.name}")
            if (commandMethod.usage.isNotEmpty())
                commandSender.sendMessage("[Server] Usage: ${commandMethod.usage}")
            logger.warning("An error occurred while executing the command ${commandMethod.name}")
            logger.warning(e.message)
        }

    }


    /**
     * method to show help for all commands depending on the arguments
     * @param commandSender the command sender
     * @param args the arguments
     * @see CommandSender
     */
    private fun handleHelp(commandSender: CommandSender, args: List<String> = emptyList()){
        if (args.isEmpty()) {
            help(commandSender)
            return
        }
        if (args.size == 1) {
            val command = args[0]
            if (command.toIntOrNull() != null) {
                help(commandSender, command.toInt())
                return
            }
            val commandHolder = this.command[command]
            if (commandHolder == null) {
                commandSender.sendMessage("[Server] [Usage] /<help, ?> <command | page>")
                commandSender.sendMessage("[Server] [Help] Command $command not found.")
                return
            }
            val (holder, method, args) = findSubCommand(commandHolder, args)
            help(commandSender, holder)
            return
        }

    }
    private fun parseArg(parameterType: Class<*>, argument: String) : Any? {
        if (parameterType.isAssignableFrom(String::class.java)) {
            return argument as String
        }
        if (parameterType.isAssignableFrom(Int::class.java)) {
            try {
                return argument.toInt()
            } catch (e: NumberFormatException) {
                return null
            }
        }

        if (parameterType.isAssignableFrom(Boolean::class.java)) {
            if (argument.equals("true", true) || argument.equals("false", true)) {
                return argument.toBoolean()
            }
        }

        if (parameterType.isAssignableFrom(Double::class.java)) {
            try {
                return argument.toDouble()
            } catch (e: NumberFormatException) {
                return null
            }
        }

        if (parameterType.isAssignableFrom(Float::class.java)) {
            try {
                return argument.toFloat()
            } catch (e: NumberFormatException) {
                return null
            }
        }

        if (parameterType.isAssignableFrom(Long::class.java)) {
            try {
                return argument.toLong()
            } catch (e: NumberFormatException) {
                return null
            }
        }

        if (parameterType.isAssignableFrom(Short::class.java)) {
            try {
                return argument.toShort()
            } catch (e: NumberFormatException) {
                return null
            }
        }

        if (parameterType.isAssignableFrom(Byte::class.java)) {
            try {
                return argument.toByte()
            } catch (e: NumberFormatException) {
                return null
            }
        }

        if (parameterType.isAssignableFrom(Char::class.java)) {
            return argument[0]
            if (parameterType.isAssignableFrom(GamePlayer::class.java)) {
                return PlayerManager.getPlayer(argument)
            }
            return null
        }

        return null
    }

    /**
     * method to show help for all commands
     * @param commandSender the command sender
     * @param page the page number
     * @param resultPerPage the number of results per page
     * @see CommandSender
     */
    private fun help(commandSender: CommandSender, page: Int = 0, resultPerPage: Int = 5){
        val commands = command.values.toList()
        val totalPages = (commands.size / resultPerPage) + 1
        if (page > totalPages){
            commandSender.sendMessage("Page $page not found.")
            return
        }
        commandSender.sendMessage("Help Page $page/$totalPages")
        for (i in page * resultPerPage..<(page + 1) * resultPerPage){
            if (i >= commands.size)
                break
            help(commandSender, commands[i])
        }
    }

    /**
     * needs improvement later on for in-depth help on many subcommands level
     */
    private fun help(commandSender: CommandSender, commandHolder: CommandHolder){
        commandSender.sendMessage("Help for ${commandHolder.name}:")
        if (commandHolder.mainMethod != null){
            val mainCommand = commandHolder.mainMethod
            commandSender.sendMessage("Main Command:")
            if (mainCommand.permission.isNotEmpty())
                commandSender.sendMessage("Permission: ${mainCommand.permission}")
            if (mainCommand.aliases.isNotEmpty())
                commandSender.sendMessage("Aliases: ${mainCommand.aliases.joinToString(", ")}")
            if (mainCommand.description.isNotEmpty())
                commandSender.sendMessage("Description: ${mainCommand.description}")
            else
                commandSender.sendMessage("No description found.")
            if (mainCommand.usage.isNotEmpty())
                commandSender.sendMessage("Usage: ${mainCommand.usage}")
            else
                commandSender.sendMessage("No Command Usage found.")
        }
        if (commandHolder.subCommands.isNotEmpty() || commandHolder.subCommandHolder.isNotEmpty()){
            commandSender.sendMessage("Sub Commands:")
            for (subCommand in commandHolder.subCommands){
                sendCommandHelp(commandSender, subCommand)
            }
            for (subCommand in commandHolder.subCommandHolder){
                subCommand.mainMethod?.let { sendCommandHelp(commandSender, it) }
                for (subSubCommand in subCommand.subCommands){
                    sendCommandHelp(commandSender, subSubCommand)
                }
            }
        } else {
            commandSender.sendMessage("No sub commands found.")
        }
    }
    private fun sendCommandHelp(commandSender: CommandSender, commandMethod: CommandMethod){
        commandSender.sendMessage("- [${commandMethod.name}]:")
        if (commandMethod.description.isNotEmpty())
            commandSender.sendMessage("Description: ${commandMethod.description}")
        else
            commandSender.sendMessage("No description found.")
        if (commandMethod.usage.isNotEmpty())
            commandSender.sendMessage("Usage: ${commandMethod.usage}")
        if (commandMethod.permission.isNotEmpty())
            commandSender.sendMessage("Permission: ${commandMethod.permission}")
        if (commandMethod.aliases.isNotEmpty())
            commandSender.sendMessage("Aliases: ${commandMethod.aliases.joinToString(", ")}")
        else
            commandSender.sendMessage("No Command Usage found.")
    }
}


private data class CommandHolder(
    val instance : ICommand,
    val name : String,
    val mainMethod: CommandMethod?,
    val subCommands: List<CommandMethod>,
    val subCommandHolder: MutableList<CommandHolder>,
    val basePermission: String,
    val aliases: List<String> = emptyList()
)

private data class CommandMethod(
    val permission: String,
    val name: String,
    val method: Method,
    val aliases: List<String>,
    val parametersType: List<Class<*>>,
    val parameterCount : Int,
    val optionalParameterCount : Int,
    val usage: String,
    val description: String,
    val executor: Class<*>
)

