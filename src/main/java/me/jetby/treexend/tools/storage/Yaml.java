package me.jetby.treexend.tools.storage;

import lombok.AccessLevel;
import lombok.Getter;
import me.jetby.treexend.Main;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static me.jetby.treexend.tools.LocationHandler.deserializeLocation;
import static me.jetby.treexend.tools.LocationHandler.serializeLocation;
import static org.bukkit.Bukkit.getLogger;

@Getter
public class Yaml implements StorageType {

    private final Map<Location, Integer> locations = new ConcurrentHashMap<>();
    private final File file;
    private final YamlConfiguration yaml;

    @Getter(AccessLevel.NONE) private final Main plugin;

    public Yaml(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "storage.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
        createOrLoad();
    }

    public void createOrLoad() {
        if (!file.exists()) {
            try {
                plugin.saveResource("storage.yml", false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }}
        load();
        debug("Файл storage.yml успешно загружен.", Level.INFO);
    }

    public void load() {
        ConfigurationSection section = yaml.getConfigurationSection("locations");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            int amount = yaml.getInt("locations." + key + ".amount");
            locations.put(deserializeLocation(key, plugin), amount);
        }
    }


    @Override
    public String type() {
        return "YAML";
    }

    @Override
    public Map<Location, Integer> getCache() {
        return locations;
    }

    @Override
    public void setCache(@NotNull Location location, int amount) {
        plugin.getRunner().runAsync(() ->{locations.put(location, amount);});


    }

    @Override
    public int getCache(@NotNull Location location) {
        return locations.get(location);
    }

    @Override
    public boolean containsCache(@NotNull Location location) {
        return locations.containsKey(location);
    }

    @Override
    public void removeCache(@NotNull Location location) {
        plugin.getRunner().runAsync(() ->{
            locations.put(location, -1);
        });
    }

    @Override
    public boolean cacheExist() {
        return !locations.isEmpty();
    }

    @Override
    public String getTop(int number) {
        if (locations.isEmpty()) return "0_0_0_world";
        List<Map.Entry<Location, Integer>> sorted = new ArrayList<>(locations.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return number <= 0 || number > sorted.size() ? "0_0_0_world" : serializeLocation(sorted.get(number - 1).getKey());
    }

    @Override
    public int getTopAmount(int number) {
        String locationStr = getTop(number);
        if (locationStr == null || locationStr.equals("0_0_0_world")) {
            return 0;
        }

        Location location = deserializeLocation(locationStr, plugin);
        if (location == null) {
            return 0;
        }

        return locations.getOrDefault(location, -1);
    }

    @Override
    public void save() {
            for (Map.Entry<Location, Integer> entry : locations.entrySet()) {
                if (entry.getValue()==-1) continue;
                Location loc = entry.getKey();
                String key = serializeLocation(loc);
                yaml.set("locations." + key + ".amount", entry.getValue());
            }
            try {
                yaml.save(file);
            } catch (IOException e) {
                debug("Не удалось сохранить файл (storage.yml)\n" + e, Level.SEVERE);
            }
    }

    @Override
    public Location getTopLocation(int number) {
        if (locations.isEmpty()) return null;
        List<Map.Entry<Location, Integer>> sorted = new ArrayList<>(locations.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return number <= 0 || number > sorted.size() ? null : sorted.get(number - 1).getKey();
    }

    private void debug(String text, Level level) {
        if (yaml.getBoolean("debug", true)) {
            getLogger().log(level, "[DEBUG] " + text);
        }
    }
}
