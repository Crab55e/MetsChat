package cc.crab55e.metsChat

import com.google.inject.Inject
import com.moandjiezana.toml.Toml
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import java.io.File
import java.nio.file.Path

class ConfigManager(
    private val plugin: MetsChat,
    private val dataDirectory: Path
) {
    private lateinit var config: Toml
    private var configFileName = "config.toml"
    init {
        reloadConfig()
    }
    fun getConfigFileName(): String { return this.configFileName }
    fun reloadConfig() {
        val configFile: File = dataDirectory.resolve(configFileName).toFile()
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            val resourceStream = plugin::class.java.classLoader.getResourceAsStream(configFileName)
                ?: throw RuntimeException("$configFileName is not found!!!")

            resourceStream.use { input ->
                configFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        this.config = Toml().read(configFile)
    }
    fun getConfig(): Toml {
        return this.config
    }
}