-ignorewarnings

# Keep the main plugin class and its onEnable/onDisable methods
-keep public class io.github.intisy.riseempire.Plugin {
    public <init>();
    public void onEnable();
    public void onDisable();
}

# Keep the constructors and event handlers of Listener classes, but allow the classes to be renamed
-keepclassmembers class * implements org.bukkit.event.Listener {
    public <init>(...);
    @org.bukkit.event.EventHandler
    public void *(...);
}

# Keep other necessary Bukkit/Spigot annotations and attributes
-keepattributes Signature, *Annotation*
