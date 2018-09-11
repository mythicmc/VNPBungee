package de.themoep.vnpbungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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
public class EventListeners implements Listener {

    private final VNPBungee plugin;

    public EventListeners(VNPBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginMessageReceive(PluginMessageEvent event) {
        if(event.getReceiver() instanceof ProxiedPlayer
                && (event.getTag().equals("vanishStatus") || event.getTag().equals("vanishnopacket:status"))) {
            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            byte status = in.readByte();
            plugin.getProxy().getPluginManager().callEvent(new VanishStatusChangeEvent(player, status == 1));
        }
    }

    @EventHandler
    public void onStatusChange(VanishStatusChangeEvent event) {
        VNPBungee.VanishStatus pre = plugin.setVanished(event.getPlayer(), event.isVanishing());
        plugin.getLogger().info(event.getPlayer().getName() + " " + (event.isVanishing() ? "" : "un") + "vanished! Previous status: " + pre.toString());
    }
    
    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        event.getPlayer().getServer().sendData("vanishStatus", "check".getBytes());
        event.getPlayer().getServer().sendData("vanishnopacket:status", "check".getBytes());
        plugin.clearStatusData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent event) {
        plugin.clearStatusData(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabCompletion(TabCompleteEvent event) {
        handleTabEvent(event.getSender(), event.getSuggestions());
    }

    @EventHandler
    public void onTabBackendCompletion(TabCompleteResponseEvent event) {
        handleTabEvent(event.getSender(), event.getSuggestions());
    }

    private void handleTabEvent(Connection senderConnection, List<String> suggestions) {
        if (senderConnection instanceof ProxiedPlayer) {
            ProxiedPlayer sender = (ProxiedPlayer) senderConnection;
            for (Iterator<String> it = suggestions.iterator(); it.hasNext(); ) {
                ProxiedPlayer player = plugin.getProxy().getPlayer(it.next());
                if (player != null && !plugin.canSee(sender, player)) {
                    it.remove();
                }
            }
        }
    }
}
