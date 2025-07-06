package me.jetby.treexend.tools.storage;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface StorageType {

    String type();
    Map<Location, Integer> getLocationCache();
    void setLocationCache(@NotNull Location location, int amount);
    int getLocationCache(@NotNull Location location);
    boolean containsLocationCache(@NotNull Location location);
    void setPriceCache(@NotNull Location location, EggPrices eggPrices);
    Map<Location, EggPrices> getPriceCache();
    EggPrices getEggPrice(Location location);
    void removeCache(@NotNull Location location);
    boolean cacheExist();
    void save();
    String getTop(int number);
    Location getTopLocation(int number);
    int getTopAmount(int number);


}
