/*
 * KarhuVelocity, a proxy-side support plugin for Karhu Anticheat
 * Copyright (C) 2022  LIWK & contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.liwk.karhuvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import me.liwk.karhuvelocity.storage.FileConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Getter
@Plugin(id = "karhuvelocity", name = "KarhuVelocity", authors = {"LIWK"}, version = "1.1.0-SNAPSHOT")
public final class KarhuVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.create("karhu", "proxy");

    private FileConfiguration config;

    @Getter
    private String alertPrefix, alertMessage, alertsEnabled, alertsDisabled;

    @Inject
    private KarhuVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.config = new FileConfiguration(this);
        config.loadConfig();

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("alerts").plugin(this).build();
        commandManager.register(meta, new AlertCommand(this));

        server.getChannelRegistrar().register(IDENTIFIER);
    }

    @Subscribe
    public void onMessageReceive(PluginMessageEvent e) {
        if (!IDENTIFIER.equals(e.getIdentifier())) {
            return;
        }

        if (e.getSource() instanceof ServerConnection) {
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
                String subChannel = in.readUTF();
                if (subChannel.equals("karhu:bban")) {
                    e.setResult(PluginMessageEvent.ForwardResult.handled());
                    String command = in.readUTF();
                    this.logger.info("Executing command \"" + command + "\"...");
                    this.server.getCommandManager().executeAsync(this.server.getConsoleCommandSource(), command);
                }

                if (subChannel.equals("karhu:alert")) {
                    e.setResult(PluginMessageEvent.ForwardResult.handled());
                    if (!(e.getSource() instanceof ServerConnection)) {
                        return;
                    }
                    InetSocketAddress address = ((ServerConnection) e.getSource()).getServer().getServerInfo().getAddress();
                    if (address == null) return;

                    String[] alertData = in.readUTF().split("#");

                    Optional<Player> cheater = server.getPlayer(alertData[2]);

                    if (cheater.isPresent() && cheater.get().getCurrentServer().isPresent()) {
                        String flagServer = cheater.get().getCurrentServer().get().getServerInfo().getName();

                        TextComponent alertMessage = LegacyComponentSerializer.legacy('&')
                                .deserialize(this.alertMessage.replaceAll("%server%", flagServer))
                                .append(LegacyComponentSerializer.legacy('&')
                                        .deserialize(this.alertPrefix
                                                .replaceAll("%check%", alertData[0])
                                                .replaceAll("%vl%", alertData[1])
                                                .replaceAll("%player%", alertData[2])));

                        for (Player loopPlayer : this.server.getAllPlayers()) {
                            Optional<ServerConnection> currentServer = loopPlayer.getCurrentServer();
                            currentServer.ifPresent(server -> {
                                if (AlertManager.ALERTS.containsKey(loopPlayer.getUniqueId()) && !server.getServerInfo().getName().equals(flagServer)) {
                                    loopPlayer.sendMessage(alertMessage);
                                }
                            });
                        }

                    } else {
                        this.logger.error("Karhu couldn't find the player!");
                    }
                }

            } catch (IllegalStateException | IOException ex) {
                logger.error("Could not do anything with this PluginMessage! (" + String.format("'%s', %s", e.getIdentifier(), Arrays.toString(e.getData())) + ")", ex);
            }
        }
    }
}
