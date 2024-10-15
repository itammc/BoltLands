package ru.alkheev.boltlands;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.land.LandWorld;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.bolt.BoltAPI;
import org.popcraft.bolt.event.LockBlockEvent;
import org.popcraft.bolt.event.LockEntityEvent;
import org.popcraft.bolt.source.SourceTypes;

public final class BoltLands extends JavaPlugin implements Listener {
    private BoltAPI bolt;
    LandsIntegration landsPlugin;

    @Override
    public void onEnable() {
        this.bolt = getServer().getServicesManager().load(BoltAPI.class);

        if (bolt == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.landsPlugin = LandsIntegration.of(this);

        bolt.registerPlayerSourceResolver((source, uuid) -> {
            if (!SourceTypes.REGION.equals(source.getType())) {
                return false;
            }
            final LandPlayer localPlayer = landsPlugin.getLandPlayer(uuid);
            if (localPlayer == null) {
                return false;
            }
            final LandWorld world = landsPlugin.getWorld(localPlayer.getPlayer().getWorld());
            if (world == null) {
                return false;
            }

            return world.hasRoleFlag(uuid, localPlayer.getPlayer().getLocation(), Flags.BLOCK_BREAK);
        });

        bolt.registerListener(LockBlockEvent.class, event -> {
            Player p = event.getPlayer();

            final LandWorld world = landsPlugin.getWorld(p.getWorld());
            if (world == null) {
                return;
            }

            final boolean canBolt = world.hasRoleFlag(p.getUniqueId(), p.getLocation(), Flags.BLOCK_BREAK);

            if (!canBolt) {
                event.setCancelled(true);
            }
        });

        bolt.registerListener(LockEntityEvent.class, event -> {
            Player p = event.getPlayer();

            final LandWorld world = landsPlugin.getWorld(p.getWorld());
            if (world == null) {
                return;
            }

            final boolean canBolt = world.hasRoleFlag(p.getUniqueId(), p.getLocation(), Flags.BLOCK_BREAK);

            if (!canBolt) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public void onDisable() {
        this.bolt = null;
        this.landsPlugin = null;
    }
}
