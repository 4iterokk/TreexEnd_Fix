package me.jetby.treexend.tools;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Logger {
    private static final java.util.logging.Logger logger = Bukkit.getLogger();

    public void warn(String message) {
        logger.warning("§7[§eTreexEnd§7] §6WARN §e" + message);
    }

    public void info(String message) {
        logger.info("§7[§aTreexEnd§7] §aINFO §f" + message);
    }
    public void success(String message) {
        logger.info("§7[§aTreexEnd§7] §aINFO §a" + message);
    }
    public void error(String message) {
        logger.warning("§7[§cTreexEnd§7] §4ERROR §c" + message);
    }
}