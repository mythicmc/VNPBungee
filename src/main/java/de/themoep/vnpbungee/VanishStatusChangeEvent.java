package de.themoep.vnpbungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

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
 *
 * Inspired by mbaxter's original vnp VanishStatusChangeEvent (GPLv2)
 * https://github.com/mbax/VanishNoPacket/blob/master/src/main/java/org/kitteh/vanish/event/VanishStatusChangeEvent.java
 */

/**
 * An event fired whenever a player changes their visibility
 */
public final class VanishStatusChangeEvent extends Event {

    private final boolean vanishing;
    private final ProxiedPlayer player;

    public VanishStatusChangeEvent(ProxiedPlayer player, boolean vanishing) {
        this.vanishing = vanishing;
        this.player = player;
    }

    /**
     * Gets the player changing visibility
     *
     * @return the player changing visibility
     */
    public ProxiedPlayer getPlayer() {
        return this.player;
    }

    /**
     * Gets if this is a vanish or unvanish
     *
     * @return true if vanishing, false is revealing
     */
    public boolean isVanishing() {
        return this.vanishing;
    }
}
