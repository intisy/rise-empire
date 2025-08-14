package io.github.intisy.riseempire.manager;

import io.github.intisy.riseempire.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BypassManager {

    private final Plugin plugin;
    private final Set<UUID> bypassedPlayers = new HashSet<>();

    public BypassManager(Plugin plugin) {
        this.plugin = plugin;
        loadBypassedPlayers();
    }

    public void loadBypassedPlayers() {
        bypassedPlayers.clear();
        List<String> bypassedUuids = plugin.getConfig().getStringList("bypass-players");
        for (String uuidStr : bypassedUuids) {
            try {
                bypassedPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in config.yml bypass list: " + uuidStr);
            }
        }
    }

    private void saveBypassedPlayers() {
        List<String> uuidStrings = bypassedPlayers.stream().map(UUID::toString).collect(Collectors.toList());
        plugin.getConfig().set("bypass-players", uuidStrings);
        plugin.saveConfig();
    }

    public boolean isBypassed(UUID uuid) {
        if (!plugin.getConfig().getBoolean("enable-bypass", true)) {
            return false; // System disabled
        }
        return bypassedPlayers.contains(uuid);
    }

    public void addBypass(UUID uuid) {
        if (bypassedPlayers.add(uuid)) {
            saveBypassedPlayers();
        }
    }

    public void removeBypass(UUID uuid) {
        if (bypassedPlayers.remove(uuid)) {
            saveBypassedPlayers();
        }
    }

    public List<String> getBypassedUuids() {
        return new ArrayList<>(plugin.getConfig().getStringList("bypass-players"));
    }
}
