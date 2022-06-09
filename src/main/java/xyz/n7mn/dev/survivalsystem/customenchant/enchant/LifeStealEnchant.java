package xyz.n7mn.dev.survivalsystem.customenchant.enchant;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.n7mn.dev.survivalsystem.SurvivalInstance;
import xyz.n7mn.dev.survivalsystem.customenchant.CustomEnchantAbstract;
import xyz.n7mn.dev.survivalsystem.util.ItemStackUtil;
import xyz.n7mn.dev.survivalsystem.util.RomanNumber;

import java.util.Set;

public class LifeStealEnchant extends CustomEnchantAbstract {
    public LifeStealEnchant() {
        super(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "enchant_life_steal"));
    }

    @Override
    public String displayNameToString(int level) {
        return ChatColor.RED + "HP吸収 " + RomanNumber.toRoman(level);
    }

    @Override
    public double getEnchantChance(int level) {
        return level * 0.3;
    }

    @Override
    public int getEnchantMax() {
        return 3;
    }

    @Override
    public boolean isActiveEnchant() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "LifeSteal".toUpperCase();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return ItemStackUtil.isBook(item) || ItemStackUtil.isSword(item);
    }

    @Override
    public @NotNull Component displayName(int level) {
        return Component.text(displayNameToString(level));
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return null;
    }

    @Override
    public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return null;
    }

    @Override
    public @NotNull String translationKey() {
        return null;
    }
}