package ink.ptms.cronus.builder.element;

import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.builder.task.TaskEntry;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.internal.xseries.XMaterial;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-06-22 22:41
 */
public class BuilderTaskType {

    private final BuilderTask builderTask;
    private boolean toggle;
    private final Map<Integer, TaskEntry> map = Maps.newHashMap();

    public BuilderTaskType(BuilderTask builderTask) {
        this.builderTask = builderTask;
    }

    public void open(Player player) {
        this.open(player, 0);
    }

    public void open(Player player, int page) {
        this.map.clear();
        this.toggle = true;
        // 创建界面
        Inventory inventory = Builders.normal("结构编辑 : 条目类型",
                e -> {
                    if (e.getClickType() == ClickType.CLICK) {
                        e.castClick().setCancelled(true);
                        // 上一页
                        if (e.castClick().getRawSlot() == 46 && XMaterial.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                            open(player, page - 1);
                        }
                        // 下一页
                        else if (e.castClick().getRawSlot() == 52 && XMaterial.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                            open(player, page + 1);
                        }
                        // 返回
                        else if (e.castClick().getRawSlot() == 49) {
                            toggle = true;
                            builderTask.open(player);
                        }
                        // 内容
                        else if (map.containsKey(e.castClick().getRawSlot())) {
                            toggle = true;
                            builderTask.setType(map.get(e.castClick().getRawSlot()).getKey());
                            builderTask.setTypeEntry(map.get(e.castClick().getRawSlot()));
                            builderTask.open(player);
                        }
                    }
                },
                e -> {
                    if (!toggle) {
                        Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> {
                            builderTask.open(player);
                        }, 1);
                    }
                }
        );
        List<TaskEntry> iterator = Indexed.INSTANCE.subList(Builders.getTaskEntries(), page * 28, (page + 1) * 28 - 1);
        for (int i = 0; i < iterator.size(); i++) {
            inventory.setItem(Items.INVENTORY_CENTER[i], iterator.get(i).getItem());
            map.put(Items.INVENTORY_CENTER[i], iterator.get(i));
        }
        if (page > 0) {
            inventory.setItem(46, new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("§a上一页").lore("", "§7点击").build());
        }
        if (Utils.next(page, Builders.getTaskEntries().size(), 28)) {
            inventory.setItem(52, new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("§a下一页").lore("", "§7点击").build());
        }
        inventory.setItem(49, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        this.toggle = false;
    }
}
