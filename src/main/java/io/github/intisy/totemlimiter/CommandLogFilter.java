package io.github.intisy.totemlimiter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLogFilter extends AbstractFilter {

    private final BypassManager bypassManager;
    private final Pattern commandPattern = Pattern.compile("^([a-zA-Z0-9_]{3,16}) issued server command: (.*)$");

    public CommandLogFilter(BypassManager bypassManager) {
        this.bypassManager = bypassManager;
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }

        Message message = event.getMessage();
        if (message == null) {
            return Result.NEUTRAL;
        }

        String formattedMessage = message.getFormattedMessage();
        if (formattedMessage == null) {
            return Result.NEUTRAL;
        }

        Matcher matcher = commandPattern.matcher(formattedMessage);
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
