package me.jetby.treexend.tools.storage;

import me.jetby.treexend.Main;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

import static me.jetby.treexend.tools.LocationHandler.deserializeLocation;
import static me.jetby.treexend.tools.LocationHandler.serializeLocation;

public class Database implements StorageType {

    private final Connection connection;
    private final boolean useMySQL;
    private final Main plugin;
    private final Map<Location, Integer> cache = new ConcurrentHashMap<>(512);
    private final ConcurrentLinkedQueue<Map.Entry<Location, Integer>> changesQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService syncScheduler = Executors.newSingleThreadScheduledExecutor();

    public Database(Main plugin) throws SQLException {
        this.plugin = plugin;
        this.useMySQL = plugin.getCfg().getStorageType().equalsIgnoreCase("MYSQL");
        this.connection = connect();
        createTable();
        initializeCache();
        startSyncDaemon();
    }
    private void createTable() {
        try (Statement stmt = connection.createStatement()) {
            String sql = useMySQL ?
                    "CREATE TABLE IF NOT EXISTS locations (" +
                            "location VARCHAR(255) PRIMARY KEY, " +
                            "amount INT NOT NULL" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" :
                    "CREATE TABLE IF NOT EXISTS locations (" +
                            "location TEXT PRIMARY KEY, " +
                            "amount INTEGER NOT NULL" +
                            ")";
            stmt.executeUpdate(sql);

            if (useMySQL) {
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_amount ON locations(amount)");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка создания таблицы", e);
        }
    }
    private void initializeCache() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT location, amount FROM locations")) {
            while (rs.next()) {
                Location loc = deserializeLocation(rs.getString("location"), plugin);
                cache.put(loc, rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка инициализации кэша", e);
        }
    }
    private void startSyncDaemon() {
        syncScheduler.scheduleAtFixedRate(() -> {
            if (!changesQueue.isEmpty()) {
                batchSync();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    private void batchSync() {
        int BATCH_SIZE = 500;
        List<Map.Entry<Location, Integer>> batch = new ArrayList<>(BATCH_SIZE);

        synchronized (changesQueue) {
            while (!changesQueue.isEmpty() && batch.size() < BATCH_SIZE) {
                batch.add(changesQueue.poll());
            }
        }

        if (!batch.isEmpty()) {
            processBatch(batch);
        }
    }
    private void processBatch(List<Map.Entry<Location, Integer>> batch) {
        String upsertSQL = useMySQL ?
                "INSERT INTO locations (location, amount) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE amount = VALUES(amount)" :
                "INSERT OR REPLACE INTO locations (location, amount) VALUES (?, ?)";

        String deleteSQL = "DELETE FROM locations WHERE location = ?";

        try (
                PreparedStatement upsertStmt = connection.prepareStatement(upsertSQL);
                PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL)
        ) {
            for (Map.Entry<Location, Integer> entry : batch) {
                String locString = serializeLocation(entry.getKey());
                Integer value = entry.getValue();

                if (value == null) {
                    deleteStmt.setString(1, locString);
                    deleteStmt.addBatch();
                } else {
                    upsertStmt.setString(1, locString);
                    upsertStmt.setInt(2, value);
                    upsertStmt.addBatch();
                }
            }
            upsertStmt.executeBatch();
            deleteStmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка пакетной синхронизации", e);
        }
    }

    private Connection connect() throws SQLException {
        if (useMySQL) {
            String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false",
                    plugin.getCfg().getStorageHost(),
                    plugin.getCfg().getStoragePort(),
                    plugin.getCfg().getStorageDatabase());
            return DriverManager.getConnection(url,
                    plugin.getCfg().getStorageUsername(),
                    plugin.getCfg().getStoragePassword());
        } else {
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/storage.db");
        }
    }

    @Override
    public String type() {
        if (useMySQL) {
            return "MYSQL";
        }
        return "SQLITE";
    }

    @Override
    public Map<Location, Integer> getCache() {
        return cache;
    }

    @Override
    public void setCache(@NotNull Location location, int amount) {
        plugin.getRunner().runAsync(() -> {
            cache.put(location, amount);
            changesQueue.offer(new AbstractMap.SimpleEntry<>(location, amount));
        });
    }

    @Override
    public int getCache(@NotNull Location location) {
        return cache.getOrDefault(location, 0);
    }

    @Override
    public boolean containsCache(@NotNull Location location) {
        return cache.containsKey(location);
    }

    @Override
    public void removeCache(@NotNull Location location) {
        plugin.getRunner().runAsync(() -> {
            cache.remove(location);
            changesQueue.offer(new AbstractMap.SimpleEntry<>(location, null)); // null = удалить
        });
    }



    @Override
    public boolean cacheExist() {
        return !cache.isEmpty();
    }

    @Override
    public String getTop(int number) {
        return cache.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .skip(number - 1L)
                .findFirst()
                .map(entry -> serializeLocation(entry.getKey()))
                .orElse("0_0_0_world");
    }



    @Override
    public Location getTopLocation(int number) {
        return deserializeLocation(getTop(number), plugin);
    }

    @Override
    public int getTopAmount(int number) {
        return cache.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .skip(number - 1L)
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(0);
    }

    @Override
    public void save() {
        batchSync();
    }
    public void shutdown() {
        syncScheduler.shutdown();
        try {
            if (!syncScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                syncScheduler.shutdownNow();
            }
            save();
            if (connection != null) connection.close();
            plugin.getLogger().log(Level.SEVERE, "Данные успешно сохранены.");
        } catch (SQLException | InterruptedException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка завершения работы", e);
        }
    }
}