package io.github.intisy.riseempire.command;

import io.github.intisy.riseempire.manager.BypassManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLogFilter extends AbstractFilter {

    private final BypassManager bypassManager;
    private final Pattern commandPattern = Pattern.compile("(.+?) issued server command: /(.*)");

    public CommandLogFilter(BypassManager bypassManager) {
        this.bypassManager = bypassManager;
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null || event.getMessage() == null) {
            return Result.NEUTRAL;
        }

        String message = event.getMessage().getFormattedMessage();
        Matcher matcher = commandPattern.matcher(message);

        if (matcher.matches()) {
            String playerName = matcher.group(1);
            Player player = Bukkit.getPlayer(playerName);

            if (player != null && bypassManager.isBypassed(player.getUniqueId())) {
                return Result.DENY;
            }
        }

        return Result.NEUTRAL;
    }
}
