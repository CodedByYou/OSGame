package me.codedbyyou.os.game.resource

import dev.dejvokep.boostedyaml.YamlDocument
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.codedbyyou.os.game.data.Server
import me.codedbyyou.os.game.data.ServersViewModel
import java.awt.Toolkit
import java.io.File
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max
import kotlin.math.min

object Config {
    private val TOOLKIT = Toolkit.getDefaultToolkit();
    private val VIRTUAL_WIDTH : Float = TOOLKIT.screenSize.width.toFloat()
    private val VIRTUAL_HEIGHT : Float = TOOLKIT.screenSize.height.toFloat()
    private val userHomeDirectory = System.getProperty("user.home")
    private val config = YamlDocument.create(File(userHomeDirectory,"/.osgame/config.yml"))

    var musicMultiplier = config.getOptionalFloat("musicMultiplier").getOrNull() ?: 0.05f
        set(value) {
            val prev = field
            field = max(0f, min(value, 1f))
            if (field == 0f) {
                AudioPlayer.stopBackgroundMusic()
            } else {
                if (prev == 0f && value > 0f) {
                    AudioPlayer.playBackgroundMusic()
                }
                AudioPlayer.setVolume(field)
            }
            config["musicMultiplier"] = field
            GlobalScope.launch {
                config.save()
            }

        }
    var sfxMultiplier = config.getOptionalFloat("sfxMultiplier").getOrNull() ?: 1f
        set(value) {
            field = max(0f, min(value, 1f))
            config["sfxMultiplier"] = field
            GlobalScope.launch {
                config.save()
            }
        }

    init {

        if (!config.contains("ip")) {
            config["ip"] = mapOf(
                "ssh+codedbyyou+com" to mapOf(
                    "13337" to mapOf<String, Any>(
                        "addition_date" to System.currentTimeMillis(),
                    )
                )
            )
            config.save()
        }

       val ipSection = config.getSection("ip")
        for (i in ipSection.keys) {
            val ip = ipSection.getSection(i.toString())
            for (p in ip.keys) {
                val port = ip.getSection(p.toString())
                //print all details
                val server = Server(
                    ip= ip.name.toString().replace("+", "."),
                    port = p.toString().toInt(),
                    psuedoName = port.getOptionalString("user").getOrNull(),
                    ticket = port.getOptionalString("ticket").getOrNull()
                )
                ServersViewModel.upsertServer(server)
            }
        }
    }

    fun upsertServer(server: Server){
        val ipSection = config.getSection("ip")
        val data = mutableMapOf<String, String>()
        val serverIP = server.ip.replace(".", "+")
        if (ipSection.keys.contains(serverIP)) {
            val ip = ipSection.getSection(serverIP)
            if (ip.keys.contains(server.port.toString())) {
                val port = ip.getSection(server.port.toString())
                if (server.psuedoName != null) {
                    port["user"] = server.psuedoName
                    port["ticket"] = server.ticket
                }
                port["addition_date"] = System.currentTimeMillis()
            } else {
                if (server.psuedoName != null) {
                    data["user"] = server.psuedoName!!
                    data["ticket"] = server.ticket!!
                }
                data["addition_date"] = System.currentTimeMillis().toString()
                ip[server.port.toString()] = data
            }
        } else {
            if (server.psuedoName != null) {
                data["user"] = server.psuedoName!!
                data["ticket"] = server.ticket!!
            }
            data["addition_date"] = System.currentTimeMillis().toString()
            ipSection[serverIP] = mapOf(
               server.port to data
            )
        }
        config.save()
        ServersViewModel.upsertServer(server)
    }

    fun removeServer(server: Server){
//        _servers.remove(server)
        val ipSection = config.getSection("ip")
        if (ipSection.keys.contains(server.ip)) {
            val ip = ipSection.getSection(server.ip)
            if (ip.keys.contains(server.port.toString())) {
                ip.remove(server.port.toString())
            }
        }
    }

    fun VSIZE(): Pair<Float, Float>{
            val aspectRatio = VIRTUAL_WIDTH/ VIRTUAL_HEIGHT
            val width = VIRTUAL_WIDTH  * 0.75f
            val height = width/aspectRatio
            return Pair<Float, Float>(width, height);
    }


    val soundAdjustStepRate = 0.1f;

}