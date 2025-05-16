package me.jetby.treexend.tools.task;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.colorizer.Colorize;
import me.jetby.treexend.tools.storage.StorageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.jetby.treexend.tools.colorizer.Colorize.setPlaceholders;
import static org.bukkit.Bukkit.broadcastMessage;


public final class TaskManager {
    private final Runner runner;
    private final Config config;
    private final StorageType storage;

    public TaskManager(Main plugin) {
        this.runner = plugin.getRunner();
        this.config = plugin.getCfg();
        this.storage = plugin.getStorageType();
    }

    public boolean dragonIsAlive(){
        World world = Bukkit.getServer().getWorld("world_the_end");
        List<LivingEntity> entities = world.getLivingEntities();
        for (LivingEntity entity : entities) {
            if (entity instanceof org.bukkit.entity.EnderDragon) return true;

        } return false;
    }
    public void startDragonCheck() {
        runner.startTimer(() -> {
            if (dragonIsAlive()) {
                int online = Bukkit.getOnlinePlayers().size();
                if (online>0) {
                    for (String string : config.getList("messages.dragonDamage.message")) {
                        broadcastMessage(Colorize.hex(setPlaceholders(string, null)));
                    }
                }

            }
        }, 0L, config.getInt("messages.dragonDamage.messageDelay")* 20L);
    }

    public void startEggChecking() {
        runner.startTimer(() -> {
            for (Location location : storage.getCache().keySet()) {
                if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                    Block block = location.getBlock();

                    if (block.getType() != Material.CHEST) {
                        storage.removeCache(location);
                        continue;
                    }

                    Chest chest = (Chest) block.getState();
                    boolean hasDragonEgg = false;
                    for (ItemStack item : chest.getInventory().getContents()) {
                        if (item != null && item.getType() == Material.DRAGON_EGG) {
                            hasDragonEgg = true;
                            break;
                        }
                    }

                    if (!hasDragonEgg) {
                        storage.removeCache(location);
                    }

                }
            }
        }, 0L, 3 * 20);
    }

}
