package me.jetby.treexend.tools;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.treexend.Main;
import me.jetby.treexend.tools.storage.StorageType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.jetby.treexend.tools.LocationHandler.deserializeLocation;

public class TreexEndExpansion extends PlaceholderExpansion {
    private final Main plugin;
    private final StorageType storage;
    private final FormatTime format;
    private final SchedulerHandler schedulerHandler;
    private final Pattern top = Pattern.compile("top_(\\d+)(?:_(coordinates|amount))?");
    private final Pattern topDamage = Pattern.compile("damage_top_(\\d+)(?:_(name|damage))?");


    public TreexEndExpansion(Main plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorageType();
        this.schedulerHandler = plugin.getSchedulerHandler();
        this.format = plugin.getFormatTime();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tend";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        Matcher matcherTop = top.matcher(identifier);
        Matcher matcherTopDamage = topDamage.matcher(identifier);

        if (matcherTopDamage.matches()) {
            String numberStr = matcherTopDamage.group(1);
            String type = matcherTopDamage.group(2);

            int place;
            try {
                place = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                return "---";
            }

            String name = plugin.getDragon().getTopName(place);
            String damage = plugin.getDragon().getTopDamage(place);
            String noFormat = "---";

            if (name == null) {
                return noFormat;
            }

            if ("damage".equalsIgnoreCase(type)) {
                return damage;
            } else if ("name".equalsIgnoreCase(type)) {
                return name;
            }
        }
        if (matcherTop.matches()) {
            String numberStr = matcherTop.group(1);
            String type = matcherTop.group(2);

            int place;
            try {
                place = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                return "---";
            }

            String coordinates = storage.getTop(place);
            int amount = storage.getTopAmount(place);
            String format = plugin.getCfg().getPlaceholderTopFormat();
            String noFormat = "---";

            if (coordinates == null) {
                return noFormat;
            }

            if ("amount".equalsIgnoreCase(type)) {
                return String.valueOf(amount);
            } else if ("coordinates".equalsIgnoreCase(type)) {
                return coordinates.equals("0_0_0_world") ? noFormat : coordinates;
            } else {
                if (amount == 0 || amount == -1 || coordinates.equals("0_0_0_world")) {
                    return noFormat;
                }

                Location loc = deserializeLocation(coordinates, plugin);
                if (loc == null || loc.getWorld() == null) {
                    return noFormat;
                }

                return format
                        .replace("%x%", String.valueOf(loc.getX()))
                        .replace("%y%", String.valueOf(loc.getY()))
                        .replace("%z%", String.valueOf(loc.getZ()))
                        .replace("%world%", loc.getWorld().getName())
                        .replace("%amount%", String.valueOf(amount));
            }
        }
        return switch (identifier.toLowerCase()) {
            case "scheduler_time_to_start" -> String.valueOf(schedulerHandler.getSecondsUntilStart());
            case "scheduler_time_to_end" -> String.valueOf(plugin.getEvent().getTimer());
            case "scheduler_time_to_start_string", "scheduler_time_to_start_format" ->
                    format.stringFormat((int) schedulerHandler.getSecondsUntilStart());
            case "scheduler_time_to_end_string", "scheduler_time_to_end_format" ->
                    format.stringFormat((plugin.getEvent().getTimer()));
            default -> null;
        };
    }
}