package cc.crab55e.metsChat.util

import cc.crab55e.metsChat.MetsChat
import com.google.gson.JsonParser
import com.google.gson.JsonElement

class JsonComponentParser(private val plugin: MetsChat) {
    private var languageFile: LanguageFile? = null

    init {
        reload()
    }

    data class LanguageFile(val translations: Map<String, String>) {
        fun resolve(key: String): String = translations[key] ?: key
    }

    fun reload(): LanguageFile {
        val file = plugin.getDataDirectory().resolve("language-base.json").toFile()
        val json = JsonParser.parseString(file.readText()).asJsonObject
        val map = json.entrySet().associate { it.key to it.value.asString }
        languageFile = LanguageFile(map)
        return languageFile!!
    }

    fun parseComponent(component: JsonElement): String {
        if (languageFile == null) languageFile = reload()
        return when {
            component.isJsonPrimitive -> component.asString

            component.isJsonObject -> {
                val obj = component.asJsonObject

                val base = when {
                    obj.has("translate") -> {
                        val key = obj["translate"].asString
                        val format = languageFile!!.resolve(key)
                        val with = if (obj.has("with")) obj["with"].asJsonArray.map { parseComponent(it) } else emptyList()
                        format.format(*with.toTypedArray())
                    }
                    obj.has("text") -> obj["text"].asString
                    else -> ""
                }

                // Handle extras like hover_event (optional)
                val extras = if (obj.has("extra")) obj["extra"].asJsonArray.joinToString("") { parseComponent(it) } else ""
                base + extras
            }

            component.isJsonArray -> component.asJsonArray.joinToString("") { parseComponent(it) }

            else -> ""
        }
    }
}