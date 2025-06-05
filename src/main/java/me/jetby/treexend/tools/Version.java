package me.jetby.treexend.tools;

import me.jetby.treexend.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class Version implements Listener {
    private final Main plugin;
    private final String versionLink = "https://raw.githubusercontent.com/MrJetby/TreexEnd/refs/heads/master/VERSION";
    private final String updateLink = "https://raw.githubusercontent.com/MrJetby/TreexEnd/refs/heads/master/UPDATE_LINK";

    public Version(Main plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("treexend.version")) {
            if (!isLastVersion()) {
                plugin.getRunner().runAsync(()->{
                    player.sendMessage("");
                    player.sendMessage("§7-------- §dTreexEnd §7--------");
                    player.sendMessage("§d● §fВнимание, доступно обновление, пожалуйста обновите плагин.");
                    player.sendMessage("§d● §7Ваша версия: §c" + getVersion() + " §7а последняя §a" + getLastVersion());
                    player.sendMessage("");
                    player.sendMessage("§d● §fСкачать тут: §b"+getUpdateLink());
                    player.sendMessage("§7-------------------------");
                    player.sendMessage("");
                });

            }
        }
    }

    private String getRaw(String link) {
        try {
            URL url = new URL(link);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder builder = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            in.close();
            return builder.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLastVersion() {
        String result = getRaw(versionLink);
        assert result != null;
        return result;
    }
    public String getUpdateLink() {
        String result = getRaw(updateLink);
        assert result != null;
        return result;
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public boolean isLastVersion() {
        String result = getRaw(versionLink);
        if (result == null) {
            return true;
        }

        return plugin.getDescription().getVersion().equalsIgnoreCase(getLastVersion());
    }

}
