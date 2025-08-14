package io.github.intisy.riseempire.command;

import io.github.intisy.riseempire.Plugin;
import io.github.intisy.riseempire.manager.BypassManager;
import io.github.intisy.riseempire.manager.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RiseEmpireCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final BypassManager bypassManager;

    public RiseEmpireCommand(Plugin plugin, BypassManager bypassManager) {
        this.plugin = plugin;
        this.bypassManager = bypassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean hasPermission = sender.isOp();
        if (sender instanceof Player && bypassManager.isBypassed(((Player) sender).getUniqueId())) {
            hasPermission = true;
        }

        if (!hasPermission) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "bypass":
                handleBypass(sender, args);
                break;
            case "totemlimit":
                handleTotemLimit(sender, args);
                break;
            case "reload":
                 plugin.reloadConfig();
                 sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                 break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /riseempire help for a list of commands.");
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- RiseEmpire Help ---");
        sender.sendMessage(ChatColor.AQUA + "/riseempire help" + ChatColor.WHITE + " - Shows this help menu.");
        sender.sendMessage(ChatColor.AQUA + "/riseempire give <player> <item> <duration>" + ChatColor.WHITE + " - Gives a player a limited-time item.");
        sender.sendMessage(ChatColor.AQUA + "/riseempire bypass <add|remove|list> [player]" + ChatColor.WHITE + " - Manages the bypass list.");
        sender.sendMessage(ChatColor.AQUA + "/riseempire totemlimit <limit>" + ChatColor.WHITE + " - Sets the totem pop limit.");
        sender.sendMessage(ChatColor.AQUA + "/riseempire reload" + ChatColor.WHITE + " - Reloads the configuration file.");
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /riseempire give <player> <item> <duration>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        Material material;
        try {
            material = Material.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid item name.");
            return;
        }

        long duration;
        try {
            String durationStr = args[3].toLowerCase();
            long value = Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
            char unit = durationStr.charAt(durationStr.length() - 1);
            switch (unit) {
                case 's': duration = value * 1000; break;
                case 'm': duration = value * 60 * 1000; break;
                case 'h': duration = value * 60 * 60 * 1000; break;
                case 'd': duration = value * 24 * 60 * 60 * 1000; break;
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid duration unit. Use s, m, h, or d.");
                    return;
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Invalid duration format.");
            return;
        }

        ItemStack item = new ItemStack(material, 1);
        long expiryTimestamp = System.currentTimeMillis() + duration;
        ItemStack limitedItem = ItemManager.setExpiry(item, expiryTimestamp);
        ItemManager.updateLore(limitedItem);

        target.getInventory().addItem(limitedItem);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a limited-time " + material.name() + ".");
    }

    private void handleBypass(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /riseempire bypass <add|remove|list> [player]");
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "list":
                List<String> bypassedUuids = bypassManager.getBypassedUuids();
                sender.sendMessage(ChatColor.GOLD + "Bypassed Players:");
                if (bypassedUuids.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "- None");
                } else {
                    for (String uuidStr : bypassedUuids) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                        sender.sendMessage(ChatColor.GRAY + "- " + (p.getName() != null ? p.getName() : uuidStr));
                    }
                }
                break;
            case "add":
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /riseempire bypass <add|remove> <player>");
                    return;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                if (target == null || !target.hasPlayedBefore()) {
                    sender.sendMessage(ChatColor.RED + "Player has not played on this server before.");
                    return;
                }
                if (action.equals("add")) {
                    bypassManager.addBypass(target.getUniqueId());
                    sender.sendMessage(ChatColor.GREEN + target.getName() + " has been added to the bypass list.");
                } else {
                    bypassManager.removeBypass(target.getUniqueId());
                    sender.sendMessage(ChatColor.GREEN + target.getName() + " has been removed from the bypass list.");
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid action. Use add, remove, or list.");
                break;
        }
    }

    private void handleTotemLimit(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /riseempire totemlimit <limit>");
            return;
        }
        try {
            int limit = Integer.parseInt(args[1]);
            if (limit < 0) {
                sender.sendMessage(ChatColor.RED + "Limit cannot be negative.");
                return;
            }
            plugin.getConfig().set("totem-limit", limit);
            plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Totem limit set to " + limit + ". A server reload or restart might be required for changes to apply everywhere.");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number format.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean hasPermission = sender.isOp();
        if (sender instanceof Player && bypassManager.isBypassed(((Player) sender).getUniqueId())) {
            hasPermission = true;
        }
        if (!hasPermission) return Collections.emptyList();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("help", "give", "bypass", "totemlimit", "reload"), new ArrayList<>());
        }

        if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "give":
                    if (args.length == 2) {
                        return null; // Let server handle player name completion
                    } else if (args.length == 3) {
                        return StringUtil.copyPartialMatches(args[2], Arrays.stream(Material.values()).map(Enum::name).map(String::toLowerCase).collect(ArrayList::new, ArrayList::add, ArrayList::addAll), new ArrayList<>());
                    }
                    break;
                case "bypass":
                    if (args.length == 2) {
                        return StringUtil.copyPartialMatches(args[1], Arrays.asList("add", "remove", "list"), new ArrayList<>());
                    } else if (args.length == 3 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                        return null; // Player name completion
                    }
                    break;
            }
        }
        return completions;
    }
}
