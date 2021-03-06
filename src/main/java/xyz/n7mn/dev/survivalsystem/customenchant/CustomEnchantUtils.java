package xyz.n7mn.dev.survivalsystem.customenchant;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import xyz.n7mn.dev.survivalsystem.customenchant.enchant.*;
import xyz.n7mn.dev.survivalsystem.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@UtilityClass
public class CustomEnchantUtils {
    public CustomEnchantAbstract RESISTANCE = new ResistanceEnchant();
    public CustomEnchantAbstract TEST = new TestEnchant();
    public CustomEnchantAbstract LIFE_STEAL = new LifeStealEnchant();
    public CustomEnchantAbstract NIGHT_VISION = new NightVisionEnchant();
    public CustomEnchantAbstract KINETIC_RESISTANCE = new KineticEnergyResistanceEnchant();

    public CustomEnchantAbstract[] AllEnchants = new CustomEnchantAbstract[]{
            RESISTANCE,
            TEST,
            LIFE_STEAL,
            NIGHT_VISION,
            KINETIC_RESISTANCE,
    };

    public void replaceLore(ItemStack target, Component match, Component replace) {
        if (target.hasLore()) {
            List<Component> format = new ArrayList<>();

            String mt = GsonComponentSerializer.gson().serialize(match);

            target.lore().forEach(lore -> {
                String st = GsonComponentSerializer.gson().serialize(lore);

                if (mt.equals(st)) format.add(replace);
                else format.add(lore);
            });

            //target.getItemMeta().getLore().forEach(lore -> {
            //    Component component = Component.text(lore);
            //
            //    if (String.valueOf(match).equals(String.valueOf(component))) {
            //        formatter.add(replace);
            //    } else {
            //        formatter.add(Component.text(lore));
            //    }
            //});

            target.lore(format);
        }
    }

    public void replaceLore(ItemStack target, ItemStack itemStack, Enchantment enchantment, Component replace) {
        if (target.hasLore()) {
            List<Component> format = new ArrayList<>();

            String enchantLore = GsonComponentSerializer.gson().serialize(enchantment.displayName(getIntValue(itemStack, enchantment)));

            target.lore().forEach(lore -> {
                String st = GsonComponentSerializer.gson().serialize(lore);

                if (enchantLore.equals(st)) format.add(replace);
                else format.add(lore);

            });

            //target.getItemMeta().getLore().forEach(lore -> {
            //    Component component = Component.text(lore);
            //
            //    if (String.valueOf(enchantment.displayName(getIntValue(itemStack, enchantment))).equals(String.valueOf(component))) {
            //        format.add(replace);
            //    } else {
            //        format.add(Component.text(lore));
            //    }
            //});

            target.lore(format);
        }
    }

    public int getIntValue(ItemStack itemStack, Enchantment enchantment) {
        if (enchantment != null) {
            if (itemStack.hasEnchant(enchantment)) return itemStack.getEnchantments().get(enchantment);
        } else {
            for (CustomEnchantAbstract enchant : CustomEnchantUtils.AllEnchants) {
                if (itemStack.hasEnchant(enchant)) return itemStack.getEnchantments().get(enchant);
            }
        }
        return 0;
    }

    public ImmutableMap<CustomEnchantAbstract, Integer> getAllCustomEnchants(ItemStack itemStack) {
        ImmutableMap.Builder<CustomEnchantAbstract, Integer> enchantment = ImmutableMap.builder();

        Map<Enchantment, Integer> enchants = itemStack.getEnchantments();

        enchants.forEach((enchant, integer) -> {
            if (enchant instanceof CustomEnchantAbstract customEnchant) {
                enchantment.put(customEnchant, integer);
            }
        });

        return enchants.size() != 0 ? enchantment.build() : ImmutableMap.of();
    }

    public ItemStack removeLore(ItemStack target, CustomEnchantAbstract enchantment, boolean cursed) {
        target.getEnchants().forEach((enchant, level) -> {
            if (enchant.equals(enchantment) && target.hasLore()) {
                removeLore(target, enchantment, level, cursed);
            }
        });
        return target;
    }

    private void removeLore(ItemStack target, CustomEnchantAbstract enchantment, final int level, final boolean cursed) {
        if (!cursed || !enchantment.isCursed()) {
            target.setLore(target.getLore().stream()
                    .filter(lore -> !lore.equals(enchantment.displayNameToString(level)))
                    .collect(Collectors.toList()));
        } else {
            target.addEnchant(enchantment, level, true);
        }
    }

    public ItemStack removeLore(ItemStack target, ItemStack from, final boolean cursed) {
        from.getEnchants().forEach((enchant, level) -> {
            if (enchant instanceof CustomEnchantAbstract found && target.hasLore()) {
                removeLore(target, found, level, cursed);
            }
        });
        return target;
    }

    public ItemStack removeLore(ItemStack target) {
        return removeLore(target, target, true);
    }

    public boolean hasCustomEnchant(ItemStack itemStack) {
        return Arrays.stream(CustomEnchantUtils.AllEnchants).anyMatch(itemStack::hasEnchant);
    }

    public boolean hasVanillaEnchant(ItemStack itemStack) {
        return itemStack.getEnchantments().keySet().stream().anyMatch(enchantment -> !(enchantment instanceof CustomEnchantAbstract));
    }

    public boolean addCustomEnchant(ItemStack itemStack, CustomEnchantAbstract enchant, int level, boolean ignoreLevelRestriction) {
        return addCustomEnchant(itemStack, enchant, level, ignoreLevelRestriction, true);
    }

    public boolean addCustomEnchant(ItemStack itemStack, CustomEnchantAbstract enchant, int level, boolean ignoreLevelRestriction, boolean force) {
        if (force || enchant.canEnchantItem(itemStack)) {
            ItemStackUtil.addLore(itemStack, enchant.displayName(level));

            itemStack.addEnchant(enchant, level, ignoreLevelRestriction);

            return true;
        }
        return false;
    }

    /**
     * @deprecated - don't need this
     */
    @Deprecated
    Collector<String, ?, List<Component>> toComponent = Collector.of(
            ArrayList::new,
            (components, s) -> components.add(Component.text(s)),
            (l1, l2) -> {
                l1.addAll(l2);
                return l1;
            },
            Collector.Characteristics.IDENTITY_FINISH);
}