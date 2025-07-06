package me.jetby.treexend.configurations;

import lombok.Getter;
import lombok.Setter;
import me.jetby.treexend.Main;
import me.jetby.treexend.tools.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

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
        Logger.info("data.yml успешно загружен.");
    }

    public void load() {
        endPortalStatusData = yaml.getBoolean("endPortalStatus", false);
        tradingStatusData = yaml.getBoolean("tradingStatus", false);
    }

    public void save() {
            yaml.set("endPortalStatus", endPortalStatusData);
            yaml.set("tradingStatus", tradingStatusData);
            try {
                yaml.save(file);
            } catch (IOException e) {
                Logger.warn("Не удалось сохранить файл (storage.yml)\n" + e);
            }
    }
}
