package xyz.n7mn.dev.survivalsystem;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalsystem.advancement.Advancement;
import xyz.n7mn.dev.survivalsystem.cache.GraveCache;
import xyz.n7mn.dev.survivalsystem.customcraft.CustomCraft;
import xyz.n7mn.dev.survivalsystem.customcraft.base.data.ItemDataUtils;
import xyz.n7mn.dev.survivalsystem.customenchant.CustomEnchant;
import xyz.n7mn.dev.survivalsystem.gui.GUIManager;
import xyz.n7mn.dev.survivalsystem.infernal.InfernalManager;
import xyz.n7mn.dev.survivalsystem.itemchecker.InventoryItemChecker;
import xyz.n7mn.dev.survivalsystem.itemchecker.TickChecker;
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

    private final Advancement advancement = new Advancement();

    private final InfernalManager infernalManager = new InfernalManager();

    private final CustomCraft customCraft = new CustomCraft();

    private final CustomEnchant customEnchant = new CustomEnchant();

    private final GUIManager guiManager = new GUIManager();

    private final InventoryItemChecker itemChecker = new InventoryItemChecker();

    private final TickChecker tickChecker = new TickChecker();

    public void init() {
        itemChecker.init();
        tickChecker.init();

        timer.start();

        connection.setUseSQL(plugin.getConfig().getBoolean("useSQL"));
        connection.init();

        GraveCache.init();
        ItemDataUtils.init();

        advancement.init();

        infernalManager.init();

        customCraft.init();
        customEnchant.init();

        guiManager.init();

        SurvivalInstance.INSTANCE.getPlugin().saveResource("dungeons/mine-1-entrance.schem", false);
    }
}