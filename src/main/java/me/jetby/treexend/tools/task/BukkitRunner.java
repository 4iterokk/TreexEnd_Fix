package me.jetby.treexend.tools.task;

import me.jetby.treexend.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;


public final class BukkitRunner implements Runner {

    private final Main plugin;
    private final BukkitScheduler scheduler;
    private int taskId;
    public BukkitRunner(Main plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void runPlayer(@NotNull Runnable task, @NotNull Player player) {
        run(task);
    }

    @Override
    public void run(@NotNull Runnable task) {
        taskId = scheduler.runTask(plugin, task).getTaskId();
    }

    @Override
    public void runAsync(@NotNull Runnable task) {
        taskId = scheduler.runTaskAsynchronously(plugin, task).getTaskId();
    }

    @Override
    public void runLater(@NotNull Runnable task, long delayTicks) {
        taskId = scheduler.runTaskLater(plugin, task, delayTicks).getTaskId();
    }

    @Override
    public void runLaterAsync(@NotNull Runnable task, long delayTicks) {
        taskId = scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks).getTaskId();
    }

    @Override
    public void startTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        taskId = scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks).getTaskId();
    }

    @Override
    public void startTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        taskId = scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks).getTaskId();
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelTasks(plugin);
    }
    @Override
    public void cancelTask(int taskId) {
        scheduler.cancelTask(taskId);
    }

    @Override
    public int getTaskdId() {
        return taskId;
    }

}