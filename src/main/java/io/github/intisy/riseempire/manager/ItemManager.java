package io.github.intisy.riseempire.manager;

import io.github.intisy.riseempire.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    private static final String EXPIRY_KEY = "rise-empire-expiry";

    private interface NbtApi {
        ItemStack setExpiry(ItemStack item, long expiryTimestamp) throws Exception;
        long getExpiry(ItemStack item) throws Exception;
        boolean hasExpiry(ItemStack item) throws Exception;
    }

    private static NbtApi nbtApi;

    static {
        try {
            Bukkit.getLogger().info("[RiseEmpire Debug] Initializing NBT API...");
            try {
                Class.forName("net.minecraft.core.component.DataComponents");
                nbtApi = new DataComponentsNbtApi();
                Bukkit.getLogger().info("[RiseEmpire] Modern DataComponents API detected (1.20.5+). Setup successful.");
            } catch (ClassNotFoundException e) {
                nbtApi = new LegacyNbtApi();
                Bukkit.getLogger().info("[RiseEmpire] Legacy NBT API detected (pre-1.20.5). Setup successful.");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("[RiseEmpire] CRITICAL: NBT API setup failed. Limited-time items will not work.");
            e.printStackTrace();
            nbtApi = new NoOpNbtApi(); // Failsafe
        }
    }

    // --- DataComponents API for 1.20.5+ ---
    private static class DataComponentsNbtApi implements NbtApi {
        private final Method asNMSCopyMethod, asBukkitCopyMethod, getComponentsMethod, componentsGetMethod, customDataOfMethod, customDataGetTagMethod, setComponentMethod;
        private final Object customDataComponent;
        private final Constructor<?> nbtTagCompoundConstructor;
        private final Class<?> nbtTagCompoundClass;

        DataComponentsNbtApi() throws Exception {
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.world.item.ItemStack");
            this.asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            this.asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);

            Class<?> dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
            Class<?> dataComponentMapClass = Class.forName("net.minecraft.core.component.DataComponentMap");
            Class<?> dataComponentTypeClass = Class.forName("net.minecraft.core.component.DataComponentType");
            Class<?> customDataClass = Class.forName("net.minecraft.world.item.component.CustomData");
            this.nbtTagCompoundClass = Class.forName("net.minecraft.nbt.CompoundTag");
            this.nbtTagCompoundConstructor = nbtTagCompoundClass.getDeclaredConstructor();

            this.getComponentsMethod = nmsItemStackClass.getMethod("getComponents");
            this.customDataComponent = dataComponentsClass.getField("CUSTOM_DATA").get(null);
            this.componentsGetMethod = dataComponentMapClass.getMethod("get", dataComponentTypeClass);
            this.customDataOfMethod = customDataClass.getMethod("of", nbtTagCompoundClass);
            this.customDataGetTagMethod = customDataClass.getMethod("copyTag");
            this.setComponentMethod = nmsItemStackClass.getMethod("set", dataComponentTypeClass, Object.class);
        }

        private Object getTag(Object nmsItemStack) throws Exception {
            Object components = getComponentsMethod.invoke(nmsItemStack);
            Object customData = componentsGetMethod.invoke(components, customDataComponent);
            return customData != null ? customDataGetTagMethod.invoke(customData) : nbtTagCompoundConstructor.newInstance();
        }

        private void setTag(Object nmsItemStack, Object tag) throws Exception {
            Object customData = customDataOfMethod.invoke(null, tag);
            setComponentMethod.invoke(nmsItemStack, customDataComponent, customData);
        }

        @Override
        public ItemStack setExpiry(ItemStack item, long expiryTimestamp) throws Exception {
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTag(nmsItem);
            Method putLong = tag.getClass().getMethod("putLong", String.class, long.class);
            putLong.invoke(tag, EXPIRY_KEY, expiryTimestamp);
            setTag(nmsItem, tag);
            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItem);
        }

        @Override
        public long getExpiry(ItemStack item) throws Exception {
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTag(nmsItem);
            if (! (boolean) tag.getClass().getMethod("contains", String.class).invoke(tag, EXPIRY_KEY)) return 0;
            return (long) tag.getClass().getMethod("getLong", String.class).invoke(tag, EXPIRY_KEY);
        }

        @Override
        public boolean hasExpiry(ItemStack item) throws Exception {
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTag(nmsItem);
            return (boolean) tag.getClass().getMethod("contains", String.class).invoke(tag, EXPIRY_KEY);
        }
    }

    // --- Legacy NBT API for pre-1.20.5 ---
    private static class LegacyNbtApi implements NbtApi {
        private final Method asNMSCopyMethod, asBukkitCopyMethod, getTagMethod, setTagMethod, hasKeyMethod, getLongMethod, setLongMethod;
        private final Constructor<?> nbtTagCompoundConstructor;

        LegacyNbtApi() throws Exception {
            String version = Bukkit.getServer().getClass().getPackage().getName().substring(23);
            Class<?> craftItemStackClass = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
            Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
            Class<?> nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

            this.asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
            this.asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", nmsItemStackClass);
            this.getTagMethod = nmsItemStackClass.getMethod("getTag");
            this.setTagMethod = nmsItemStackClass.getMethod("setTag", nbtTagCompoundClass);
            this.hasKeyMethod = nbtTagCompoundClass.getMethod("hasKey", String.class);
            this.getLongMethod = nbtTagCompoundClass.getMethod("getLong", String.class);
            this.setLongMethod = nbtTagCompoundClass.getMethod("setLong", String.class, long.class);
            this.nbtTagCompoundConstructor = nbtTagCompoundClass.getDeclaredConstructor();
            this.nbtTagCompoundConstructor.setAccessible(true);
        }

        private Object getTag(Object nmsItemStack) throws Exception {
            Object tag = getTagMethod.invoke(nmsItemStack);
            return tag != null ? tag : nbtTagCompoundConstructor.newInstance();
        }

        @Override
        public ItemStack setExpiry(ItemStack item, long expiryTimestamp) throws Exception {
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTag(nmsItem);
            setLongMethod.invoke(tag, EXPIRY_KEY, expiryTimestamp);
            setTagMethod.invoke(nmsItem, tag);
            return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItem);
        }

        @Override
        public long getExpiry(ItemStack item) throws Exception {
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTagMethod.invoke(nmsItem);
            if (tag == null || !(boolean) hasKeyMethod.invoke(tag, EXPIRY_KEY)) return 0;
            return (long) getLongMethod.invoke(tag, EXPIRY_KEY);
        }

        @Override
        public boolean hasExpiry(ItemStack item) throws Exception {
            Object nmsItem = asNMSCopyMethod.invoke(null, item);
            Object tag = getTagMethod.invoke(nmsItem);
            return tag != null && (boolean) hasKeyMethod.invoke(tag, EXPIRY_KEY);
        }
    }

    // --- Failsafe No-Op API ---
    private static class NoOpNbtApi implements NbtApi {
        @Override public ItemStack setExpiry(ItemStack item, long expiryTimestamp) { return item; }
        @Override public long getExpiry(ItemStack item) { return 0; }
        @Override public boolean hasExpiry(ItemStack item) { return false; }
    }

    // --- Public Static API ---
    public static ItemStack setExpiry(ItemStack item, long expiryTimestamp) {
        try {
            return nbtApi.setExpiry(item, expiryTimestamp);
        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    public static long getExpiry(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        try {
            return nbtApi.getExpiry(item);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean hasExpiry(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        try {
            return nbtApi.hasExpiry(item);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        long expiryTimestamp = getExpiry(item);
        if (expiryTimestamp == 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> line.startsWith(ChatColor.GRAY + "Expires in:") || line.startsWith(ChatColor.RED + "Expired"));

        long remainingSeconds = (expiryTimestamp - System.currentTimeMillis()) / 1000;
        if (remainingSeconds >= 0) {
            lore.add(0, ChatColor.GRAY + "Expires in: " + formatDuration(remainingSeconds));
        } else {
            lore.add(0, ChatColor.RED + "Expired");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static String formatDuration(long seconds) {
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60, remainingSeconds = seconds % 60;
        if (minutes < 60) return String.format("%dm %ds", minutes, remainingSeconds);
        long hours = minutes / 60, remainingMinutes = minutes % 60;
        if (hours < 24) return String.format("%dh %dm", hours, remainingMinutes);
        long days = hours / 24, remainingHours = hours % 24;
        return String.format("%dd %dh", days, remainingHours);
    }

    // --- Instance for Task ---
    private final Plugin plugin;

    public ItemManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateInventories, 0L, 20L);
    }

    private void updateInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Update inventory contents
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (hasExpiry(item)) {
                    if (System.currentTimeMillis() > getExpiry(item)) {
                        player.getInventory().setItem(i, null);
                    } else {
                        updateLore(item);
                    }
                }
            }
            // Update item on cursor
            ItemStack cursorItem = player.getItemOnCursor();
            if (hasExpiry(cursorItem)) {
                if (System.currentTimeMillis() > getExpiry(cursorItem)) {
                    player.setItemOnCursor(null);
                } else {
                    updateLore(cursorItem);
                }
            }
        }
    }
}
