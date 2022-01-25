package xyz.n7mn.dev.survivalsystem;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalsystem.cache.GraveCache;
import xyz.n7mn.dev.survivalsystem.sql.SQLConnection;
import xyz.n7mn.dev.survivalsystem.timer.Timer;

import java.util.Objects;


@Getter
public enum SurvivalInstance {

    INSTANCE;

    @NotNull
    private final Plugin plugin = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("SurvivalSystem"));

    private final Timer timer = new Timer();

    private final SQLConnection connection = new SQLConnection();

    public void init() {
        timer.start();

        connection.setUseSQL(plugin.getConfig().getBoolean("useSQL"));
        connection.init();

        GraveCache.init();
    }
}