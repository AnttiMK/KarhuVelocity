package me.liwk.karhuvelocity.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.liwk.karhuvelocity.storage.FileConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;

public class PluginMessageHandler {

    private static final Set<UUID> ENABLED_ALERTS = new HashSet<>();
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.create("karhu", "proxy");
    private final ProxyServer server;
    private final FileConfiguration config;
    private final Logger logger;

    public PluginMessageHandler(ProxyServer server, FileConfiguration config, Logger logger) {
        this.server = server;
        this.config = config;
        this.logger = logger;

        server.getChannelRegistrar().register(IDENTIFIER);
    }

    public void toggleAlertsFor(Player player) {
        UUID uuid = player.getUniqueId();
        if (ENABLED_ALERTS.contains(uuid)) {
            ENABLED_ALERTS.remove(uuid);
            player.sendRichMessage(config.get().getAlerts().getDisabled());
        } else {
            ENABLED_ALERTS.add(uuid);
            player.sendRichMessage(config.get().getAlerts().getEnabled());
        }
    }

    @Subscribe
    public void onMessageReceive(PluginMessageEvent event) {
        if (!IDENTIFIER.equals(event.getIdentifier())) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection connection)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String subChannel = in.readUTF();
            ServerInfo serverInfo = connection.getServerInfo();

            if (subChannel.equals("karhu:bban")) {
                String command = in.readUTF();
                this.logger.info("Received command \"{}\" from server {}, executing...", command, serverInfo.getName());
                this.server.getCommandManager().executeAsync(this.server.getConsoleCommandSource(), command);
            }

            if (subChannel.equals("karhu:alert")) {
                InetSocketAddress address = serverInfo.getAddress();
                if (address == null) return;

                String[] alertData = in.readUTF().split("#");
                final String check = alertData[0];
                final String vl = alertData[1];
                final String cheaterName = alertData[2];

                Optional<Player> cheater = server.getPlayer(cheaterName);

                if (cheater.isPresent() && cheater.get().getCurrentServer().isPresent()) {
                    String flagServer = cheater.get().getCurrentServer().get().getServerInfo().getName();

                    for (Player player : this.server.getAllPlayers()) {
                        Optional<ServerConnection> currentServer = player.getCurrentServer();
                        currentServer.ifPresent(server -> {
                            if (ENABLED_ALERTS.contains(player.getUniqueId()) && !server.getServerInfo().getName().equals(flagServer)) {
                                player.sendMessage(MiniMessage.miniMessage().deserialize(config.get().getPrefix().replaceAll("%server%", flagServer))
                                        .append(MiniMessage.miniMessage().deserialize(config.get().getAlertMessage()
                                                .replaceAll("%check%", check)
                                                .replaceAll("%vl%", vl)
                                                .replaceAll("%player%", cheaterName))));
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not do anything with this plugin message! (identifier: {}, message: {})",
                    event.getIdentifier(), Arrays.toString(event.getData()), e);
        }
    }
}
