package ink.ptms.cronus.builder.listener;

import ink.ptms.cronus.builder.task.data.Location;
import ink.ptms.cronus.command.CronusCommand;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.util.lite.Catchers;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-06-23 13:06
 */
@TListener
public class ListenerLocation implements Listener {

    @EventHandler
    public void e(PlayerInteractEvent e) {
        if (Catchers.getPlayerdata().containsKey(e.getPlayer().getName()) && e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().getItemInHand().getType() == Material.NETHER_STAR) {
            LinkedList<Catchers.Catcher> catchers = Catchers.getPlayerdata().get(e.getPlayer().getName());
            for (Catchers.Catcher catcher : catchers) {
                // 单项
                if (catcher instanceof Location.LocationPoint) {
                    e.setCancelled(true);
                    List<org.bukkit.Location> selected = ((Location.LocationPoint) catcher).getLocation().getLocation();
                    if (selected.contains(e.getClickedBlock().getLocation())) {
                        CronusCommand.error(e.getPlayer(), "该坐标已被选择.");
                        return;
                    }
                    selected.add(e.getClickedBlock().getLocation());
                    CronusCommand.normal(e.getPlayer(), "坐标已添加.");
                }
                // 区域
                else if (catcher instanceof Location.LocationArea) {
                    e.setCancelled(true);
                    org.bukkit.Location[] selected = ((Location.LocationArea) catcher).getLocation().getLocationArea();
                    selected[1] = e.getClickedBlock().getLocation();
                    CronusCommand.normal(e.getPlayer(), "坐标 B 已设置.");
                }
            }
        }
    }

    @EventHandler
    public void e(BlockBreakEvent e) {
        if (Catchers.getPlayerdata().containsKey(e.getPlayer().getName()) && e.getPlayer().getItemInHand().getType() == Material.NETHER_STAR) {
            LinkedList<Catchers.Catcher> catchers = Catchers.getPlayerdata().get(e.getPlayer().getName());
            for (Catchers.Catcher catcher : catchers) {
                // 单项
                if (catcher instanceof Location.LocationPoint) {
                    e.setCancelled(true);
                    List<org.bukkit.Location> selected = ((Location.LocationPoint) catcher).getLocation().getLocation();
                    if (!selected.contains(e.getBlock().getLocation())) {
                        CronusCommand.error(e.getPlayer(), "该坐标未被选择.");
                        return;
                    }
                    selected.remove(e.getBlock().getLocation());
                    CronusCommand.normal(e.getPlayer(), "坐标已删除.");
                }
                // 区域
                else if (catcher instanceof Location.LocationArea) {
                    e.setCancelled(true);
                    org.bukkit.Location[] selected = ((Location.LocationArea) catcher).getLocation().getLocationArea();
                    selected[0] = e.getBlock().getLocation();
                    CronusCommand.normal(e.getPlayer(), "坐标 A 已设置.");
                }
            }
        }
    }
}
