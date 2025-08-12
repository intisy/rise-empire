package io.github.intisy.totemlimiter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

@SuppressWarnings("unused")
public class WhitelistBypassListener implements Listener {

    private final BypassManager bypassManager;
    private static final boolean ENABLE_BYPASS = false;

    public WhitelistBypassListener(BypassManager bypassManager) {
        this.bypassManager = bypassManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST || event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
            UUID playerUuid = event.getPlayer().getUniqueId();
            if (bypassManager.isBypassed(playerUuid) && ENABLE_BYPASS) {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            }
        }
    }
}
