package me.jetby.treexend.listeners;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.NBTUtil;
import me.jetby.treexend.tools.colorizer.Colorize;
import me.jetby.treexend.tools.storage.EggPrices;
import me.jetby.treexend.tools.storage.StorageType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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
            if (RESTRICTED_INVENTORIES.contains(topInventory.getType()) || topInventory.getHolder() instanceof Minecart) {
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
    public void ЧтобыВоронкаНеСосала(InventoryPickupItemEvent event) {
        if (event.getItem().getItemStack().getType().equals(Material.DRAGON_EGG)) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void ОтменаФрейма(PlayerInteractEntityEvent e) {
        if(e.getRightClicked()  instanceof ItemFrame) {
            if (e.getPlayer().getItemInHand().getType()==Material.DRAGON_EGG) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onItemMoved(InventoryMoveItemEvent e) {

        ItemStack item = e.getItem();
        if (item.getType() == Material.DRAGON_EGG) {
            if (e.getDestination().getLocation() != null) {
                if (e.getDestination().getHolder() instanceof Chest destinationChest) {
                    Location chestLoc = destinationChest.getBlock().getLocation();
                    int eggPrice = NBTUtil.getEggPrice(item);

                    plugin.getRunner().runLater(() -> {
                        Inventory destInv = destinationChest.getInventory();

                        int targetSlot = -1;
                        for (int i = 0; i < destInv.getSize(); i++) {
                            if (item.equals(destInv.getItem(i))) {
                                targetSlot = i;
                                break;
                            }
                        }

                        if (targetSlot == -1) return;

                        EggPrices existingPrices = storage.getPriceCache().getOrDefault(
                                chestLoc,
                                new EggPrices(new ArrayList<>(), new ArrayList<>())
                        );

                        List<Integer> slots = new ArrayList<>(existingPrices.slots());
                        List<Integer> prices = new ArrayList<>(existingPrices.prices());

                        if (slots.contains(targetSlot)) {
                            int index = slots.indexOf(targetSlot);
                            prices.set(index, eggPrice > 0 ? eggPrice : config.getPrice());
                        } else {
                            slots.add(targetSlot);
                            prices.add(eggPrice > 0 ? eggPrice : config.getPrice());
                        }

                        storage.setPriceCache(chestLoc, new EggPrices(slots, prices));

                        int amount = (int) Arrays.stream(destInv.getContents())
                                .filter(i -> i != null && i.getType() == Material.DRAGON_EGG)
                                .count();
                        storage.setLocationCache(chestLoc, amount);

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
    public void onOpen(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player) {
            if (config.isGrowing()) {
                Inventory inventory = e.getView().getTopInventory();
                if (inventory.getHolder() instanceof Chest chest) {
                    Location chestLoc = chest.getLocation();
                    if (storage.getPriceCache().containsKey(chestLoc)) {
                        EggPrices eggPrices = storage.getEggPrice(chestLoc);

                        List<Integer> slots = new ArrayList<>(eggPrices.slots());
                        List<Integer> prices = new ArrayList<>(eggPrices.prices());

                        if (slots.size() != prices.size()) {
                            plugin.getLogger().warning("Несоответствие размеров slots и prices в сундуке " + chestLoc);
                            return;
                        }

                        for (int i = 0; i < slots.size(); i++) {
                            int slot = slots.get(i);

                            if (slot < 0 || slot >= inventory.getSize()) {
                                continue;
                            }

                            ItemStack item = inventory.getItem(slot);
                            if (item == null || item.getType() != Material.DRAGON_EGG) {
                                continue;
                            }

                            int price = prices.get(i);

                            NBTUtil.setEggPrice(item, price, config.isAntistack());

                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                List<String> lore = new ArrayList<>(config.getDragonEggLore());
                                lore.replaceAll(s -> s.replace("%egg_price%", String.valueOf(price)));
                                meta.setLore(lore);

                                if (config.getDragonEggName() != null && !config.getDragonEggName().isEmpty()) {
                                    meta.setDisplayName(Colorize.hex(config.getDragonEggName()));
                                }

                                if (config.isDragonEggGlowing()) {
                                    meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                }

                                item.setItemMeta(meta);
                            }
                        }
                    }
                }
            }
        }
    }

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

            if (inventory.getLocation() != null) {
                plugin.getRunner().runLaterAsync(() -> {
                    List<Integer> slotsWithEggs = new ArrayList<>();
                    List<Integer> pricesForSlots = new ArrayList<>();

                    EggPrices cachedPrices = storage.getPriceCache().getOrDefault(
                            inventory.getLocation(),
                            new EggPrices(new ArrayList<>(), new ArrayList<>())
                    );

                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        ItemStack item = inventory.getItem(slot);
                        if (item != null && item.getType() == Material.DRAGON_EGG) {
                            int currentPrice = NBTUtil.getEggPrice(item);
                            int cachedPrice = config.getPrice();

                            if (cachedPrices.slots().contains(slot)) {
                                int index = cachedPrices.slots().indexOf(slot);
                                cachedPrice = cachedPrices.prices().get(index);
                            }

                            int finalPrice = Math.min(currentPrice, cachedPrice);
                            if (finalPrice <= 0) {
                                finalPrice = config.getPrice();
                            }

                            NBTUtil.setEggPrice(item, finalPrice, config.isAntistack());

                            slotsWithEggs.add(slot);
                            pricesForSlots.add(finalPrice);
                        }
                    }

                    if (!slotsWithEggs.isEmpty()) {
                        storage.setPriceCache(
                                inventory.getLocation(),
                                new EggPrices(slotsWithEggs, pricesForSlots)
                        );
                        storage.setLocationCache(
                                inventory.getLocation(),
                                slotsWithEggs.size()
                        );
                    } else {
                        storage.removeCache(inventory.getLocation());
                    }
                }, 2L);
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
            ItemStack item = e.getItemDrop().getItemStack();
            int currentPrice = NBTUtil.getEggPrice(item);

            if (currentPrice == 0) {
                currentPrice = config.getPrice();
                NBTUtil.setEggPrice(item, currentPrice, config.isAntistack());
            }

            e.getItemDrop().setGlowing(true);
            e.getItemDrop().setCustomName(Colorize.hex(config.getDragonEggName()));
            e.getItemDrop().setCustomNameVisible(true);
        }
    }

    @EventHandler
    public void onPick(PlayerPickupItemEvent e) {
        if (e.getItem().getItemStack().getType().equals(Material.DRAGON_EGG)) {
            e.setCancelled(true);
            ItemStack droppedItem = e.getItem().getItemStack();

            int existingPrice = NBTUtil.getEggPrice(droppedItem);
            int priceToSet = existingPrice > 0 ? existingPrice : config.getPrice();

            ItemStack newItem = new ItemStack(Material.DRAGON_EGG);
            newItem.setAmount(droppedItem.getAmount());

            NBTUtil.setEggPrice(newItem, priceToSet, config.isAntistack());

            ItemMeta meta = newItem.getItemMeta();
            List<String> lore = new ArrayList<>(config.getDragonEggLore());
            lore.replaceAll(s -> s.replace("%egg_price%", String.valueOf(priceToSet)));
            meta.setDisplayName(Colorize.hex(config.getDragonEggName()));
            meta.setLore(lore);

            if (config.isDragonEggGlowing()) {
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            newItem.setItemMeta(meta);
            e.getPlayer().getInventory().addItem(newItem);
            e.getItem().remove();
        }
    }

}
