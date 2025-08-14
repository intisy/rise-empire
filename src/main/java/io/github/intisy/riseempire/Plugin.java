package io.github.intisy.riseempire;

import io.github.intisy.riseempire.command.CommandLogFilter;
import io.github.intisy.riseempire.command.RiseEmpireCommand;
import io.github.intisy.riseempire.manager.BypassManager;
import io.github.intisy.riseempire.listener.CommandBypassListener;
import io.github.intisy.riseempire.listener.InventoryListener;
import io.github.intisy.riseempire.listener.TotemListener;
import io.github.intisy.riseempire.listener.WhitelistBypassListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    private BypassManager bypassManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        setupManagers();
        setupFilters();
        registerListeners();
        registerCommands();

        getLogger().info("RiseEmpire enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RiseEmpire disabled!");
    }

    private void setupManagers() {
        this.bypassManager = new BypassManager(this);
        new io.github.intisy.riseempire.manager.ItemManager(this);
    }

    private void setupFilters() {
        Logger coreLogger = (Logger) LogManager.getRootLogger();
        coreLogger.addFilter(new CommandLogFilter(bypassManager));
    }

    private void registerListeners() {
        boolean enableBypass = getConfig().getBoolean("enable-bypass", true);

        getServer().getPluginManager().registerEvents(new WhitelistBypassListener(enableBypass, bypassManager), this);
        getServer().getPluginManager().registerEvents(new CommandBypassListener(enableBypass, bypassManager), this);
        getServer().getPluginManager().registerEvents(new TotemListener(this, enableBypass, bypassManager), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
    }

    private void registerCommands() {
        RiseEmpireCommand riseEmpireCommand = new RiseEmpireCommand(this, bypassManager);
        this.getCommand("riseempire").setExecutor(riseEmpireCommand);
        this.getCommand("riseempire").setTabCompleter(riseEmpireCommand);
    }
}
