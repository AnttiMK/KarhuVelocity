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
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.liwk.karhuvelocity.commands.AlertCommand;
import me.liwk.karhuvelocity.messaging.PluginMessageHandler;
import me.liwk.karhuvelocity.storage.FileConfiguration;
import org.slf4j.Logger;

import java.nio.file.Path;

@Getter
@Plugin(
        id = "karhuvelocity",
        name = "KarhuVelocity",
        authors = {"LIWK", "AnttiMK"},
        version = "1.1.0-SNAPSHOT"
)
public final class KarhuVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private FileConfiguration config;

    @Inject
    private KarhuVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.config = new FileConfiguration(this);
        try {
            config.load();
        } catch (Exception e) {
            logger.error("Failed to load plugin configuration, plugin will be disabled.", e);
            return;
        }

        PluginMessageHandler listener = new PluginMessageHandler(server, config, logger);
        server.getEventManager().register(this, listener);

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("alerts").plugin(this).build();
        commandManager.register(meta, new AlertCommand(listener));

        logger.info("KarhuVelocity is now listening.");
    }
}
