package ink.ptms.cronus.builder.listener;

import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.task.data.Entity;
import ink.ptms.cronus.service.selector.EntitySelector;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.util.lite.Catchers;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.LinkedList;

/**
 * @Author 坏黑
 * @Since 2019-07-01 13:41
 */
@TListener
public class ListenerEntity implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void e(PlayerInteractAtEntityEvent e) {
        if (e.getHand() == EquipmentSlot.HAND && e.getPlayer().getItemInHand().getType() == Material.NETHER_STAR) {
            try {
                LinkedList<Catchers.Catcher> catchers = Catchers.getPlayerdata().get(e.getPlayer().getName());
                if (catchers == null) {
                    return;
                }
                for (Catchers.Catcher catcher : catchers) {
                    if (catcher instanceof Entity.EntitySelect) {
                        e.setCancelled(true);
                        e.getPlayer().chat(Cronus.getCronusService().getService(EntitySelector.class).fromEntity(e.getRightClicked()));
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
