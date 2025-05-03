package cc.crab55e.metsChat.util

import cc.crab55e.metsChat.MetsChat
import com.moandjiezana.toml.Toml
import java.io.File
import java.nio.file.Path

class MessageConfigManager(
    private val plugin: MetsChat,
    private val dataDirectory: Path
) {
    private lateinit var config: Toml
    private var messageConfigFileName = "messages.toml"
    init {
        reload()
    }
    fun getFilename(): String { return this.messageConfigFileName }
    fun reload() {
        val configFile: File = dataDirectory.resolve(messageConfigFileName).toFile()
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            val resourceStream = plugin::class.java.classLoader.getResourceAsStream(messageConfigFileName)
                ?: throw RuntimeException("$messageConfigFileName is not found!!!")

            resourceStream.use { input ->
                configFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        this.config = Toml().read(configFile)
    }
    fun get(): Toml {
        return this.config
    }
}