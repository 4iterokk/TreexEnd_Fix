package me.jetby.treexend.tools.storage;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface StorageType {

    String type();
    Map<Location, Integer> getCache();
    void setCache(@NotNull Location location, int amount);
    int getCache(@NotNull Location location);
    boolean containsCache(@NotNull Location location);
    void removeCache(@NotNull Location location);
    boolean cacheExist();
    void save();
    String getTop(int number);
    Location getTopLocation(int number);
    int getTopAmount(int number);


}
