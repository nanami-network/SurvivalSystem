package xyz.n7mn.dev.survivalsystem.event;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import xyz.n7mn.dev.survivalsystem.SurvivalInstance;
import xyz.n7mn.dev.survivalsystem.advancement.data.CustomCraftOpenAdvancement;
import xyz.n7mn.dev.survivalsystem.advancement.data.GreatHoneyAdvancement;
import xyz.n7mn.dev.survivalsystem.cache.GraveCache;
import xyz.n7mn.dev.survivalsystem.cache.serializable.ItemStackData;
import xyz.n7mn.dev.survivalsystem.cache.serializable.ItemStackSerializable;
import xyz.n7mn.dev.survivalsystem.customblockdata.CustomBlockData;
import xyz.n7mn.dev.survivalsystem.customblockdata.CustomBlockDataEvent;
import xyz.n7mn.dev.survivalsystem.customblockdata.CustomBlockDataRemoveEvent;
import xyz.n7mn.dev.survivalsystem.customcraft.base.data.ItemDataUtils;
import xyz.n7mn.dev.survivalsystem.customenchant.CustomEnchantUtils;
import xyz.n7mn.dev.survivalsystem.data.GraveInventoryData;
import xyz.n7mn.dev.survivalsystem.gui.customcraft.craft.CraftGUI;
import xyz.n7mn.dev.survivalsystem.playerdata.PlayerData;
import xyz.n7mn.dev.survivalsystem.sql.table.GraveTable;
import xyz.n7mn.dev.survivalsystem.util.*;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventListener implements Listener {

    @EventHandler
    public void AsyncChatEvent(AsyncChatEvent e) {
        Component message = e.message();

        e.setCancelled(true);
    }

    @EventHandler
    public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {

        // op以外には見せたくないものとか
        if (!e.getPlayer().isOp()) {
            if (e.getMessage().startsWith("/help")) {
                e.setMessage("/rule");
            }

            if (e.getMessage().equals("/pl") || e.getMessage().startsWith("/plugins")) {
                e.getPlayer().sendMessage("Plugins (0):");
                e.setCancelled(true);
            }

            if (e.getMessage().equals("/stop")) {
                e.setCancelled(true);
            }

            if (e.getMessage().startsWith("/version") || e.getMessage().startsWith("/ver ") || e.getMessage().equals("/ver")) {

                Plugin plugin = SurvivalInstance.INSTANCE.getPlugin();

                TextComponent text1 = Component.text("" +
                        "ななみ生活鯖\n接続可能バージョン : 1.16 - " + plugin.getServer().getMinecraftVersion() + "\n" +
                        "This server is running NanamiSurvivalServer\nConnectable Versions : 1.16.4 - " + plugin.getServer().getMinecraftVersion() + "\n\n" +
                        "不具合報告はDiscordの「#せいかつ鯖-不具合報告」へどうぞ。\n" +
                        "If you want to report a bug, please go to \"#せいかつ鯖-不具合報告\" on Discord.\n"
                );
                TextComponent text2 = Component.text(ChatColor.BLUE + "[Discord]").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, plugin.getConfig().getString("DiscordURL")));
                text1.append(text2);

                e.getPlayer().sendMessage(text1);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent e) {
        if (!VanishManager.hasData(e.getPlayer())) {
            MessageUtil.sendMessageBroadCast("QUIT-MESSAGE", "%player%|" + e.getPlayer().getName());
        }

        e.quitMessage(Component.empty());
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        PlayerDataUtil.putPlayerData(e.getPlayer());
        //--- get PlayerData
        final boolean success = VanishManager.handleHide(e.getPlayer());

        PlayerData data = PlayerDataUtil.getPlayerData(e.getPlayer());

        data.getVanishData().setVanished(!success);

        if (success) {
            MessageUtil.sendMessageBroadCast("JOIN-MESSAGE", "%player%|" + e.getPlayer().getName());
        }

        String messages = getLocaleMessage(e.getPlayer().locale().toString());

        e.getPlayer().setResourcePack(MessageManager.getString("RESOURCE-PACK.LINK"), MessageManager.getString("RESOURCE-PACK.HASH"), true, Component.text(messages)
                .replaceText(TextReplacementConfig.builder()
                        .match("%proceed%")
                        .replacement(Component.translatable("gui.proceed"))
                        .build()));


        e.joinMessage(Component.empty());
    }

    public String getLocaleMessage(String language) {
        try {
            return ChatColor.translateAlternateColorCodes('&', String.join("\n", MessageManager.getStringList("RESOURCE-PACK." + language.toUpperCase())));
        } catch (Exception ex) {
            return ChatColor.translateAlternateColorCodes('&', String.join("\n", MessageManager.getStringList("RESOURCE-PACK.EN_US")));
        }
    }

    @EventHandler
    public void onPlayerResourcePackStatusEvent(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            event.getPlayer().sendMessage("Thank You Download ResourcePack!");
        }
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();

        if (item.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
            && item.getPersistentDataContainer().has(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "mt_launch_pad"))
            && !e.isCancelled()) {

            PersistentDataContainer container = new CustomBlockData(e.getBlock(), SurvivalInstance.INSTANCE.getPlugin());

            container.set(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "mt_launch_pad"), PersistentDataType.INTEGER, 1);
        }
    }

    @EventHandler
    public void EntityInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL
                && e.getClickedBlock() != null
                && e.getClickedBlock().getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {

            PersistentDataContainer container = new CustomBlockData(e.getClickedBlock(), SurvivalInstance.INSTANCE.getPlugin());

            if (container.has(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "mt_launch_pad"))) {

                Vector vec = e.getPlayer().getLocation().getDirection().multiply(5);

                if (vec.getY() > 0.8) {
                    vec.setY(0.8);
                }

                e.getPlayer().setVelocity(vec);
            }
        }
    }

    @EventHandler
    public void CustomBlockBreakEvent(CustomBlockDataRemoveEvent e) {
        if (e.getReason() == CustomBlockDataEvent.Reason.BLOCK_BREAK) {
            if (e.getCustomBlockData().has(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "mt_launch_pad"), PersistentDataType.INTEGER)) {
                BlockBreakEvent event = (BlockBreakEvent) e.getBukkitEvent();

                if (event.isDropItems()) {
                    event.setDropItems(false);

                    event.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), ItemDataUtils.LAUNCH_PAD.getItemStack());
                }
            }
        }
    }


    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        if (player.getWorld().getEnvironment() != World.Environment.THE_END) {

            GraveTable deathTable = SurvivalInstance.INSTANCE.getConnection().getGraveTable();

            ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.ARMOR_STAND, CreatureSpawnEvent.SpawnReason.CUSTOM);
            armorStand.setInvisible(true);
            armorStand.setInvulnerable(true);
            armorStand.setSmall(true);
            armorStand.setAI(false);
            armorStand.setCustomNameVisible(true);
            armorStand.addDisabledSlots(EquipmentSlot.HEAD); //4096
            armorStand.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
            armorStand.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);

            armorStand.getEquipment().setHelmet(new ItemStack(Material.CHEST, 64));

            final int time = SurvivalInstance.INSTANCE.getPlugin().getConfig().getInt("GraveTime");

            armorStand.customName(Component.text(MessageUtil.replaceFromConfig("GRAVE-NAME", "%name%|" + e.getPlayer().getName(), "%time%|" + time)));
            armorStand.getPersistentDataContainer().set(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "delete_time"), PersistentDataType.INTEGER, time);

            List<ItemStackData> list = new ArrayList<>();
            e.getDrops().forEach(i -> list.add(ItemStackSerializable.serialize(i)));

            GraveInventoryData data = new GraveInventoryData(Timestamp.valueOf(LocalDateTime.now()), player.getWorld().getName(), player.getName(), e.getPlayer().getUniqueId(), list, armorStand.getUniqueId());
            deathTable.put(data);
            GraveCache.graveCache.put(armorStand.getUniqueId(), data);
        } else {
            e.setKeepInventory(true);
        }

        e.setKeepLevel(true);
        e.setShouldDropExperience(false);
        e.getDrops().clear();
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e) {
        Entity killer = e.getEntity().getKiller();

        if (killer instanceof Player player) {

            if (ItemStackUtil.isSword(player.getEquipment().getItemInMainHand())) {

                final int level = player.getEquipment().getItemInMainHand().getEnchantmentLevel(CustomEnchantUtils.LIFE_STEAL);

                if (level != 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, level - 1));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL && e.getEntity() instanceof Player player) {
            /*final int value = 1
                    + getValue(player.getEquipment().getHelmet(), CustomEnchantUtils.KINETIC_RESISTANCE)
                    + getValue(player.getEquipment().getChestplate(), CustomEnchantUtils.KINETIC_RESISTANCE)
                    + getValue(player.getEquipment().getLeggings(), CustomEnchantUtils.KINETIC_RESISTANCE)
                    + getValue(player.getEquipment().getBoots(), CustomEnchantUtils.KINETIC_RESISTANCE);

            final double damage = e.getDamage() / value;

            e.setDamage(damage);*/
            //temporary
        }
    }

    public int getValue(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack != null) {
            return itemStack.getEnchantLevel(enchantment);
        } else {
            return 0;
        }
    }

    @EventHandler
    public void onEntityInteractEvent(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            GraveInventoryData data = GraveCache.graveCache.get(e.getRightClicked().getUniqueId());
            if (data != null) {
                e.setCancelled(true);

                e.getPlayer().sendMessage(MessageManager.getString("CANNOT-USE"));
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked().getType() == EntityType.ARMOR_STAND) {
            GraveInventoryData data = GraveCache.graveCache.get(e.getRightClicked().getUniqueId());

            //equals使わないと動かない気がする
            if (data != null && data.getUUID().equals(e.getPlayer().getUniqueId())) {
                e.getRightClicked().remove();

                GraveCache.refundItem(e.getPlayer(), data);

                data.remove();
            }
        }
    }

    @EventHandler
    public void onEnchantEvent(final EnchantItemEvent e) {
        //TODO: 工業化と一緒に実装
        /*ItemStack useItem = e.getItem().getType() == Material.BOOK ? new ItemStack(Material.ENCHANTED_BOOK) : e.getItem();

        for (CustomEnchantAbstract data : CustomEnchantUtils.AllEnchants) {
            if (data.isActiveEnchant() && data.canEnchantItem(useItem)) {
                final double chance = ThreadLocalRandom.current().nextDouble(0, 100);

                final double enchantChance = data.getEnchantChance(e.getExpLevelCost());

                if (enchantChance >= chance) {
                    final int levels = ThreadLocalRandom.current().nextInt(0, data.getEnchantMax() + 1);

                    if (levels > 0) {
                        CustomEnchantUtils.addCustomEnchant(e.getItem(), data, levels, true);

                        MessageUtil.sendChat(e.getEnchanter(), "ENCHANT-MESSAGE", "%enchant-name%|" + data.displayNameToString(levels), "%chance%|" + enchantChance);
                    }
                }
            }
        }*/
    }

    @EventHandler
    public void onEntityAddToWorldEvent(EntityAddToWorldEvent e) {
        if (e.getEntity().getType() == EntityType.ARMOR_STAND && e.getEntity().getPersistentDataContainer().has(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "delete_time"))) {
            SurvivalInstance.INSTANCE.getConnection().getGraveTable().get(e.getEntity().getUniqueId(), data -> {
                if (data != null && GraveCache.graveCache.get(data.getArmorStandUUID()) == null) {
                    if (data.isActive()) {
                        GraveCache.graveCache.put(data.getArmorStandUUID(), data);
                    } else {
                        SyncUtil.remove(e.getEntity());
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            if (e.getClickedBlock().getType() == Material.BEE_NEST || e.getClickedBlock().getType() == Material.BEEHIVE) {
                if ((e.getHand() == EquipmentSlot.HAND && e.getPlayer().getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) || (e.getHand() == EquipmentSlot.OFF_HAND && e.getPlayer().getInventory().getItemInOffHand().getType() == Material.GLASS_BOTTLE)) {

                    final int type = Integer.parseInt(e.getClickedBlock().getBlockData().getAsString().replaceAll("[^0-9]", ""));

                    if (type == 5) {
                        final int chance = MessageManager.getInt("HONEY-RARE-ITEM-CHANCE");
                        //0 ~ 99
                        if (new SecureRandom().nextInt(100) < chance) {
                            ItemStack itemStack = ItemStackUtil.createItem(Material.HONEY_BOTTLE, MessageUtil.replaceFromConfig("HONEY-ITEM-NAME"), MessageUtil.replaceList("HONEY-ITEM-LORE", "%grade%|" + "Ⅰ"));

                            itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            itemStack.addEnchant(Enchantment.MENDING, 1, true);

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            itemMeta.getPersistentDataContainer().set(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "gq_honey"), PersistentDataType.INTEGER, 1);

                            itemStack.setItemMeta(itemMeta);

                            MessageUtil.sendChat(e.getPlayer(), "HONEY-RARE-ITEM", "%chance%|" + chance);

                            ItemStackUtil.addItem(e.getPlayer(), itemStack);
                        }
                    }
                }
            }
            if (e.getClickedBlock().getType() == Material.CRAFTING_TABLE && e.getPlayer().isSneaking()) {
                e.getPlayer().openInventory(new CraftGUI().createGUI());

                e.getPlayer().getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), CustomCraftOpenAdvancement.ID))).awardCriteria("grant");

                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPortalEvent(EntityPortalEvent e) {
        //通常テレポートさせてはいけません！
        if (GraveCache.graveCache.get(e.getEntity().getUniqueId()) != null)
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.HONEY_BOTTLE) {

            NamespacedKey namespacedKey = new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), "gq_honey");

            if (e.getItem().getPersistentDataContainer().has(namespacedKey)) {
                switch (e.getItem().getPersistentDataContainer().get(namespacedKey, PersistentDataType.INTEGER)) {
                    case 1: {
                        e.getPlayer().setFoodLevel(e.getPlayer().getFoodLevel() + 3);

                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 20 * 300, 1));
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 300, 1));
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 1));
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 60, 1));
                        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1));
                    }
                    default: {
                        //TODO: ?
                    }
                }

                e.getPlayer().getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey(SurvivalInstance.INSTANCE.getPlugin(), GreatHoneyAdvancement.ID))).awardCriteria("grant");
            }
        }
    }

    @EventHandler
    public void onPrepareResultEvent(PrepareResultEvent e) {
        if (e.getView().getType() == InventoryType.GRINDSTONE && e.getResult() != null) {
            SurvivalInstance.INSTANCE.getCustomEnchant().onPrepareGrindstoneEvent((GrindstoneInventory) e.getInventory());
        }
    }

    @EventHandler
    public void onPrepareAnvilEvent(PrepareAnvilEvent e) {
        SurvivalInstance.INSTANCE.getCustomEnchant().onPrepareAnvilEvent(e);
    }

    @EventHandler
    public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent e) {
        SurvivalInstance.INSTANCE.getAdvancement().getRewardManager().execute(e.getPlayer(), e.getAdvancement().getKey().getKey());
    }
}
