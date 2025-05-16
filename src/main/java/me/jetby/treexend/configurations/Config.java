package me.jetby.treexend.configurations;

import me.jetby.treexend.Main;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class Config {
    private List<String> chestOnly;
    private List<String> noEggs;
    private List<String> portalIsBlocked;
    private List<String> endIsClose;
    private List<String> tradingIsDisabled;
    private List<String> dragonDamage;
    private int dragonMessageDelay;
    private String placeholderTopFormat;
    private String dragonEggName;
    private List<String> dragonEggLore;
    private List<String> dragonEggPrizes;
    private boolean listenersDropOnQuit;
    private boolean listenersPlace;
    private boolean dragonEggGlowing;
    private boolean listenersTeleportOnPortalJoin;
    private String storageType;
    private String storageHost;
    private int storagePort;
    private String storageDatabase;
    private String storageUsername;
    private String storagePassword;
    private String tradeCommand;
    private String endCloseTeleport;
    private String endEnterTeleport;
    private final FileConfiguration configuration;
    public Config(Main plugin) {
        this.configuration = plugin.getConfig();
        load();
    }

    public void load(){
        if (!configuration.getStringList("messages.chestOnly").isEmpty()) chestOnly = configuration.getStringList("messages.chestOnly");
        else {
            chestOnly.add("&cЯйцо Дракона можно хранить только в сундуке.");
        }
        if (!configuration.getStringList("messages.endIsClose").isEmpty()) endIsClose = configuration.getStringList("messages.endIsClose");
        else {
            endIsClose.add("&fЭндер мир сейчас закрыт, возвращайтесь в &bвоскресенье в 16:00 &fпо МСК");
        }
        if (!configuration.getStringList("messages.tradingIsDisabled").isEmpty()) tradingIsDisabled = configuration.getStringList("messages.tradingIsDisabled");
        else {
            tradingIsDisabled.add("&cСейчас вы не можете обменять яйца.");
        }
        if (!configuration.getStringList("messages.noEggs").isEmpty()) noEggs = configuration.getStringList("messages.noEggs");
        else {
            noEggs.add("&cУ вас нету яиц дракона для получения приза!");
        }
        if (!configuration.getStringList("messages.portalIsBlocked").isEmpty()) portalIsBlocked = configuration.getStringList("messages.portalIsBlocked");
        placeholderTopFormat = configuration.getString("placeholder-top-format", "x: %x%, y: %y%, z: %z%");
        dragonEggName = configuration.getString("dragon-egg.name", "&dЯйцо дракона");
        if (!configuration.getStringList("dragon-egg.lore").isEmpty()) dragonEggLore = configuration.getStringList("dragon-egg.lore");
        else {
            dragonEggLore.add("");
            dragonEggLore.add("&#FB0783&n◢&f В ваших руках самый ценный предмет.");
            dragonEggLore.add("&#FB0783◤&f Будьте с ним осторожнее!");
            dragonEggLore.add("");
            dragonEggLore.add("&#FB43F9Вот его особенности:");
            dragonEggLore.add("&#FB0783● &fВ конце вайпа яйцо(а) можно");
            dragonEggLore.add("  &fобменять на Сапфиры - &#FB0783/egg-swap");
            dragonEggLore.add("  &fКурс обмена: &#FB07831 яйцо &f= &#FB07832500 сапфиров");
            dragonEggLore.add("&#FB0783● &fПрятать яйца можно только");
            dragonEggLore.add("&#FB0783  в обычном сундуке.");
            dragonEggLore.add("&#FB0783● &fНа спавне формируется топ,");
            dragonEggLore.add("  &fпо спрятанным яйцам - &#FB0783/warp top");
            dragonEggLore.add("");
        }
        if (!configuration.getStringList("dragon-egg.prizes").isEmpty()) dragonEggPrizes = configuration.getStringList("dragon-egg.prizes");
        else {
            dragonEggPrizes.add("");
        }
        listenersDropOnQuit = configuration.getBoolean("listeners.dropOnQuit", true);
        listenersPlace = configuration.getBoolean("dragon-egg.glowing", true);
        listenersTeleportOnPortalJoin = configuration.getBoolean("teleportOnPortalJoin", false);
        dragonEggGlowing = configuration.getBoolean("listeners.place", true);


        // database
        storageType = configuration.getString("storage.type", "YAML");
        storageHost = configuration.getString("storage.host", "localhost");
        storagePort = configuration.getInt("storage.port", 3306);
        storageDatabase = configuration.getString("storage.database", "treexend");
        storageUsername = configuration.getString("storage.username", "root");
        storagePassword = configuration.getString("storage.password", "");
        tradeCommand = configuration.getString("trade-command", "egg-swap");
        endEnterTeleport = configuration.getString("end-enter-teleport");
        endCloseTeleport = configuration.getString("end-close-teleport");


        if (!configuration.getStringList("messages.dragonDamage.message").isEmpty()) dragonDamage = configuration.getStringList("messages.dragonDamage.message");
        else {
            dragonDamage.add("");
            dragonDamage.add("Могущественный Дракон &#d27c33скоро будет поврежен, &fи всё");
            dragonDamage.add("благодаря этим воинам:");
            dragonDamage.add("&#ebcd35▪ 1 место &7- &#3dcde0%tend_damage_top_1_name% &7| &#d27c33%tend_damage_top_1_damage%");
            dragonDamage.add("&f▪ 2 место &7- &#3dcde0%tend_damage_top_2_name% &7| &#d27c33%tend_damage_top_2_damage%");
            dragonDamage.add("&#d27c33▪ 3 место &7- &#3dcde0%tend_damage_top_3_name% &7| &#d27c33%tend_damage_top_3_damage%");
            dragonDamage.add("");
            dragonDamage.add("Игрок, занявший первое место, &#ebcd35&lполучит Яйцо дракона.");
            dragonDamage.add("");
        }

        dragonMessageDelay = configuration.getInt("messages.dragonDamage.messageDelay", 60);
    }

    public String getString(String name) {
        return switch (name) {
            case "placeholder-top-format" -> placeholderTopFormat;
            case "dragon-egg.name" -> dragonEggName;
            case "storage.type" -> storageType;
            case "storage.host" -> storageHost;
            case "storage.database" -> storageDatabase;
            case "storage.username" -> storageUsername;
            case "storage.password" -> storagePassword;
            case "trade-command" -> tradeCommand;
            case "end-close-teleport" -> endCloseTeleport;
            case "end-enter-teleport" -> endEnterTeleport;
            default -> configuration.getString(name, name);
        };
    }
    public int getInt(String name) {
        return switch (name) {
            case "storage.port" -> storagePort;
            case "messages.dragonDamage.messageDelay" -> dragonMessageDelay;
            default -> configuration.getInt(name, 0);
        };
    }

    public boolean getBoolean(String name) {
        return switch (name) {
            case "dragon-egg.glowing" -> dragonEggGlowing;
            case "listeners.dropOnQuit" -> listenersDropOnQuit;
            case "listeners.place" -> listenersPlace;
            case "listeners.teleportOnPortalJoin" -> listenersTeleportOnPortalJoin;
            default ->  configuration.getBoolean(name, false);
        };
    }

    public List<String> getList(String name) {
        return switch (name) {
            case "dragon-egg.lore" -> dragonEggLore;
            case "dragon-egg.prizes" -> dragonEggPrizes;
            case "messages.dragonDamage.message" -> dragonDamage;
            case "messages.chestOnly" -> chestOnly;
            case "messages.portalIsBlocked" -> portalIsBlocked;
            case "messages.noEggs" -> noEggs;
            case "messages.endIsClose" -> endIsClose;
            case "messages.tradingIsDisabled" -> tradingIsDisabled;
            default -> configuration.getStringList(name);
        };
    }

}
