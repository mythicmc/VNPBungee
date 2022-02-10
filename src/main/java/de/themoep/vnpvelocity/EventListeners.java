package de.themoep.vnpvelocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;

import java.util.Iterator;
import java.util.List;

/**
 * VNPBungee - Bungee bridge for VanishNoPacket
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class EventListeners {

    private final VNPVelocity plugin;

    public EventListeners(VNPVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessageReceive(PluginMessageEvent event) {
        if(event.getTarget() instanceof Player
                && (event.getIdentifier().getId().equals("vanishStatus") ||
                        event.getIdentifier().getId().equals("vanishnopacket:status"))) {
            Player player = (Player) event.getTarget();
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            byte status = in.readByte();
            plugin.getProxy().getEventManager().fireAndForget(new VanishStatusChangeEvent(player, status == 1));
        }
    }

    @Subscribe
    public void onStatusChange(VanishStatusChangeEvent event) {
        VNPVelocity.VanishStatus pre = plugin.setVanished(event.getPlayer(), event.isVanishing());
        plugin.getLogger().info(event.getPlayer().getUsername() + " " + (event.isVanishing() ? "" : "un") + "vanished! Previous status: " + pre.toString());
    }
    
    @Subscribe
    public void onServerSwitch(ServerPostConnectEvent event) {
        ServerConnection serverConnection = event.getPlayer().getCurrentServer().orElse(null);
        if (serverConnection == null) {
            return;
        }
        int serverVersion = plugin.getServerVersion(serverConnection.getServerInfo());
        ChannelIdentifier pluginChannel = plugin.VANISH_STATUS_CHANNEL;
        if (serverVersion > 0 && serverVersion < 386) {
            pluginChannel = plugin.LEGACY_VANISH_STATUS_CHANNEL;
        }
        serverConnection.getServer().sendPluginMessage(pluginChannel, "check".getBytes());
        plugin.clearStatusData(event.getPlayer());
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        plugin.clearStatusData(event.getPlayer());
    }

    @Subscribe(order = PostOrder.LATE)
    public void onTabCompletion(TabCompleteEvent event) {
        handleTabEvent(event.getPlayer(), event.getSuggestions());
    }

    private void handleTabEvent(Player sender, List<String> suggestions) {
        for (Iterator<String> it = suggestions.iterator(); it.hasNext(); ) {
            Player player = plugin.getProxy().getPlayer(it.next()).orElse(null);
            if (player != null && !plugin.canSee(sender, player)) {
                it.remove();
            }
        }
    }
}
