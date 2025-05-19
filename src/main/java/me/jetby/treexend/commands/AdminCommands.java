package me.jetby.treexend.commands;

import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.tools.colorizer.Colorize;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static me.jetby.treexend.tools.LocationHandler.deserializeLocation;


public class AdminCommands implements CommandExecutor, TabCompleter {


    final Main plugin;
    final Config config;
    public AdminCommands(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getCfg();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("treexend.admin")) {
            if (args.length==0) {
                sender.sendMessage(Colorize.hex("&d/tend open <время> &7- &fРазрешить вход в портал Энда."));
                sender.sendMessage(Colorize.hex("&d/tend close &7- &fЗапретить вход в портал Энда."));
                sender.sendMessage(Colorize.hex("&d/tend enableTrading &7- &fИгроки могут обменять яйца на приз."));
                sender.sendMessage(Colorize.hex("&d/tend disableTrading &7- &fИгроки не могут обменять яйца на приз."));
                sender.sendMessage(Colorize.hex("&d/tend reload &7- &fПерезагрузить плагин."));
                return true;
                // Идея себе на будущее, если не передумаю или не придумаю вариант лучше
//        sender.sendMessage(Colorize.hex("/tend scheduler start <month:week:day:hours:minutes:seconds> - Запустить авто-запуск эндер мира в нужное время"));
//        sender.sendMessage(Colorize.hex("1) Пример: /tend scheduler start month:June,day:15 - Это значит что каждое 15 июня запустится ивент."));
//        sender.sendMessage(Colorize.hex("2) Пример: /tend scheduler start month:2 - Это значит что каждый месяца будет запускаться ивент."));
//        sender.sendMessage(Colorize.hex("3) Пример: /tend scheduler start week:saturday,hours:17,minutes:30 - Это значит что каждое воскресенье в 17:30 будет запускаться ивент."));
//        sender.sendMessage(Colorize.hex("/tend scheduler stop - Выключить авто-запуск."));
            }

            switch (args[0].toLowerCase()) {
                case "reload": {
                    plugin.getRunner().runAsync(() -> {
                        plugin.reloadConfig();
                        final FileConfiguration configFile = plugin.getCfg().getFile(String.valueOf(plugin.getDataFolder()), "config.yml");
                        plugin.getCfg().load(configFile);
                        if (plugin.getStorageType().cacheExist()) {
                            plugin.getStorageType().save();
                        }
                        plugin.loadStorage();
                        plugin.getTaskManager().startDragonCheck();
                        plugin.getTaskManager().startEggChecking();
                        sender.sendMessage("Config reloaded.");
                    });
                    break;
                }
                case "open": {
                    if (args.length==1) {
                        plugin.getEvent().setEndPortalStatus(true);
                        sender.sendMessage("End portal opened.");
                        break;
                    }
                    if (args.length==2) {
                        plugin.getEvent().start(Integer.parseInt(args[1]));
                        sender.sendMessage("End portal opened for "+ args[1] + " seconds.");
                    }
                    break;
                }
                case "close": {
                    if (args.length==1) {
                        plugin.getEvent().setEndPortalStatus(false);
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            if (players.getWorld().getName().equalsIgnoreCase("world_the_end")) {
                                players.teleport(deserializeLocation(config.getEndCloseTeleport(), plugin));
                            }
                        }
                        sender.sendMessage("End portal closed.");
                        break;
                    }
                    break;
                }
                case "enabletrading": {
                    plugin.getEvent().setTradingStatus(true);
                    sender.sendMessage("Trading enabled.");
                    break;
                }
                case "disabletrading": {
                    plugin.getEvent().setTradingStatus(false);
                    sender.sendMessage("Trading disabled.");
                    break;
                }
            }
            return true;
        }

        return false;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (sender.hasPermission("treexend.admin")) {
            if (args.length==1) {
                completions.add("open");
                completions.add("close");
                completions.add("enableTrading");
                completions.add("disableTrading");
                completions.add("reload");
            }
        }
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(option -> !option.toLowerCase().startsWith(input));
        return completions;
    }
}
