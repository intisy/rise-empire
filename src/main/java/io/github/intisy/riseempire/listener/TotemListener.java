package io.github.intisy.riseempire.listener;

import io.github.intisy.riseempire.manager.BypassManager;
import io.github.intisy.riseempire.Plugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class TotemListener implements Listener {

    private static final Material TOTEM_MATERIAL;

    static {
        Material totemMaterial = Material.getMaterial("TOTEM_OF_UNDYING");
        if (totemMaterial == null) {
            totemMaterial = Material.getMaterial("TOTEM");
        }
        TOTEM_MATERIAL = totemMaterial;
    }

    private final Plugin plugin;
    private final boolean enableBypass;
    private final BypassManager bypassManager;

    public TotemListener(Plugin plugin, boolean enableBypass, BypassManager bypassManager) {
        this.plugin = plugin;
        this.enableBypass = enableBypass;
        this.bypassManager = bypassManager;
    }

    private int getTotemCount(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == TOTEM_MATERIAL) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isBypassed(event.getPlayer())) {
            return;
        }
        if (event.getItem().getItemStack().getType() == TOTEM_MATERIAL) {
            int totemLimit = plugin.getConfig().getInt("totem-limit", 2);
            int currentTotems = getTotemCount(event.getPlayer());
            if (currentTotems >= totemLimit) {
                event.setCancelled(true);
                plugin.getLogger().info("Blocked " + event.getPlayer().getName() + " from picking up a totem (limit: " + totemLimit + ")");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (isBypassed(player)) {
            return;
        }

        int totemLimit = plugin.getConfig().getInt("totem-limit", 2);
        ItemStack currentItem = event.getCurrentItem();

        if (event.isShiftClick() && currentItem != null && currentItem.getType() == TOTEM_MATERIAL) {

            if (event.getClickedInventory() != null && !event.getClickedInventory().equals(player.getInventory())) {
                if (getTotemCount(player) >= totemLimit) {
                    event.setCancelled(true);
                    plugin.getLogger().info("Blocked " + player.getName() + " from shift-clicking a totem into inventory (limit: " + totemLimit + ")");
                    return;
                }
            }
        }

        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && cursorItem.getType() == TOTEM_MATERIAL) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
                if (getTotemCount(player) >= totemLimit) {
                    event.setCancelled(true);
                    plugin.getLogger().info("Blocked " + player.getName() + " from moving a totem in inventory (limit: " + totemLimit + ")");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (isBypassed(player)) {
            return;
        }
        ItemStack cursorItem = event.getPlayer().getItemOnCursor();

        if (cursorItem != null && cursorItem.getType() == TOTEM_MATERIAL) {
            int totemLimit = plugin.getConfig().getInt("totem-limit", 2);
            if (getTotemCount(player) >= totemLimit) {
                event.getView().setCursor(null);
                player.getWorld().dropItemNaturally(player.getLocation(), cursorItem);
                plugin.getLogger().info("Dropped a totem from " + player.getName() + "'s cursor because their inventory is full (limit: " + totemLimit + ")");
            }
        }
    }

    private boolean isBypassed(Player player) {
        return enableBypass && bypassManager.isBypassed(player.getUniqueId());
    }
}
