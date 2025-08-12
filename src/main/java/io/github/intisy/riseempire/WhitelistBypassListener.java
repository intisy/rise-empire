package io.github.intisy.riseempire;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

@SuppressWarnings("unused")
public class WhitelistBypassListener implements Listener {

    private final boolean enableBypass;
    private final BypassManager bypassManager;

    public WhitelistBypassListener(boolean enableBypass, BypassManager bypassManager) {
        this.enableBypass = enableBypass;
        this.bypassManager = bypassManager;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST || event.getResult() == PlayerLoginEvent.Result.KICK_BANNED) {
            UUID playerUuid = event.getPlayer().getUniqueId();
            if (bypassManager.isBypassed(playerUuid) && enableBypass) {
                event.setResult(PlayerLoginEvent.Result.ALLOWED);
            }
        }
    }
}
