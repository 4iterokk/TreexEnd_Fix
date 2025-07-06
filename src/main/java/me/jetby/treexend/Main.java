package me.jetby.treexend;

import lombok.Getter;
import me.jetby.treexend.commands.AdminCommands;
import me.jetby.treexend.commands.TradeCommand;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.configurations.Data;
import me.jetby.treexend.configurations.Scheduler;
import me.jetby.treexend.listeners.DragonEgg;
import me.jetby.treexend.listeners.EndPortal;
import me.jetby.treexend.listeners.EnderDragon;
import me.jetby.treexend.tools.*;
import me.jetby.treexend.tools.storage.Database;
import me.jetby.treexend.tools.storage.StorageType;
import me.jetby.treexend.tools.Version;
import me.jetby.treexend.tools.storage.Yaml;
import me.jetby.treexend.tools.task.BukkitRunner;
import me.jetby.treexend.tools.task.Runner;
import me.jetby.treexend.tools.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;

@Getter
public final class Main extends JavaPlugin {

    private final Config cfg = new Config(this);
    private final Data data = new Data(this);
    private final Scheduler scheduler = new Scheduler(this);
    private final Runner runner = new BukkitRunner(this);
    private StorageType storageType;
    private TreexEndExpansion treexEndExpansion;
    private Database database;
    private Event event;
    private EnderDragon dragon;
    private TaskManager taskManager;
    private Version version;
    private Actions actions;
    private SchedulerHandler schedulerHandler;
    private FormatTime formatTime;
    private BossBarHandler bossBarHandler;
    @Override
    public void onEnable() {
        saveDefaultConfig();

        version = new Version(this);

        for (String string : version.getAlert()) {
            Logger.info(string);
        }

        new Metrics(this, 25881);

        NBTUtil.initialize(this);

        final FileConfiguration configFile = cfg.getFile(getDataFolder().getAbsolutePath(), "config.yml");
        cfg.load(configFile);
        bossBarHandler = new BossBarHandler(this);

        formatTime = new FormatTime(this);
        data.load();

        final FileConfiguration schedulerFile = cfg.getFile(getDataFolder().getAbsolutePath(), "scheduler.yml");
        scheduler.load(schedulerFile);

        schedulerHandler = new SchedulerHandler(this);
        schedulerHandler.start();
        event = new Event(this);
        loadStorage();

        actions = new Actions(this);

        taskManager = new TaskManager(this);
        taskManager.startDragonCheck();
        taskManager.startEggsLocationsChecking();
        taskManager.startEggChecking();
        taskManager.startPriceCheck();
        taskManager.startPortalCheck();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            treexEndExpansion = new TreexEndExpansion(this);
            treexEndExpansion.register();
            Logger.info("Плейсхлодеры успешно зарегистрированы.");
        } else {
            Logger.error("[!]------------------[!]");
            Logger.error("Плагин PlaceholderAPI не был найден! Плагин без него не может работать.");
            Logger.error("[!]------------------[!]");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        dragon = new EnderDragon(this);
        getCommand("tend").setExecutor(new AdminCommands(this));
        getServer().getPluginManager().registerEvents(new EndPortal(this), this);
        getServer().getPluginManager().registerEvents(new DragonEgg(this), this);
        getServer().getPluginManager().registerEvents(dragon, this);
        getServer().getPluginManager().registerEvents(version, this);
        registerCommand(cfg.getTradeCommand(), new TradeCommand(this));

        Logger.success("Плагин готов к работе!");
    }

    public void loadStorage() {
        if (cfg.getStorageType().equalsIgnoreCase("YAML")) {
            storageType = new Yaml(this);
        } else {
            database = new Database(this);
            storageType = database;
        }
    }
    private void registerCommand(String commandName, CommandExecutor executor) {
        try {
            Field commandMapField = getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(getServer());

            Command command = new BukkitCommand(commandName) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return executor.onCommand(sender, this, label, args);
                }
            };

            command.setAliases(Collections.emptyList());

            commandMap.register(getDescription().getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {
        runner.cancelTasks();
        if (storageType!=null) {
            if (storageType.cacheExist()) {
                storageType.save();
                if (!storageType.type().equalsIgnoreCase("YAML")) {
                    database.shutdown();
                }
            }
        } else {
            Logger.warn("Не удалось сохранить кэш.");
        }

        if (treexEndExpansion != null) {
            treexEndExpansion.unregister();
        }
        data.save();
        if (schedulerHandler != null) {
            schedulerHandler.stop();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBarHandler.clearPlayerBossBars(player);
        }
    }
}
