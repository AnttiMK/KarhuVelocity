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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public class ConfigMigrator {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static void tryMigrate(File configFile, KarhuVelocity plugin) {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().file(configFile).build();
        CommentedConfigurationNode root;
        try {
            root = loader.load();
            if (root.node("version").virtual()) {
                migrate(root, loader);
            }
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load plugin configuration", e);
        }
    }

    private static void migrate(CommentedConfigurationNode root, YamlConfigurationLoader loader) throws SerializationException {
        root.node("version").set(1);

        var alertsMessage = root.node("AlertsMessage").getString();
        if (alertsMessage != null) {
            root.node("alert-message").set(miniMessage().serialize(LEGACY_SERIALIZER.deserialize(alertsMessage)));
        }


    }
}
