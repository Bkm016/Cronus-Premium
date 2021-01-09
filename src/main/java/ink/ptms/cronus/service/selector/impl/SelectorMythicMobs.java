package ink.ptms.cronus.service.selector.impl;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * @Author 坏黑
 * @Since 2019-07-01 12:02
 */
public class SelectorMythicMobs extends Selector {

    @Override
    public String[] getPrefix() {
        return new String[] {"mythicmobs", "mythicmob", "mm"};
    }

    @Override
    public String getPlugin() {
        return "MythicMobs";
    }

    @Override
    public String getDisplay(String in, Player player) {
        MythicMob mythicMob = ((MythicMobs) plugin).getMobManager().getMythicMob(in);
        return mythicMob == null ? "?" : mythicMob.getDisplayName().get();
    }

    @Override
    public String fromEntity(Entity entity) {
        ActiveMob activeMob = ((MythicMobs) plugin).getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        return activeMob != null ? activeMob.getType().getInternalName() : null;
    }

    @Override
    public boolean isSelect(Entity entity, String in) {
        ActiveMob activeMob = ((MythicMobs) plugin).getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        return activeMob != null && activeMob.getType().getInternalName().equals(in);
    }
}
