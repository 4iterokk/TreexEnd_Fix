package me.jetby.treexend.configurations;

import lombok.Getter;
import me.jetby.treexend.Main;
import me.jetby.treexend.tools.colorizer.Colorize;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Scheduler {
    private boolean enable;
    private String timezone;
    private List<String> times;
    private List<String> onStart;
    private List<String> onEnd;
    private List<String> preStartActions;
    private List<Integer> preStartTimes;

    private final Main plugin;
    public Scheduler(Main plugin) {
        this.plugin = plugin;
    }

    public void load(FileConfiguration configuration) {
        List<String> timesDefault = new ArrayList<>();
        times = getOrDefaultList(configuration, "scheduler.times", timesDefault);

        List<String> onStartDefault = new ArrayList<>();
        onStart = getOrDefaultList(configuration, "scheduler.actions.onStart", onStartDefault);

        List<String> onEndDefault = new ArrayList<>();
        onEnd = getOrDefaultList(configuration, "scheduler.actions.onEnd", onEndDefault);

        List<String> preStartActionsDefault = new ArrayList<>();
        preStartActions = getOrDefaultList(configuration, "scheduler.actions.preStart.actions", preStartActionsDefault);

        List<Integer> preStartTimesDefault = new ArrayList<>();
        preStartTimes = getOrDefaultInt(configuration, "scheduler.actions.preStart.times", preStartTimesDefault);

        enable = configuration.getBoolean("enable", false);

        timezone = configuration.getString("scheduler.timezone", "GMT+3");

    }

    private List<String> getOrDefaultList(FileConfiguration config, String path, List<String> defaultValue) {
        List<String> list = config.getStringList(path);
        defaultValue.replaceAll(Colorize::hex);
        list.replaceAll(Colorize::hex);
        return list.isEmpty() ? defaultValue : list;
    }
    private List<Integer> getOrDefaultInt(FileConfiguration config, String path, List<Integer> defaultValue) {
        List<Integer> list = config.getIntegerList(path);
        return list.isEmpty() ? defaultValue : list;
    }

    public FileConfiguration getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
    public void save(String path, FileConfiguration config, String fileName) {
        plugin.getRunner().runAsync(() -> {
            try {
                config.save(new File(path, fileName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
