package io.github.intisy.riseempire;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {

    private BypassManager bypassManager;
    private CommandLogFilter commandLogFilter;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        int totemLimit = getConfig().getInt("totem-limit", 2);
        boolean enableBypass = getConfig().getBoolean("enable-bypass", false);

        bypassManager = new BypassManager(this);
        commandLogFilter = new CommandLogFilter(bypassManager);

        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addFilter(commandLogFilter);

        getServer().getPluginManager().registerEvents(new TotemListener(this, enableBypass, totemLimit, bypassManager), this);
        getServer().getPluginManager().registerEvents(new WhitelistBypassListener(enableBypass, bypassManager), this);
        getServer().getPluginManager().registerEvents(new CommandBypassListener(enableBypass, bypassManager), this);
        getLogger().info("RiseEmpire enabled! Totem limit set to: " + totemLimit);
    }

    @Override
    public void onDisable() {
        if (commandLogFilter != null) {
            commandLogFilter.stop();
        }
        getLogger().info("RiseEmpire disabled!");
    }

    public BypassManager getBypassManager() {
        return bypassManager;
    }
}
