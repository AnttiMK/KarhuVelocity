package me.liwk.karhuvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.Data;
import lombok.Getter;
import lombok.var;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Data
@Plugin(id = "karhuvelocity", name = "KarhuVelocity", authors = {"LIWK"}, version = "1.0.0-SNAPSHOT")
public final class KarhuVelocity {

    private static KarhuVelocity instance;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigurationNode config;
    @Getter
    private String alertPrefix, alertMessage, alertsEnabled, alertsDisabled;

    @Inject
    private KarhuVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, CommandManager commandManager) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        commandManager.register("alerts", new AlertCommand());
    }

    public static KarhuVelocity getInstance() {
        return instance;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        onEnable();
    }

    public void onEnable() {
        instance = this;

        var config = loadConfig();

        if (!config.isPresent()) {
            logger.warn("Config file could not be loaded.");
            return;
        }

        this.config = config.get();


        if (this.config.getString("Prefix") == null) {
            this.config.getNode("Prefix").setValue("&7(&b&l%server%&7) ");
        }

        if (this.config.getString("AlertsMessage") == null) {
            this.config.getNode("AlertsMessage").setValue("&f%player% &7failed &b%check% &7[x&b%vl%&7]");
        }

        if (this.config.getString("alerts.enabled") == null) {
            this.config.getNode("alerts.enabled").setValue("&aYou enabled Karhu bungee alerts");
        }

        if (this.config.getString("alerts.disabled") == null) {
            this.config.getNode("alerts.disabled").setValue("&cYou disabled Karhu bungee alerts");
        }

        this.alertMessage = this.config.getString("AlertsMessage");
        this.alertPrefix = this.config.getString("Prefix");
        this.alertsEnabled = this.config.getString("alerts.enabled");
        this.alertsDisabled = this.config.getString("alerts.disabled");

        this.saveConfig();
    }

    @Subscribe
    public void onMessageReceive(PluginMessageEvent e) {

        if (e.getIdentifier().getId().equalsIgnoreCase("BungeeCord") && e.getSource() instanceof ServerConnection) {
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
                String subChannel = in.readUTF();
                if (subChannel.equals("karhu:bban")) {

                    this.logger.info("Received: " + Arrays.toString(e.getData()));

                    String command = in.readUTF();
                    this.logger.info("Received an order to execute the command '/" + command + "'. Executing...");
                    this.server.getCommandManager().executeAsync(this.server.getConsoleCommandSource(), command);
                }

                if (subChannel.equals("karhu:alert")) {
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

    private Optional<ConfigurationNode> loadConfig() {
        var file = new File(dataDirectory.toFile(), "config.yml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("config.yml")), file.toPath());
            } catch (IOException e) {
                logger.error("Unable to load default config!", e);
            }
        }
        try {
            return Optional.of(YAMLConfigurationLoader.builder().setFile(file).build().load());
        } catch (IOException e) {
            logger.error("Unable to load config!", e);
        }
        return Optional.empty();
    }

    public void saveConfig() {
        var file = new File(dataDirectory.toFile(), "config.yml");
        if (!file.getParentFile().exists() || !file.exists()) loadConfig();

        try {
            YAMLConfigurationLoader.builder().setFile(file).setFlowStyle(DumperOptions.FlowStyle.BLOCK).setIndent(2).build().save(config);
        } catch (IOException e) {
            logger.error("Could not write to config!", e);
        }
    }
}
