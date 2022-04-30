package me.liwk.karhuvelocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public class AlertCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player) {
            Player player = (Player) invocation.source();
            UUID uuid = player.getUniqueId();

            if (AlertManager.ALERTS.containsKey(uuid)) {
                AlertManager.ALERTS.remove(uuid);
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(KarhuVelocity.getInstance().getAlertsDisabled()));
            } else {
                AlertManager.ALERTS.put(uuid, true);
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(KarhuVelocity.getInstance().getAlertsEnabled()));
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("karhu.bungee.alerts");
    }
}
