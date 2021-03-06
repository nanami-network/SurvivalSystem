package xyz.n7mn.dev.survivalsystem.gui.customcraft.craft;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.n7mn.dev.survivalsystem.SurvivalInstance;
import xyz.n7mn.dev.survivalsystem.advancement.data.CustomCraftCreateAdvancement;
import xyz.n7mn.dev.survivalsystem.customcraft.base.CustomCraftAbstract;
import xyz.n7mn.dev.survivalsystem.customcraft.base.CustomCraftData;
import xyz.n7mn.dev.survivalsystem.customcraft.base.data.ItemData;
import xyz.n7mn.dev.survivalsystem.customcraft.base.data.ItemDataUtils;
import xyz.n7mn.dev.survivalsystem.gui.base.GUIItem;
import xyz.n7mn.dev.survivalsystem.gui.base.GUIListener;
import xyz.n7mn.dev.survivalsystem.gui.customcraft.recipe.RecipeGUI;
import xyz.n7mn.dev.survivalsystem.util.ItemStackUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CraftGUI implements Listener, GUIListener {

    public Inventory createGUI() {
        CraftHolder craftHolder = new CraftHolder();

        Inventory inventory = Bukkit.createInventory(craftHolder, 54, Component.text("カスタム作業台"));

        createCraftGUI(craftHolder, inventory);

        craftHolder.setInventory(inventory);

        return inventory;
    }

    public void createCraftGUI(CraftHolder craftHolder, Inventory inventory) {
        for (int i = 0; i < 54; i++) {
            if (!deny(i)) {
                inventory.setItem(i, ItemStackUtil.createItem(Material.GRAY_STAINED_GLASS_PANE, String.valueOf(ChatColor.GRAY)));
            } else {
                craftHolder.addListener(i, player -> Bukkit.getScheduler().runTask(SurvivalInstance.INSTANCE.getPlugin(), () -> checkUpdates(craftHolder)));
            }
        }

        inventory.setItem(53, ItemStackUtil.createItem(Material.KNOWLEDGE_BOOK, ChatColor.YELLOW + "レシピ本を見る"));
        craftHolder.addListener(53, player -> new RecipeGUI().createRecipePreview(player, 1));
        inventory.setItem(24, ItemDataUtils.INVALID_ITEM.getItemStack());
    }

    public void checkUpdates(CraftHolder craftHolder) {
        for (CustomCraftAbstract data : SurvivalInstance.INSTANCE.getCustomCraft().getCraftAbstractHashMap().values()) {
            if (craftHolder.translateCustomCraftData().equals(data.create(), true)) {
                craftHolder.getInventory().setItem(24, data.getItem(craftHolder.translateCustomCraftData()).getItemStack());
                return;
            }
        }
        craftHolder.getInventory().setItem(24, ItemDataUtils.INVALID_ITEM.getItemStack());
    }

    public boolean craftItem(CraftHolder craftHolder, ItemStack cursor, HumanEntity player, InventoryAction inventoryAction) {
        if (inventoryAction != InventoryAction.HOTBAR_MOVE_AND_READD) {
            for (CustomCraftAbstract data : SurvivalInstance.INSTANCE.getCustomCraft().getCraftAbstractHashMap().values()) {
                CustomCraftData customCraftData = data.create();

                //レシピが一致しているか確認する
                if (craftHolder.translateCustomCraftData().equals(customCraftData, true)) {

                    if (inventoryAction == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                        ItemData item = data.getItem(customCraftData);

                        while (player.getInventory().firstEmpty() != -1 && getItemCheckData(craftHolder) != null) {
                            //TODO: もっといい条件式にしよう
                            player.getInventory().addItem(item.getItemStack());
                        }

                        checkUpdates(craftHolder);
                    } else if (cursor != null && cursor.getType() == Material.AIR) {
                        return push(craftHolder, data);
                    } else if (cursor != null
                            && new ItemData(cursor).equals(data.getItem(customCraftData), true, true, true)
                            && cursor.getType().getMaxStackSize() > cursor.getAmount()) {
                        cursor.setAmount(cursor.getAmount() + 1);

                        push(craftHolder, data);
                        checkUpdates(craftHolder);
                    }

                    return true;
                }
            }
        } else {
            player.sendMessage(Component.text(ChatColor.RED + "[!] それはできませんよ"));
        }
        return true;
    }

    public void grant(HumanEntity player) {
        if (player instanceof Player it) {
            it.getAdvancementProgress(Objects.requireNonNull(Bukkit.getAdvancement(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), CustomCraftCreateAdvancement.ID)))).awardCriteria("grant");
        }
    }

    public ItemData getItemCheckData(CraftHolder craftHolder) {
        for (CustomCraftAbstract data : SurvivalInstance.INSTANCE.getCustomCraft().getCraftAbstractHashMap().values()) {
            if (craftHolder.translateCustomCraftData().equals(data.create(), true)) {
                return getItem(craftHolder, data);
            }
        }
        return null;
    }

    public ItemData getItem(CraftHolder craftHolder, CustomCraftAbstract data) {
        List<Integer> deny = denyList();

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = craftHolder.getInventory().getItem(deny.get(i));

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                ItemData itemData = data.getUsesItem().getItemData().get(i);

                if (itemStack.getType() != itemData.getItemStack().getType()) itemStack.setType(itemData.getItemStack().getType());
                //typeが変わることは特殊です、それを変更することは自己責任
                else itemStack.setAmount(itemStack.getAmount() - data.getUsesItem().getItemData().get(i).getItemStack().getAmount());
            }
        }

        return data.getItem(craftHolder.translateCustomCraftData());
    }


    public boolean push(CraftHolder craftHolder, CustomCraftAbstract data) {
        return getItem(craftHolder, data) == null;
    }

    public void onInventoryClickEvent(final InventoryClickEvent e) {
        final boolean isGUI = e.getClickedInventory() == e.getView().getTopInventory();

        if ((isGUI || e.isShiftClick())
                && e.getView().getTopInventory().getHolder() instanceof CraftHolder craftHolder) {

            GUIItem guiItem = craftHolder.getHashMap().get(e.getRawSlot());

            if (isGUI) {
                if (guiItem != null) {
                    guiItem.execute((Player) e.getWhoClicked());
                } else if (e.getRawSlot() == 24) {
                    if (craftItem(craftHolder, e.getCursor(), e.getWhoClicked(), e.getAction())) {
                        e.setCancelled(true);
                    } else {
                        Bukkit.getScheduler().runTask(SurvivalInstance.INSTANCE.getPlugin(), () -> checkUpdates(craftHolder));
                    }
                }

                if (!deny(e.getRawSlot()) && e.getRawSlot() != 24) {
                    e.setCancelled(true);
                }

            } else if (e.isShiftClick()) {
                Bukkit.getScheduler().runTask(SurvivalInstance.INSTANCE.getPlugin(), () -> checkUpdates(craftHolder));
            }
        }
    }

    public void onInventoryDragEvent(final InventoryDragEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTopInventory() == e.getInventory()
                && e.getView().getTopInventory().getHolder() instanceof CraftHolder craftHolder) {
            for (int slot : e.getRawSlots()) {

                GUIItem guiItem = craftHolder.getHashMap().get(slot);
                if (guiItem != null) {
                    guiItem.execute((Player) e.getWhoClicked());

                    break;
                }

                if (slot == 24) {
                    if (craftItem(craftHolder, e.getCursor(), e.getWhoClicked(), null)) {
                        e.setCancelled(true);
                    } else {
                        checkUpdates(craftHolder);
                    }
                }
            }
        }
    }

    public void onInventoryCloseEvent(final InventoryCloseEvent e) {
        if (e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof CraftHolder) {
            for (int refund : denyList()) {
                ItemStack itemStack = e.getInventory().getItem(refund);

                if (itemStack != null && itemStack.getType() != Material.AIR) ItemStackUtil.addItem(e.getPlayer(), itemStack);
            }
        }
    }

    public boolean deny(final int rawSlot) {
        return (rawSlot >= 10 && rawSlot <= 12) ||
                (rawSlot >= 19 && rawSlot <= 21) ||
                (rawSlot >= 28 && rawSlot <= 30);
    }

    /**
     * @return - クラフトスロットを返します
     */
    public List<Integer> denyList() {
        return Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30);
    }
}