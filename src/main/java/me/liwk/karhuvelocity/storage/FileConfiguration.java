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
import me.liwk.karhuvelocity.storage.migrations.ConfigMigration;
import me.liwk.karhuvelocity.storage.migrations.LegacyConfigMigration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FileConfiguration {

    private final KarhuVelocity plugin;
    private final YamlConfigurationLoader loader;
    private final ObjectMapper<KarhuVelocityConfig> mapper;
    private final AtomicReference<KarhuVelocityConfig> config;

    public FileConfiguration(final @NotNull KarhuVelocity plugin) {
        this.plugin = plugin;
        this.config = new AtomicReference<>();

        this.loader = YamlConfigurationLoader.builder()
                .path(plugin.getDataDirectory().resolve("config.yml"))
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .build();

        try {
            mapper = ObjectMapper.factory().get(KarhuVelocityConfig.class);
        } catch (SerializationException e) {
            throw new RuntimeException("Failed to create ObjectMapper", e);
        }
    }

    public KarhuVelocityConfig get() {
        return config.get();
    }

    public CommentedConfigurationNode applyMigrations() throws Exception {
        final CommentedConfigurationNode root = this.loader.load();
        final List<ConfigMigration> migrations = List.of(
                new LegacyConfigMigration()
        );

        boolean migrated = false;
        for (final ConfigMigration migration : migrations) {
            if (migration.shouldMigrate(root)) {
                migration.migrate(root, plugin.getLogger());
                migrated = true;
            }
        }

        if (migrated) {
            this.loader.save(root);
        }

        return root;
    }

    public void load() throws Exception {
        var configuration = this.mapper.load(this.applyMigrations());
        this.save(configuration);
        this.config.set(configuration);
    }

    public void save(final @NotNull KarhuVelocityConfig config) throws ConfigurateException {
        var node = this.loader.createNode();
        this.mapper.save(config, node);
        this.loader.save(node);
    }
}
