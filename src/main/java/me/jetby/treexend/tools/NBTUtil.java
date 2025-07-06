package me.jetby.treexend.tools;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class NBTUtil {
    private static NamespacedKey EGG_PRICE_KEY;
    private static NamespacedKey ANTI_STACK;

    public static void initialize(Plugin plugin) {
        EGG_PRICE_KEY = new NamespacedKey(plugin, "egg_price");
        ANTI_STACK = new NamespacedKey(plugin, "antistack");
    }

    public static void setEggPrice(ItemStack item, int price, boolean antistack) {
        if (item == null || item.getType() != Material.DRAGON_EGG) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(EGG_PRICE_KEY, PersistentDataType.INTEGER, price);

        if (antistack) {
            pdc.set(ANTI_STACK, PersistentDataType.STRING, UUID.randomUUID().toString());
        }

        item.setItemMeta(meta);
    }

    public static int getEggPrice(ItemStack item) {
        if (item == null || item.getType() != Material.DRAGON_EGG) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(EGG_PRICE_KEY, PersistentDataType.INTEGER, 0);
    }
}