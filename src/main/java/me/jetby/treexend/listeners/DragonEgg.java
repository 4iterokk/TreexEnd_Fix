package me.jetby.treexend.listeners;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.colorizer.Colorize;
import me.jetby.treexend.tools.colorizer.ColorizerType;
import me.jetby.treexend.tools.storage.StorageType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;


public class DragonEgg implements Listener {

    private final Config config;
    private final StorageType storage;
    private final Main plugin;
    public DragonEgg(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getCfg();
        this.storage = plugin.getStorageType();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();
        Inventory clickedInventory = e.getClickedInventory();
        Inventory topInventory = e.getView().getTopInventory();
        if ((clickedItem != null && clickedItem.getType() == Material.DRAGON_EGG) ||
                (cursorItem != null && cursorItem.getType() == Material.DRAGON_EGG)) {
                Inventory inventory = e.getInventory();
            if (RESTRICTED_INVENTORIES.contains(topInventory.getType())) {
                config.getChestOnly().forEach(player::sendMessage);
                e.setCancelled(true);
                return;
            }
        }
        if (e.getClick().isShiftClick() || e.getHotbarButton() >= 0) {
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (offHandItem.getType() == Material.DRAGON_EGG && RESTRICTED_INVENTORIES.contains(topInventory.getType())) {
                config.getChestOnly().forEach(player::sendMessage);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        Inventory topInventory = player.getOpenInventory().getTopInventory();

        if ((e.getMainHandItem() != null && e.getMainHandItem().getType() == Material.DRAGON_EGG) ||
                (e.getOffHandItem() != null && e.getOffHandItem().getType() == Material.DRAGON_EGG)) {

            if (topInventory != null && RESTRICTED_INVENTORIES.contains(topInventory.getType())) {
                config.getChestOnly().forEach(player::sendMessage);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (config.isListenersDropOnQuit()) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (offHandItem != null && offHandItem.getType() == Material.DRAGON_EGG) {
                player.getWorld().dropItemNaturally(location, offHandItem);
                player.getInventory().setItemInOffHand(null);
            }
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.DRAGON_EGG) {
                    player.getWorld().dropItemNaturally(location, item);
                    player.getInventory().remove(item);
                }
            }
        }
    }
    @EventHandler
    public void clearBossBars(PlayerQuitEvent event) {
        plugin.getBossBarHandler().clearPlayerBossBars(event.getPlayer());
    }
    @EventHandler
    public void onItemMoved(InventoryMoveItemEvent e) {
        ItemStack item = e.getItem();
        if (item.getType() == Material.DRAGON_EGG) {
            if (e.getDestination().getLocation() != null) {
                    if (e.getDestination().getHolder() instanceof Chest destinationChest) {
                        Location chestLoc = destinationChest.getBlock().getLocation();
                        plugin.getRunner().runLater(() -> {
                            Inventory destInv = destinationChest.getInventory();
                            int amount = Arrays.stream(destInv.getContents())
                                    .filter(i -> i != null && i.getType() == Material.DRAGON_EGG)
                                    .mapToInt(ItemStack::getAmount)
                                    .sum();

                            if (amount > 0) {
                                storage.setCache(chestLoc, amount);
                            } else {
                                storage.removeCache(chestLoc);
                            }
                        }, 1L);
                    } else {
                    e.setCancelled(true);
                    }
            } else {
                e.setCancelled(true);
            }
        }
    }

    private static final EnumSet<InventoryType> RESTRICTED_INVENTORIES = EnumSet.of(
            InventoryType.SHULKER_BOX,
            InventoryType.BARREL,
            InventoryType.FURNACE,
            InventoryType.BREWING,
            InventoryType.ENDER_CHEST,
            InventoryType.LECTERN,
            InventoryType.DISPENSER,
            InventoryType.DROPPER,
            InventoryType.HOPPER,
            InventoryType.BLAST_FURNACE,
            InventoryType.STONECUTTER
    );

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player player) {
            Inventory inventory = e.getInventory();
            if (RESTRICTED_INVENTORIES.contains(inventory.getType())) {
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == Material.DRAGON_EGG) {
                        inventory.remove(item);
                        player.getWorld().dropItemNaturally(e.getPlayer().getLocation(), item);
                    }
                }
            }
            if (player.getInventory().contains(Material.DRAGON_EGG) || inventory.contains(Material.DRAGON_EGG)) {
                int amount = Arrays.stream(inventory.getContents()).filter(item -> item != null && item.getType() == Material.DRAGON_EGG).mapToInt(ItemStack::getAmount).sum();
                if (inventory.getLocation()!=null) {
                    plugin.getRunner().runLaterAsync(()-> {
                        if (amount>0) {
                            storage.setCache(inventory.getLocation(), amount);
                        } else {
                            storage.removeCache(inventory.getLocation());
                        }
                    }, 3*20L);
                }
            }
        }

    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType().equals(Material.DRAGON_EGG)) {
            if (!config.isListenersPlace()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType().equals(Material.DRAGON_EGG)) {
            e.getItemDrop().setGlowing(true);
            e.getItemDrop().setCustomName(Colorize.hex(config.getDragonEggName()));
            e.getItemDrop().setCustomNameVisible(true);
        }
    }

    @EventHandler
    public void onPick(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getType().equals(Material.DRAGON_EGG)) {
            e.setCancelled(true);
            ItemStack itemStack = new ItemStack(Material.DRAGON_EGG);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(Colorize.hex(config.getDragonEggName()));
            itemMeta.setLore(Colorize.hex(config.getDragonEggLore()));
            if (config.isDragonEggGlowing()) {
                itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            itemStack.setItemMeta(itemMeta);
            itemStack.setAmount(e.getItem().getItemStack().getAmount());
            e.getPlayer().getInventory().addItem(itemStack);
            e.getItem().remove();
        }
    }

}
