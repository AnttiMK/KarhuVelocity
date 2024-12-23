package me.liwk.karhuvelocity.storage.migrations;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.IOException;

public interface ConfigMigration {

    boolean shouldMigrate(CommentedConfigurationNode config);

    void migrate(CommentedConfigurationNode rootNode, Logger logger) throws IOException;

    default double configVersion(CommentedConfigurationNode rootNode) {
        return rootNode.node("version").getInt(0);
    }
}
