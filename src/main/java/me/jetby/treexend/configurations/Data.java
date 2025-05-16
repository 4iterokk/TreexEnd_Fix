package me.jetby.treexend.configurations;

import lombok.Getter;
import lombok.Setter;
import me.jetby.treexend.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

public class Data {

    private final File file;
    private final YamlConfiguration yaml;
    private final Main plugin;
    @Getter @Setter
    private boolean endPortalStatusData;
    @Getter @Setter private boolean tradingStatusData;

    public Data(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
        createOrLoad();
    }

    public void createOrLoad() {
        if (!file.exists()) {
            try {
                plugin.saveResource("data.yml", false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }}
        load();
        debug("Файл data.yml успешно загружен.", Level.INFO);
    }

    public void load() {
        endPortalStatusData = yaml.getBoolean("endPortalStatus", false);
        tradingStatusData = yaml.getBoolean("tradingStatus", false);
    }
    private void debug(String text, Level level) {
        if (yaml.getBoolean("debug", true)) {
            getLogger().log(level, "[DEBUG] " + text);
        }
    }
    public void save() {
            yaml.set("endPortalStatus", endPortalStatusData);
            yaml.set("tradingStatus", tradingStatusData);
            try {
                yaml.save(file);
            } catch (IOException e) {
                debug("Не удалось сохранить файл (storage.yml)\n" + e, Level.SEVERE);
            }


    }
}
