package me.jetby.treexend.tools.task;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.Logger;
import me.jetby.treexend.tools.NBTUtil;
import me.jetby.treexend.tools.storage.EggPrices;
import me.jetby.treexend.tools.storage.StorageType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public final class TaskManager {
    private final Runner runner;
    private final Config config;
    private final StorageType storage;
    private final Main plugin;

    public TaskManager(Main plugin) {
        this.plugin = plugin;
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

    public void startPortalCheck() {
        runner.startTimerAsync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!plugin.getEvent().isEndPortalStatus()) {
                    Location loc = player.getLocation();
                    Block block = loc.getBlock();
                    if (block.getType() == Material.END_PORTAL) {
                        for (String string : config.getEndIsClose()) {
                            player.sendMessage(string);
                        }
                    }
                }

            }

        }, 0L, 3 * 20L);
    }

    public void startPriceCheck() {
        runner.startTimer(() -> {
            for (Map.Entry<Location, EggPrices> entry : storage.getPriceCache().entrySet()) {
                EggPrices eggPrices = entry.getValue();
                eggPrices.increasePrices(config.getUpdateAmount(), config.getPriceMax());

                if (config.isDebug()) {
                    Logger.info("Цены "+eggPrices.prices());
                }

            }

        }, 0L, config.getUpdateInterval() * 20L);
    }


    public void startDragonCheck() {
        runner.startTimer(() -> {
            if (plugin.getEvent().isEndPortalStatus()) {
                if (dragonIsAlive()) {
                    int online = Bukkit.getOnlinePlayers().size();
                    if (online>0) {
                        plugin.getActions().execute(config.getActionsIfDragonAlive());
                    }
                }
            }
        }, 0L, config.getActionsIfDragonAliveDelay()*20L);
    }
    public void startEggChecking() {
        runner.startTimerAsync(() -> {
            if (config.isBarEgg()) {
                    int online = Bukkit.getOnlinePlayers().size();
                    if (online>0) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (plugin.getBossBarHandler().getEggBossBars().containsKey(player.getUniqueId())) {
                                if (!player.getInventory().contains(Material.DRAGON_EGG)) {
                                    plugin.getBossBarHandler().removeEggBossbar(player);
                                }
                            }
                            if (!player.getInventory().contains(Material.DRAGON_EGG)
                                    && player.getInventory().getItemInOffHand().getType()!=Material.DRAGON_EGG) continue;
                            plugin.getBossBarHandler().sendEggBossbar(player);
                        }
                    }
            }
        }, 0L, 20L);
    }

    public void startEggsLocationsChecking() {
        runner.startTimer(() -> {

            List<Location> priceLocations = new ArrayList<>(storage.getPriceCache().keySet());
            for (Location priceLocation : priceLocations) {
                if (!priceLocation.getBlock().getType().equals(Material.CHEST)) {
                    storage.getPriceCache().remove(priceLocation);
                    if (config.isDebug()) {
                        Logger.info("Локация " + priceLocation + " удалена");
                    }
                }
            }

            List<Location> locations = new ArrayList<>(storage.getLocationCache().keySet());
            for (Location location : locations) {
                World world = location.getWorld();
                int chunkX = location.getBlockX() >> 4;
                int chunkZ = location.getBlockZ() >> 4;

                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }

                Block block = location.getBlock();

                if (!block.getType().equals(Material.CHEST)) {
                    storage.removeCache(location);
                    continue;
                }

                if (!hasDragonEggInChest(location)) {
                    storage.removeCache(location);
                }
            }
        }, 20L * 10, 20L * 10);
    }

    private boolean hasDragonEggInChest(Location location) {
        BlockState state = location.getBlock().getState();
        if (!(state instanceof Chest)) {
            return false;
        }

        Chest chest = (Chest) state;
        Inventory inventory = chest.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.DRAGON_EGG) {
                return true;
            }
        }
        return false;
    }

}
