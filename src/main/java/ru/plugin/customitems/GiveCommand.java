package ru.plugin.customitems;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiveCommand implements CommandExecutor, TabCompleter {

    private final CustomItemsPlugin plugin;
    private static final List<String> TYPES = List.of("ash", "flash", "immunity");

    public GiveCommand(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /customitem <ash|flash|immunity> [ник]");
            return true;
        }

        String type = args[0].toLowerCase();
        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок не найден: " + args[1]);
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(ChatColor.RED + "Укажи ник игрока: /customitem <тип> <ник>");
            return true;
        }

        ItemStack item = switch (type) {
            case "ash" -> plugin.getItemFactory().createAshOfTheSlain();
            case "flash" -> plugin.getItemFactory().createFlashOfLight();
            case "immunity" -> plugin.getItemFactory().createImmunityItem();
            default -> null;
        };

        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Неизвестный тип предмета. Доступно: ash, flash, immunity");
            return true;
        }

        target.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "Выдан предмет '" + type + "' игроку " + target.getName());
        if (!sender.equals(target)) {
            target.sendMessage(ChatColor.GREEN + "Вы получили кастомный предмет!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String t : TYPES) {
                if (t.startsWith(args[0].toLowerCase())) {
                    completions.add(t);
                }
            }
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(partial)) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}
