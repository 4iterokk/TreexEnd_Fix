package me.jetby.treexend.tools.colorizer;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Colorize {
    private final ColorizerType colorizer = new LegacyColorizer();
    public static String setPlaceholders(String text, Player player) {
        text = PlaceholderAPI.setPlaceholders(player, text);
        return text;
    }
    public String hex(String message) {
        if (message == null || message.isEmpty()) return message;
        return colorizer.colorize(message);
    }

    public List<String> hex(List<String> list) {
        List<String> colored = new ArrayList<>();
        for (var str : list) colored.add(hex(str));
        return colored;
    }
}
