package ru.plugin.customitems;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomItemsPlugin extends JavaPlugin {

    private NamespacedKey ashKey;
    private NamespacedKey flashKey;
    private NamespacedKey immunityKey;

    private ItemFactory itemFactory;

    @Override
    public void onEnable() {
        this.ashKey = new NamespacedKey(this, "ash_of_the_slain");
        this.flashKey = new NamespacedKey(this, "flash_of_light");
        this.immunityKey = new NamespacedKey(this, "poison_immunity");

        this.itemFactory = new ItemFactory(this);

        // Регистрируем слушатель событий (использование предметов, защита от яда)
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);

        // Регистрируем команду выдачи предметов
        GiveCommand giveCommand = new GiveCommand(this);
        getCommand("customitem").setExecutor(giveCommand);
        getCommand("customitem").setTabCompleter(giveCommand);

        getLogger().info("CustomItemsPlugin включен! Предметы: Прах убитого, Вспышка света, Иммунитет.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomItemsPlugin выключен.");
    }

    public NamespacedKey getAshKey() {
        return ashKey;
    }

    public NamespacedKey getFlashKey() {
        return flashKey;
    }

    public NamespacedKey getImmunityKey() {
        return immunityKey;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }
}
