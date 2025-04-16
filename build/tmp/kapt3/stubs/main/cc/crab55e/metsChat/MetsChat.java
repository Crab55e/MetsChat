package cc.crab55e.metsChat;

@com.velocitypowered.api.plugin.Plugin(id = "metschat", name = "MetsChat", version = "1.0-SNAPSHOT")
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u000b\u001a\u00020\fJ\u0006\u0010\r\u001a\u00020\nJ\u0006\u0010\u000e\u001a\u00020\u0007J\u0006\u0010\u000f\u001a\u00020\u0003J\u0006\u0010\u0010\u001a\u00020\u0005J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0007J\u0010\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0016H\u0007R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcc/crab55e/metsChat/MetsChat;", "", "logger", "Lorg/slf4j/Logger;", "server", "Lcom/velocitypowered/api/proxy/ProxyServer;", "dataDirectory", "Ljava/nio/file/Path;", "(Lorg/slf4j/Logger;Lcom/velocitypowered/api/proxy/ProxyServer;Ljava/nio/file/Path;)V", "configManager", "Lcc/crab55e/metsChat/ConfigManager;", "getCommandManager", "Lcom/velocitypowered/api/command/CommandManager;", "getConfigManager", "getDataDirectory", "getLogger", "getServer", "onPlayerChat", "", "event", "Lcom/velocitypowered/api/event/player/PlayerChatEvent;", "onProxyInitialization", "Lcom/velocitypowered/api/event/proxy/ProxyInitializeEvent;", "MetsChat"})
public final class MetsChat {
    @org.jetbrains.annotations.NotNull()
    private final org.slf4j.Logger logger = null;
    @org.jetbrains.annotations.NotNull()
    private final com.velocitypowered.api.proxy.ProxyServer server = null;
    @org.jetbrains.annotations.NotNull()
    private final java.nio.file.Path dataDirectory = null;
    @org.jetbrains.annotations.NotNull()
    private final cc.crab55e.metsChat.ConfigManager configManager = null;
    
    @com.google.inject.Inject()
    public MetsChat(@org.jetbrains.annotations.NotNull()
    org.slf4j.Logger logger, @org.jetbrains.annotations.NotNull()
    com.velocitypowered.api.proxy.ProxyServer server, @com.velocitypowered.api.plugin.annotation.DataDirectory()
    @org.jetbrains.annotations.NotNull()
    java.nio.file.Path dataDirectory) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final org.slf4j.Logger getLogger() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.velocitypowered.api.proxy.ProxyServer getServer() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final cc.crab55e.metsChat.ConfigManager getConfigManager() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.nio.file.Path getDataDirectory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.velocitypowered.api.command.CommandManager getCommandManager() {
        return null;
    }
    
    @com.velocitypowered.api.event.Subscribe()
    public final void onPlayerChat(@org.jetbrains.annotations.NotNull()
    com.velocitypowered.api.event.player.PlayerChatEvent event) {
    }
    
    @com.velocitypowered.api.event.Subscribe()
    public final void onProxyInitialization(@org.jetbrains.annotations.NotNull()
    com.velocitypowered.api.event.proxy.ProxyInitializeEvent event) {
    }
}