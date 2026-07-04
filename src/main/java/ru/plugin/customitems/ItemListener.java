package ru.plugin.customitems;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemListener implements Listener {

    private final CustomItemsPlugin plugin;

    private final Map<UUID, Long> ashCooldowns = new HashMap<>();
    private final Map<UUID, Long> flashCooldowns = new HashMap<>();

    private static final double RADIUS = 15.0;

    private static final long ASH_COOLDOWN_MS = 60_000L;
    private static final long FLASH_COOLDOWN_MS = 55_000L;

    public ItemListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction().isRightClick())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta.getPersistentDataContainer().has(plugin.getAshKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            handleAshActivation(player, item);
        } else if (meta.getPersistentDataContainer().has(plugin.getFlashKey(), PersistentDataType.BYTE)) {
            event.setCancelled(true);
            handleFlashActivation(player, item);
        }
    }

    private void handleAshActivation(Player player, ItemStack usedItem) {
        long now = System.currentTimeMillis();
        long lastUse = ashCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = ASH_COOLDOWN_MS - (now - lastUse);

        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Прах убитого перезаряжается ещё " + formatSeconds(remaining) + " сек.");
            return;
        }

        ashCooldowns.put(player.getUniqueId(), now);

        Location center = player.getLocation();
        for (LivingEntity entity : getNearbyPlayers(center, RADIUS, player)) {
            Player target = (Player) entity;
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0, false, true, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 5 * 20, 1, false, true, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 3 * 20, 0, false, true, true));
        }

        center.getWorld().spawnParticle(Particle.SOUL, center, 60, RADIUS / 3, 1, RADIUS / 3, 0.02);
        center.getWorld().playSound(center, Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.6f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Вы активировали Прах убитого!");

        consumeOne(usedItem);
    }

    private void handleFlashActivation(Player player, ItemStack usedItem) {
        long now = System.currentTimeMillis();
        long lastUse = flashCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = FLASH_COOLDOWN_MS - (now - lastUse);

        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Вспышка света перезаряжается ещё " + formatSeconds(remaining) + " сек.");
            return;
        }

        flashCooldowns.put(player.getUniqueId(), now);
        Location center = player.getLocation();
        for (LivingEntity entity : getNearbyPlayers(center, RADIUS, player)) {
            Player target = (Player) entity;
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 30 * 20, 0, false, true, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 3 * 20, 0, false, true, true));
        }

        center.getWorld().spawnParticle(Particle.FLASH, center, 1);
        center.getWorld().spawnParticle(Particle.END_ROD, center, 80, RADIUS / 3, 1, RADIUS / 3, 0.05);
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.8f);
        player.sendMessage(ChatColor.YELLOW + "Вы активировали Вспышку света!");

        consumeOne(usedItem);
    }

    private void consumeOne(ItemStack item) {
        if (item.getAmount() <= 1) {
            item.setAmount(0);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    private java.util.List<LivingEntity> getNearbyPlayers(Location center, double radius, Player excluded) {
        java.util.List<LivingEntity> result = new java.util.ArrayList<>();
        for (org.bukkit.entity.Entity nearby : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (nearby instanceof Player player && !player.getUniqueId().equals(excluded.getUniqueId())) {
                result.add(player);
            }
        }
        return result;
    }

    private String formatSeconds(long millis) {
        return String.valueOf((millis / 1000) + 1);
    }

    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getNewEffect() == null) {
            return;
        }
        if (event.getNewEffect().getType() != PotionEffectType.POISON) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack immunityItem = findImmunityItem(player);
        if (immunityItem == null) {
            return;
        }

        event.setCancelled(true);

        ItemMeta meta = immunityItem.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int newDamage = damageable.getDamage() + 5;
            int maxDurability = immunityItem.getType().getMaxDurability();

            if (newDamage >= maxDurability) {
                immunityItem.setAmount(immunityItem.getAmount() - 1);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                player.sendMessage(ChatColor.RED + "Ваш предмет Иммунитет сломался, поглотив отравление!");
            } else {
                damageable.setDamage(newDamage);
                immunityItem.setItemMeta((ItemMeta) damageable);
                player.sendMessage(ChatColor.GREEN + "Иммунитет поглотил отравление! Прочность предмета снижена.");
            }
        }
    }

    private ItemStack findImmunityItem(Player player) {
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || !stack.hasItemMeta()) {
                continue;
            }
            if (stack.getItemMeta().getPersistentDataContainer()
                    .has(plugin.getImmunityKey(), PersistentDataType.BYTE)) {
                return stack;
            }
        }
        return null;
    }
}
