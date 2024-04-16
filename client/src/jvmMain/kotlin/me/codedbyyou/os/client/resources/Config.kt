package me.codedbyyou.os.client.resources

import dev.dejvokep.boostedyaml.YamlDocument
import me.codedbyyou.os.client.ui.dialog.Server
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
    private val _servers = mutableListOf<Server>()

    val servers: List<Server>
        get() = _servers
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
            println(i.toString())

            val ip = ipSection.getSection(i.toString())
            for (p in ip.keys) {
                val port = ip.getSection(p.toString())
                val server = Server(
                    ip= ip.name.toString().replace("+", "."),
                    port = p.toString().toInt(),
                    psuedoName = port.getOptionalString("user").getOrNull(),
                    ticket = port.getOptionalString("ticket").getOrNull()
                )
                _servers.add(server)
            }
        }

    }

    fun upsertServer(server: Server){
        if (_servers.contains(server)) {
            _servers.remove(server)
            _servers.add(server)
        }
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
        _servers.add(server)
    }

    fun removeServer(server: Server){
        _servers.remove(server)
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
    var musicMultiplier = 0.1f
        set(value) {
            val prev = field
            field = max(0f, min(value, 1f))
            if (field == 0f) {
                Assets.music.pause()
            } else {
                if (prev == 0f && value > 0f) {
                    Assets.music.resume()
                }
                Assets.music.volume =  field
            }
        }
    var sfxMultiplier = 1f
        set(value) {
            field = max(0f, min(value, 1f))
        }
}