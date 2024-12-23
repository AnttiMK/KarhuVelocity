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

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.liwk.karhuvelocity.messaging.PluginMessageHandler;

public class AlertCommand implements SimpleCommand {

    private final PluginMessageHandler listener;

    public AlertCommand(PluginMessageHandler listener) {
        this.listener = listener;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            listener.toggleAlertsFor(player);
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("karhu.bungee.alerts");
    }
}
