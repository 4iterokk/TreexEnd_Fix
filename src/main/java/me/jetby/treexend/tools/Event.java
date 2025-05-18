package me.jetby.treexend.tools;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treexend.Main;
import me.jetby.treexend.configurations.Config;
import me.jetby.treexend.configurations.Data;
import me.jetby.treexend.tools.task.Runner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static me.jetby.treexend.tools.LocationHandler.deserializeLocation;

@Getter @Setter
public class Event {
    private int timer;
    private boolean endPortalStatus;
    private boolean tradingStatus;
    @Getter(AccessLevel.NONE) private final Runner runner;
    @Getter(AccessLevel.NONE) private final Data data;
    @Getter(AccessLevel.NONE) private final Config config;
    @Getter(AccessLevel.NONE) private final Main plugin;

    public void setEndPortalStatus(boolean endPortalStatus) {
        data.setEndPortalStatusData(endPortalStatus);
        this.endPortalStatus = endPortalStatus;
    }
    public void setTradingStatus(boolean tradingStatus) {
        data.setTradingStatusData(tradingStatus);
        this.tradingStatus = tradingStatus;
    }

    public Event(Main plugin) {
        this.plugin = plugin;
        this.runner = plugin.getRunner();
        this.data = plugin.getData();
        this.config = plugin.getCfg();
    }

    public void start(int time) {
        setEndPortalStatus(true);
        if (timer==0) {
            timer=time;
        }
        runner.startTimer(() -> {
            if (timer<=0) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (players.getWorld().getName().equalsIgnoreCase("world_the_end")) {
                        players.teleport(deserializeLocation(config.getEndCloseTeleport(), plugin));
                    }
                }
                setEndPortalStatus(false);
                data.setEndPortalStatusData(endPortalStatus);
                runner.cancelTask(runner.getTaskdId());
            } else {
                setTimer(getTimer()-1);
            }
        }, 0, 20L);
    }


}
