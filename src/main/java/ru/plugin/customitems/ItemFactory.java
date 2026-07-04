package ru.plugin.customitems;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Отвечает за создание готовых ItemStack'ов трёх кастомных предметов
 * с уникальными NBT-метками, по которым их потом узнают слушатели событий.
 */
public class ItemFactory {

    private final CustomItemsPlugin plugin;

    public ItemFactory(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    /** 1. Прах убитого */
    public ItemStack createAshOfTheSlain() {
        ItemStack item = new ItemStack(Material.WITHER_ROSE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.of("#5b2a86") + "Прах убитого");
        meta.setLore(List.of(
                ChatColor.GRAY + "Древний тёмный артефакт.",
                ChatColor.GRAY + "ПКМ — высвобождает проклятие",
                ChatColor.GRAY + "в радиусе " + ChatColor.WHITE + "15 блоков" + ChatColor.GRAY + ":",
                ChatColor.DARK_PURPLE + "• Слепота 5 сек",
                ChatColor.DARK_PURPLE + "• Голод 5 сек",
                ChatColor.DARK_PURPLE + "• Отравление 3 сек",
                "",
                ChatColor.DARK_GRAY + "Перезарядка: 60 сек"
        ));

        meta.getPersistentDataContainer().set(plugin.getAshKey(), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /** 2. Вспышка света */
    public ItemStack createFlashOfLight() {
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.of("#ffe066") + "Вспышка света");
        meta.setLore(List.of(
                ChatColor.GRAY + "Ослепляющая вспышка энергии.",
                ChatColor.GRAY + "ПКМ — освещает всех",
                ChatColor.GRAY + "в радиусе " + ChatColor.WHITE + "15 блоков" + ChatColor.GRAY + ":",
                ChatColor.YELLOW + "• Свечение 30 сек",
                ChatColor.YELLOW + "• Слабость 3 сек",
                "",
                ChatColor.DARK_GRAY + "Перезарядка: 55 сек"
        ));

        meta.getPersistentDataContainer().set(plugin.getFlashKey(), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /** 3. Иммунитет */
    public ItemStack createImmunityItem() {
        // SHIELD выбран потому, что у него по умолчанию есть механика прочности (durability)
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.of("#2fd15e") + "Иммунитет");
        meta.setLore(List.of(
                ChatColor.GRAY + "Пока предмет лежит в инвентаре,",
                ChatColor.GRAY + "он блокирует эффект " + ChatColor.DARK_GREEN + "Отравления" + ChatColor.GRAY + ".",
                ChatColor.GRAY + "При блокировке теряет прочность.",
                "",
                ChatColor.RED + "Полностью изнашивается и ломается!"
        ));

        meta.getPersistentDataContainer().set(plugin.getImmunityKey(), PersistentDataType.BYTE, (byte) 1);
        // Делаем предмет "инструментом" с прочностью через Damageable
        if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
            damageable.setDamage(0);
        }
        meta.setUnbreakable(false);
        item.setItemMeta(meta);
        return item;
    }
}
