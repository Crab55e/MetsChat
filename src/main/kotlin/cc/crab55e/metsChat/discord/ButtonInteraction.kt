package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ButtonInteraction(private val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        if (event.button.id != "metschat:timeout_notify_stop") return

        val discordNotifyMessagesTable = plugin.getMessageConfigManager().get().getTable("backend-support.timeout.discord-notify")
        val discordNotifyTable = plugin.getBackendSupportConfigManager().get().getTable("gateway.timeout.discord-notify")

        val buttonPushableUserIds = discordNotifyTable.getList<String>("notify-stop-button-pushable-user-ids")

        if (!buttonPushableUserIds.contains(event.user.id)) {
            val permissionDeniedMessage = discordNotifyMessagesTable.getString("stop-button-permission-denied")
            event.interaction.reply(permissionDeniedMessage).setEphemeral(true).queue()
            return
        }

        if (plugin.getHeartbeatTimeoutEvent().stoppedNotifyLoop) {
            val alreadyStoppedMessage = discordNotifyMessagesTable.getString("notify-already-stopped-message")
            event.interaction.reply(alreadyStoppedMessage).setEphemeral(true).queue()
            return
        } else {
            plugin.getHeartbeatTimeoutEvent().stoppedNotifyLoop = true
            val stoppedMessage = discordNotifyMessagesTable.getString("notify-stopped-message")
            event.interaction.reply(stoppedMessage).setEphemeral(true).queue()
            logger.info("Stopping discord timeout notify message loop.")
            return
        }
    }
}