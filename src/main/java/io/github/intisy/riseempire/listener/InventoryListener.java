package io.github.intisy.riseempire.listener;

import io.github.intisy.riseempire.manager.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scanInventory(event.getPlayer().getInventory());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        scanInventory(event.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        scanInventory(event.getInventory());
        if (event.getWhoClicked() instanceof Player) {
            scanInventory(event.getWhoClicked().getInventory());
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (ItemManager.hasExpiry(event.getItem().getItemStack()) && ItemManager.getExpiry(event.getItem().getItemStack()) < System.currentTimeMillis()) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    private void scanInventory(Inventory inventory) {
        if (inventory == null) return;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && ItemManager.hasExpiry(item) && ItemManager.getExpiry(item) < System.currentTimeMillis()) {
                inventory.setItem(i, null);
            }
        }
    }
}
