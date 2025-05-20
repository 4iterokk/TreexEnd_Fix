package me.jetby.treexend.tools;

import lombok.AccessLevel;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.colorizer.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@Getter
public class BossBarHandler {
    @Getter(AccessLevel.NONE) private final Config config;
    private final Map<UUID, BossBar> eggBossBars;
    private final Map<UUID, BossBar> durationBossBars;

    public BossBarHandler(Main plugin) {
        config = plugin.getCfg();
        eggBossBars = new HashMap<>();
        durationBossBars = new HashMap<>();
    }

    public void removeEggBossbar(Player player) {
        if (eggBossBars.containsKey(player.getUniqueId())) {
            BossBar oldBar = eggBossBars.get(player.getUniqueId());
            oldBar.removePlayer(player);
            eggBossBars.remove(player.getUniqueId());
        }
    }
    public void sendEggBossbar(Player player) {
        String barTitle = Colorize.hex(config.getBarEggTitle());
        BarStyle barStyle = config.getBarStyleEgg();
        BarColor barColor = config.getBarColorEgg();
        if (eggBossBars.containsKey(player.getUniqueId())) {
            BossBar oldBar = eggBossBars.get(player.getUniqueId());
            oldBar.setTitle(barTitle);
            return;
        }
        BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle);
        bossBar.addPlayer(player);
        eggBossBars.put(player.getUniqueId(), bossBar);
    }
    public void removeDurationBossbar(Player player) {
        if (durationBossBars.containsKey(player.getUniqueId())) {
            BossBar oldBar = durationBossBars.get(player.getUniqueId());
            oldBar.removePlayer(player);
            durationBossBars.remove(player.getUniqueId());
        }
    }
    public void sendDurationBossbar(Player player) {
        String barTitle = PlaceholderAPI.setPlaceholders(null, Colorize.hex(config.getBarDurationTitle()));
        BarStyle barStyle = config.getBarStyleDuration();
        BarColor barColor = config.getBarColorDuration();
        if (durationBossBars.containsKey(player.getUniqueId())) {
            BossBar oldBar = durationBossBars.get(player.getUniqueId());
            oldBar.setTitle(barTitle);
            return;
        }
        BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle);
        bossBar.addPlayer(player);
        durationBossBars.put(player.getUniqueId(), bossBar);
    }

    public void clearPlayerBossBars(Player player) {
        UUID uuid = player.getUniqueId();

        if (eggBossBars.containsKey(uuid)) {
            eggBossBars.get(uuid).removeAll();
            eggBossBars.remove(uuid);
        }

        if (durationBossBars.containsKey(uuid)) {
            durationBossBars.get(uuid).removeAll();
            durationBossBars.remove(uuid);
        }
    }
}