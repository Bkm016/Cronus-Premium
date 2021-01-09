package ink.ptms.cronus.service.selector.impl;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

/**
 * @Author 坏黑
 * @Since 2019-07-01 12:02
 */
public class SelectorCitizens extends Selector {

    @Override
    public String[] getPrefix() {
        return new String[] {"citizens", "npc"};
    }

    @Override
    public String getPlugin() {
        return "Citizens";
    }

    @Override
    public String getDisplay(String in, Player player) {
        NPC npc = ((Citizens) plugin).getNPCRegistry().getById(NumberConversions.toInt(in));
        return npc == null ? "?" : npc.getName();
    }

    @Override
    public String fromEntity(Entity entity) {
        NPC npc = ((Citizens) plugin).getNPCRegistry().getNPC(entity);
        return npc != null ? String.valueOf(npc.getId()) : null;
    }

    @Override
    public boolean isSelect(Entity entity, String in) {
        NPC npc = ((Citizens) plugin).getNPCRegistry().getNPC(entity);
        return npc != null && npc.getId() == NumberConversions.toInt(in);
    }
}
