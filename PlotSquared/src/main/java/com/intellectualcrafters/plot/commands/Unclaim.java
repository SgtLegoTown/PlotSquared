////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Unclaim extends SubCommand {
    public Unclaim() {
        super(Command.UNCLAIM, "Unclaim a plot", "unclaim", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!BukkitPlayerFunctions.isInPlot(plr)) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        final Plot plot = BukkitPlayerFunctions.getCurrentPlot(plr);
        if (!BukkitPlayerFunctions.getTopPlot(plr.getWorld(), plot).equals(BukkitPlayerFunctions.getBottomPlot(plr.getWorld(), plot))) {
            return !sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if ((((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr)))) && !BukkitMain.hasPermission(plr, "plots.admin.command.unclaim")) {
            return !sendMessage(plr, C.NO_PLOT_PERMS);
        }
        assert plot != null;
        final PlotWorld pWorld = PlotSquared.getPlotWorld(plot.world);
        if (PlotSquared.useEconomy && pWorld.USE_ECONOMY) {
            final double c = pWorld.SELL_PRICE;
            if (c > 0d) {
                final Economy economy = PlotSquared.economy;
                economy.depositPlayer(plr, c);
                sendMessage(plr, C.ADDED_BALANCE, c + "");
            }
        }
        final boolean result = PlotSquared.removePlot(plr.getWorld().getName(), plot.id, true);
        if (result) {
            final World world = plr.getWorld();
            final String worldname = world.getName();
            PlotSquared.getPlotManager(world).unclaimPlot(world, pWorld, plot);
            DBFunc.delete(worldname, plot);
            // TODO set wall block
        } else {
            BukkitPlayerFunctions.sendMessage(plr, "Plot removal has been denied.");
        }
        BukkitPlayerFunctions.sendMessage(plr, C.UNCLAIM_SUCCESS);
        return true;
    }
}
