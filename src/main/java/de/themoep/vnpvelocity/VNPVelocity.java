package de.themoep.vnpvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.viaversion.viaversion.velocity.service.ProtocolDetectorService;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.*
 */
@Plugin(
    id = "vnpvelocity",
    name = "VNPVelocity",
    version = "1.2-SNAPSHOT",
    dependencies = {@Dependency(id = "viaversion", optional = true)},
    description = "Velocity bridge for VanishNoPacket",
    authors = {"Phoenix616", "retrixe"}
)
public class VNPVelocity {

    private static VNPVelocity instance;
    Map<UUID, VanishStatus> statusUUIDMap = new HashMap<>();
    Map<String, VanishStatus> statusNameMap = new HashMap<>();

    private boolean viaVersionEnabled = false;

    private final ProxyServer proxy;
    private final Logger logger;

    public final ChannelIdentifier LEGACY_VANISH_STATUS_CHANNEL = new LegacyChannelIdentifier(
            "vanishStatus");
    public final ChannelIdentifier VANISH_STATUS_CHANNEL = MinecraftChannelIdentifier.from(
            "vanishnopacket:status");

    @Inject
    public VNPVelocity(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        proxy.getEventManager().register(VNPVelocity.getInstance(), new EventListeners(this));
        proxy.getChannelRegistrar().register(LEGACY_VANISH_STATUS_CHANNEL);
        proxy.getChannelRegistrar().register(VANISH_STATUS_CHANNEL);
        viaVersionEnabled = proxy.getPluginManager().getPlugin("ViaVersion").isPresent();
    }

    /**
     * Get the instance of the plugin
     * @return Itself
     */
    public static VNPVelocity getInstance() {
        return instance;
    }

    /**
     * Check if one player can se another one
     * @param watcher   The one that watches
     * @param player    The player he tries to see
     * @return          If the watcher can see the player
     */
    public boolean canSee(CommandSource watcher, Player player) {
        return getVanishStatus(player) == VanishStatus.VISIBLE
                || (watcher instanceof Player
                        && getVanishStatus((Player) watcher) == VanishStatus.UNKNOWN
                        && getVanishStatus(player) == VanishStatus.UNKNOWN)
                || watcher.hasPermission("vanish.see");
    }

    /**
     * Set the vanish status of a player<br />
     * This will <strong>not</strong> fire a VanishStatusChangeEvent!
     * @param player    The Player to set
     * @param vanished  If the user is vanished or not
     * @return          The previously assigned VanishStatus, VanishStatus.UNKNOWN if there weren't one!
     */
    VanishStatus setVanished(Player player, boolean vanished) {
        VanishStatus pre = statusUUIDMap.put(player.getUniqueId(), (vanished) ? VanishStatus.VANISHED : VanishStatus.VISIBLE);
        statusNameMap.put(player.getUsername(), (vanished) ? VanishStatus.VANISHED : VanishStatus.VISIBLE);
        return (pre == null) ? VanishStatus.UNKNOWN : pre;
    }

    /**
     * Set the vanish status of a player<br />
     * This will <strong>not</strong> fire a VanishStatusChangeEvent!
     * @param player    The Player to set
     * @param status    The VanishStatus to set
     * @return          The previously assigned status, VanishStatus.UNKNOWN if there weren't one!
     */
    VanishStatus setVanishStatus(Player player, VanishStatus status) {
        VanishStatus pre = statusUUIDMap.put(player.getUniqueId(), status);
        statusNameMap.put(player.getUsername(), status);
        return (pre == null) ? VanishStatus.UNKNOWN : pre;
    }

    /**
     * Get if a player is vanished or not
     * @param player    The Player to check
     * @return          The VanishStatus of the player, VanishStatus.UNKNOWN if we don't know it!
     */
    public VanishStatus getVanishStatus(Player player) {
        return getVanishStatus(player.getUniqueId());
    }

    /**
     * Get if a player is vanished or not
     * @param playerId  The UUID of the player to check
     * @return          The VanishStatus of the player, VanishStatus.UNKNOWN if we don't know it!
     */
    public VanishStatus getVanishStatus(UUID playerId) {
        VanishStatus status = statusUUIDMap.get(playerId);
        return (status == null) ? VanishStatus.UNKNOWN : status;
    }

    /**
     * Get if a player is vanished or not
     * @param playername    The name of the player to check
     * @return              The VanishStatus of the player, VanishStatus.UNKNOWN if we don't know it!
     */
    public VanishStatus getVanishStatus(String playername) {
        VanishStatus status = statusNameMap.get(playername);
        return (status == null) ? VanishStatus.UNKNOWN : status;
    }

    /**
     * Clears the player's status data<br />
     * This will <strong>not</strong> fire a VanishStatusChangeEvent!
     * @param player    The Player to clear the status data of
     * @return          The previously assigned status, VanishStatus.UNKNOWN if there weren't one!
     */
    VanishStatus clearStatusData(Player player) {
        VanishStatus pre = statusUUIDMap.remove(player.getUniqueId());
        statusNameMap.remove(player.getUsername());
        return (pre == null) ? VanishStatus.UNKNOWN : pre;
    }

    int getServerVersion(ServerInfo server) {
        if (viaVersionEnabled) {
            return ProtocolDetectorService.getProtocolId(server.getName());
        }
        return -1;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public enum VanishStatus {
        VANISHED,
        VISIBLE,
        UNKNOWN;
    }
}
