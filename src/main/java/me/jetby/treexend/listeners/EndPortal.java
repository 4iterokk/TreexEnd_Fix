package me.jetby.treexend.listeners;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.Event;
import me.jetby.treexend.tools.colorizer.Colorize;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import static me.jetby.treexend.tools.LocationHandler.deserializeLocation;

public class EndPortal implements Listener {

    private final Main plugin;
    private final Config config;
    private final Event event;
    public EndPortal(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getCfg();
        this.event = plugin.getEvent();
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        if (!event.isEndPortalStatus()) {
            if (e.getCause()==PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                e.setCancelled(true);

                plugin.getRunner().runAsync(() -> {
                    for (String string : config.getList("messages.endIsClose")) {
                        player.sendMessage(Colorize.hex(string));
                    }
                });
            }
        } else {
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
                Location from = e.getFrom();
                Block block = from.getBlock();
                if (block.getType() == Material.END_GATEWAY) {
                    BlockState state = block.getState();
                    if (state instanceof EndGateway gateway) {
                        gateway.setExitLocation(deserializeLocation(config.getString("end-close-teleport"), plugin));
                        gateway.update();
                        e.setCancelled(true);
                    }
                }
            }
            if (e.getCause()==PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                e.setTo(deserializeLocation(config.getString("end-enter-teleport"), plugin));
            }
        }
    }

}
