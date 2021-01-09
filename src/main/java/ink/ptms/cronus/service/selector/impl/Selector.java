package ink.ptms.cronus.service.selector.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * @Author 坏黑
 * @Since 2019-07-01 12:00
 */
public abstract class Selector {

    protected Plugin plugin;

    abstract public String[] getPrefix();

    abstract public String getPlugin();

    abstract public String getDisplay(String in, Player player);

    abstract public String fromEntity(Entity entity);

    abstract public boolean isSelect(Entity entity, String in);

    public void init() {
        plugin = Bukkit.getPluginManager().getPlugin(getPlugin());
    }

    public boolean match(String prefix) {
        return Arrays.stream(getPrefix()).anyMatch(prefix::equalsIgnoreCase);
    }

    public boolean isHooked() {
        return plugin != null;
    }
}
