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
                "0" to mapOf(
                    "ip" to "ssh.codedbyyou.com",
                    "port" to 13337
                )
            )
            println("Nothing is in here whaaat?")
            config.save()
        }

       val ipSection = config.getSection("ip")
        for (i in ipSection.keys) {
            val ip = ipSection.getSection(i.toString())
            println("IP: ${ip["ip"]}, Port: ${ip["port"]}")
            _servers.add(Server(
                ip=ip["ip"] as String,
                port=ip.getInt("port"),
                psuedoName = ip.getOptionalString("user").getOrNull(),
                ticket = ip.getOptionalString("ticket").getOrNull()
            ))
        }

    }

    fun addServer(server: Server){
        val ipSection = config.getSection("ip")
        val index = ipSection.keys.size
        ipSection[index.toString()] = mapOf(
            "ip" to server.ip,
            "port" to server.port
        )
        config.save()
        _servers.add(server)
    }

    fun removeServer(server: Server){
        val ipSection = config.getSection("ip")
        for (i in ipSection.keys) {
            val ip = ipSection.getSection(i.toString())
            if (ip["ip"] == server.ip && ip.getInt("port") == server.port) {
                ipSection.remove(i.toString())
                config.save()
                _servers.remove(server)
                break
            }
        }
    }

    fun VSIZE(): Pair<Float, Float>{
            val aspectRatio = VIRTUAL_WIDTH/ VIRTUAL_HEIGHT
            val width = VIRTUAL_WIDTH  * 0.75f
            val height = width/aspectRatio
            return Pair<Float, Float>(width, height);
    }


    val soundAdjustStepRate = 0.01f;
    var musicMultiplier = 1f
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