package me.liwk.karhuvelocity.storage.migrations;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.IOException;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.SECTION_CHAR;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand;

public class LegacyConfigMigration implements ConfigMigration {

    @Override
    public boolean shouldMigrate(CommentedConfigurationNode config) {
        return configVersion(config) < 1;
    }

    @Override
    public void migrate(CommentedConfigurationNode root, Logger logger) throws IOException {
        root.node("version").set(1);

        CommentedConfigurationNode alertsMessageNode = root.node("AlertsMessage");
        var alertsMessage = alertsMessageNode.getString();
        if (alertsMessage != null && alertsMessage.indexOf(SECTION_CHAR) != -1) {
            root.node("alert-message").set(miniMessage().serialize(legacyAmpersand().deserialize(alertsMessage)));
            alertsMessageNode.set(null);
        }

        CommentedConfigurationNode prefixNode = root.node("Prefix");
        var prefix = prefixNode.getString();
        if (prefix != null && prefix.indexOf(SECTION_CHAR) != -1) {
            root.node("prefix").set(miniMessage().serialize(legacyAmpersand().deserialize(prefix)));
            prefixNode.set(null);
        }

        CommentedConfigurationNode alertsEnabledNode = root.node("alerts", "enabled");
        var alertsEnabled = alertsEnabledNode.getString();
        if (alertsEnabled != null && alertsEnabled.indexOf(SECTION_CHAR) != -1) {
            alertsEnabledNode.set(miniMessage().serialize(legacyAmpersand().deserialize(alertsEnabled)));
        }

        CommentedConfigurationNode alertsDisabledNode = root.node("alerts", "disabled");
        var alertsDisabled = alertsDisabledNode.getString();
        if (alertsDisabled != null && alertsDisabled.indexOf(SECTION_CHAR) != -1) {
            alertsDisabledNode.set(miniMessage().serialize(legacyAmpersand().deserialize(alertsDisabled)));
        }
    }
}
