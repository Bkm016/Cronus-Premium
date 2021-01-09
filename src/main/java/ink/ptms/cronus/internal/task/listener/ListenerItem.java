package ink.ptms.cronus.internal.task.listener;

import ink.ptms.cronus.CronusAPI;
import ink.ptms.cronus.internal.task.item.*;
import ink.ptms.cronus.internal.task.player.total.TaskItemDamage;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.util.item.Items;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-05-28 17:24
 */
@TListener
public class ListenerItem implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PlayerItemConsumeEvent e) {
        CronusAPI.stageHandle(e.getPlayer(), e, TaskItemConsume.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PlayerItemBreakEvent e) {
        CronusAPI.stageHandle(e.getPlayer(), e, TaskItemBreak.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PlayerItemDamageEvent e) {
        CronusAPI.stageHandle(e.getPlayer(), e, TaskItemDamage.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(CraftItemEvent e) {
        CronusAPI.stageHandle((Player) e.getWhoClicked(), e, TaskItemCraft.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(EnchantItemEvent e) {
        CronusAPI.stageHandle(e.getEnchanter(), e, TaskItemEnchant.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PlayerPickupItemEvent e) {
        CronusAPI.stageHandle(e.getPlayer(), e, TaskItemPick.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PlayerPickupArrowEvent e) {
        CronusAPI.stageHandle(e.getPlayer(), e, TaskItemPickArrow.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PlayerDropItemEvent e) {
        CronusAPI.stageHandle(e.getPlayer(), e, TaskItemDrop.class);
    }

    @EventHandler
    public void e(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL && Items.nonNull(e.getItem())) {
            CronusAPI.stageHandle(e.getPlayer(), e, TaskItemUse.class);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(PrepareAnvilEvent e) {
        if (!e.getViewers().isEmpty()) {
            CronusAPI.stageHandle((Player) e.getViewers().get(0), e, TaskItemRepair.class);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void e(FurnaceSmeltEvent e) {
        BlockState blockState = e.getBlock().getState();
        if (blockState instanceof Furnace) {
            List<HumanEntity> humanEntities = ((Furnace) blockState).getInventory().getViewers();
            if (!humanEntities.isEmpty()) {
                CronusAPI.stageHandle((Player) humanEntities.get(0), e, TaskItemFurnace.class);
            }
        }
    }
}
