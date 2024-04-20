package me.codedbyyou.os.core.interfaces.impl

import dev.dejvokep.boostedyaml.YamlDocument
import me.codedbyyou.os.core.interfaces.GameServer
import java.io.File

abstract class OSGameServer : GameServer {
    override val workingDirectory = File(System.getProperty("user.dir"))
    override val isFirstRun: Boolean = !File(workingDirectory, "config.yml").exists()
    override val config: YamlDocument = YamlDocument.create(File(workingDirectory, "config.yml"), this::class.java.classLoader.getResourceAsStream("config.yml"))

}