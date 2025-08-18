package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.discord.command.Playerlist
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.concurrent.TimeUnit

class SlashCommandInteraction(private val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {

        if (event.name == "playerlist") {
            Playerlist(plugin).handler(event)
            return
        }
    }
}