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

package me.liwk.karhuvelocity.storage;

import me.liwk.karhuvelocity.KarhuVelocity;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileConfiguration {

    private final KarhuVelocity plugin;
    private KarhuVelocityConfig config;

    public FileConfiguration(KarhuVelocity plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataDirectory().toFile(), "config.yml");
        if (!configFile.exists()) {
            try (var is = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (is == null) {
                    throw new RuntimeException("Default config.yml was not found in the JAR, please report this");
                }
                Files.copy(is, configFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy default config.yml", e);
            }
        } else {
            ConfigMigrator.tryMigrate(configFile, plugin);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().file(configFile).build();
        CommentedConfigurationNode root;
        try {
            root = loader.load();
            config = root.get(KarhuVelocityConfig.class);
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load plugin configuration", e);
        }
    }

    @ConfigSerializable
    static class KarhuVelocityConfig {

    }
}
