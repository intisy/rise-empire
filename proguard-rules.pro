-ignorewarnings
-dontwarn org.bukkit.**
-dontwarn org.apache.logging.log4j.**

# Keep the main plugin class and its onEnable/onDisable methods
-keep public class io.github.intisy.riseempire.Plugin extends org.bukkit.plugin.java.JavaPlugin {
    public <init>();
    public void onEnable();
    public void onDisable();
}

# Keep command executors
-keep public class * implements org.bukkit.command.CommandExecutor {
    public <init>(...);
    public boolean onCommand(...);
}

# Keep BukkitRunnables
-keep public class * extends org.bukkit.scheduler.BukkitRunnable {
    public <init>(...);
    public void run();
}

# Keep the constructors and event handlers of Listener classes, but allow the classes to be renamed
-keepclassmembers class * implements org.bukkit.event.Listener {
    public <init>(...);
    @org.bukkit.event.EventHandler
    public void *(...);
}

# Keep other necessary Bukkit/Spigot annotations and attributes
-keepattributes Signature, *Annotation*
