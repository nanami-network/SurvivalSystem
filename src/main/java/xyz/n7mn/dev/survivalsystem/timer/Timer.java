package xyz.n7mn.dev.survivalsystem.timer;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import xyz.n7mn.dev.survivalsystem.SurvivalInstance;
import xyz.n7mn.dev.survivalsystem.playerdata.PlayerData;
import xyz.n7mn.dev.survivalsystem.util.MessageUtil;
import xyz.n7mn.dev.survivalsystem.util.PlayerDataUtil;

public class Timer {
    private long systemTime;

    public static long serverLagSpike;
    private BukkitTask bukkitTask;

    public void start() {
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(SurvivalInstance.INSTANCE.getPlugin(), () -> {

            long now = System.currentTimeMillis() - systemTime - 1000;

            if (now < 0) now = 0;

            serverLagSpike = now;

            this.systemTime = System.currentTimeMillis();

            handle();
        }, 0, 20);

    }

    private void handle() {
        Bukkit.getOnlinePlayers().forEach(onlinePlayers -> {
            PlayerData data = PlayerDataUtil.getPlayerData(onlinePlayers);

            data.getVanishData().handle();
        });
    }

    public void stop() {
        if (bukkitTask != null) bukkitTask.cancel();
    }
}