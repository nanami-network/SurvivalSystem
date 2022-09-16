package xyz.n7mn.dev.survivalsystem.customenchant;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class CustomEnchantAbstract extends Enchantment {

    private boolean upgradeable = true;

    public CustomEnchantAbstract(@NotNull NamespacedKey key) {
        super(key);
    }

    public abstract String displayNameToString(final int level);

    public abstract double getEnchantChance(final int level);

    public abstract int getEnchantMax();

    public abstract boolean isActiveEnchant();

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public @NotNull String translationKey() {
        return null;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return null;
    }

    public boolean isUpgradeable() {
        return upgradeable;
    }

    public void setUpgradeable(boolean upgradeable) {
        this.upgradeable = upgradeable;
    }
}