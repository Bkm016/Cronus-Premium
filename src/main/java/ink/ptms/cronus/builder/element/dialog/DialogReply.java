package ink.ptms.cronus.builder.element.dialog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-06-30 11:29
 */
public class DialogReply {

    private final Dialog dialog;
    private final Player player;
    private int page;
    private boolean toggle;
    private boolean append;
    private List<Dialog> reply;
    private final Map<Integer, Integer> map = Maps.newHashMap();

    public DialogReply(Dialog dialog, Player player) {
        this.dialog = dialog;
        this.player = player;
    }

    public void open(int page) {
        this.toggle = true;
        this.page = page;
        this.reply = Lists.newArrayList(dialog.getReply());
        this.reply.add(new DialogAppend(dialog));
        Inventory inventory = Builders.normal("结构编辑 : 对话回复",
                e -> {
                    if (e.getClickType() == ClickType.CLICK && !Items.isNull(e.castClick().getCurrentItem())) {
                        e.castClick().setCancelled(true);
                        // 上一页
                        if (e.castClick().getRawSlot() == 46 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                            open(page - 1);
                        }
                        // 下一页
                        else if (e.castClick().getRawSlot() == 52 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                            open(page + 1);
                        }
                        // 返回
                        else if (e.castClick().getRawSlot() == 49) {
                            toggle = true;
                            dialog.open(player);
                        }
                        // 内容
                        else if (ArrayUtil.contains(Items.INVENTORY_CENTER, e.castClick().getRawSlot())) {
                            try {
                                int index = map.get(e.castClick().getRawSlot());
                                Dialog replyDialog = reply.get(index);
                                // 左键
                                if (e.castClick().getClick().isLeftClick()) {
                                    // 左移
                                    if (e.castClick().isShiftClick()) {
                                        // 新建
                                        if (replyDialog instanceof DialogAppend) {
                                            return;
                                        }
                                        // 有效
                                        if (index > 0 && !Utils.isActionCooldown(player)) {
                                            Dialog element = dialog.getReply().remove(index);
                                            dialog.getReply().add(index - 1, element);
                                            open(page);
                                        }
                                    } else {
                                        // 新建
                                        if (replyDialog instanceof DialogAppend) {
                                            dialog.getReply().add(new Dialog(!dialog.isInReply(), dialog));
                                            open(page);
                                        }
                                        // 修改
                                        else {
                                            toggle = true;
                                            replyDialog.open(player, c -> open(page));
                                        }
                                    }
                                }
                                // 右键
                                else if (e.castClick().getClick().isRightClick()) {
                                    // 右移
                                    if (e.castClick().isShiftClick()) {
                                        // 新建
                                        if (replyDialog instanceof DialogAppend) {
                                            return;
                                        }
                                        // 有效
                                        if (index < reply.size() - 2 && !Utils.isActionCooldown(player)) {
                                            Dialog element = dialog.getReply().remove(index);
                                            dialog.getReply().add(index + 1, element);
                                            open(page);
                                        }
                                    } else {
                                        // 新建
                                        if (replyDialog instanceof DialogAppend) {
                                            return;
                                        }
                                        // 删除
                                        dialog.getReply().remove(index);
                                        open(page);
                                    }
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }
                }, e -> {
                    if (!toggle) {
                        Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> {
                            dialog.open(player);
                        }, 1);
                    }
                });
        List<Dialog> iterator = Indexed.INSTANCE.subList(reply, page * 28, (page + 1) * 28 - 1);
        for (int i = 0; i < iterator.size(); i++) {
            if (iterator.get(i) instanceof DialogAppend) {
                inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.MAP)
                        .name("§f增加新的回复")
                        .lore("", "§7点击")
                        .build());
            } else {
                List<String> text = iterator.get(i).getText();
                List<String> lore = Lists.newArrayList("");
                for (int j = 0; j < text.size() && j < 8; j++) {
                    lore.add("§f" + text.get(j));
                }
                lore.addAll(Lists.newArrayList("§8§m                  ", "§7修改: §8左键", "§7删除: §8右键", "§7左移: §8SHIFT+左键", "§7右移: §8SHIFT+右键"));
                inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.PAPER)
                        .name("§f第 " + (page + i + 1) + " 项")
                        .lore(lore)
                        .build());
            }
            map.put(Items.INVENTORY_CENTER[i], page * 28 + i);
        }
        if (page > 0) {
            inventory.setItem(46, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a上一页").lore("", "§7点击").build());
        }
        if (Utils.next(page, reply.size(), 28)) {
            inventory.setItem(52, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a下一页").lore("", "§7点击").build());
        }
        inventory.setItem(49, new ItemBuilder(Materials.RED_STAINED_GLASS_PANE.parseItem()).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        this.toggle = false;
    }
}
