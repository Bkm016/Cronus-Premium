package ink.ptms.cronus.builder.element;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.ArrayUtil;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.item.inventory.CloseTask;
import io.izzel.taboolib.util.lite.Catchers;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-06-19 0:16
 */
public class BuilderList extends BuilderQuest {

    protected String display;
    protected List<String> list;
    protected List<String> listOrigin;
    protected Player player;
    protected CloseTask close;
    protected Candidate candidate;
    protected int page;
    protected boolean toggle;
    protected boolean append;
    protected Map<Integer, Integer> map = Maps.newHashMap();
    // 默认点击动作
    protected ClickTask defaultEdit = index -> {
        editString(player, display, list.get(index).equals("$append") ? "-" : list.get(index), r -> list.set(index, r), candidate);
    };
    protected ClickTask defaultDelete = index -> {
        list.remove(index);
    };
    protected ClickTask defaultMoveLeft = index -> {
        String element = list.remove(index);
        list.add(index - 1, element);
    };
    protected ClickTask defaultMoveRight = index -> {
        String element = list.remove(index);
        list.add(index + 1, element);
    };

    public BuilderList(String display, List<String> list) {
        super("");
        this.display = display;
        this.listOrigin = list;
        this.list = Lists.newArrayList(list);
    }

    public void open(Player player) {
        open(player, page, close, candidate);
    }

    public void open(Player player, int page) {
        open(player, page, close, candidate);
    }

    public void open(Player player, int page, CloseTask close) {
        open(player, page, close, candidate);
    }

    public void open(Player player, int page, CloseTask close, Candidate candidate) {
        this.map.clear();
        this.page = page;
        this.close = close;
        this.player = player;
        this.candidate = candidate;
        this.toggle = true;
        this.append = this.list.contains("$append") || this.list.add("$append");
        // 创建界面
        Inventory inventory = Builders.normal("结构编辑 : " + display,
                e -> {
                    if (e.getClickType() == ClickType.CLICK && !Items.isNull(e.castClick().getCurrentItem())) {
                        e.castClick().setCancelled(true);
                        // 上一页
                        if (e.castClick().getRawSlot() == 46 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                            open(player, page - 1, close);
                        }
                        // 下一页
                        else if (e.castClick().getRawSlot() == 52 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                            open(player, page + 1, close);
                        }
                        // 返回
                        else if (e.castClick().getRawSlot() == 49) {
                            toggle = true;
                            close(close, null);
                        }
                        // 内容
                        else if (ArrayUtil.contains(Items.INVENTORY_CENTER, e.castClick().getRawSlot())) {
                            try {
                                int index = map.get(e.castClick().getRawSlot());
                                // 左键
                                if (e.castClick().getClick().isLeftClick()) {
                                    // 左移
                                    if (e.castClick().isShiftClick()) {
                                        // 有效
                                        if (index > 0 && !Utils.isActionCooldown(player)) {
                                            defaultMoveLeft.run(index);
                                            open(player, page, close);
                                        }
                                    } else {
                                        defaultEdit.run(index);
                                    }
                                }
                                // 右键
                                else if (e.castClick().getClick().isRightClick()) {
                                    // 右移
                                    if (e.castClick().isShiftClick()) {
                                        // 有效
                                        if (index < list.size() - 2 && !Utils.isActionCooldown(player)) {
                                            defaultMoveRight.run(index);
                                            open(player, page, close);
                                        }
                                    } else {
                                        defaultDelete.run(index);
                                        open(player, page, close);
                                    }
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }
                },
                c -> {
                    if (!toggle) {
                        Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> {
                            close(close, c);
                        }, 1);
                    }
                });
        // 获取成员
        List<String> iterator = Indexed.INSTANCE.subList(this.list, page * 28, (page + 1) * 28 - 1);
        for (int i = 0; i < iterator.size(); i++) {
            // 追加
            if (iterator.get(i).equals("$append")) {
                inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.MAP)
                        .name("§f增加新的" + display)
                        .lore("", "§7点击")
                        .build());
            }
            // 修改 & 删除
            else {
                inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.PAPER)
                        .name("§f" + iterator.get(i))
                        .lore("", "§8§m                  ", "§7修改: §8左键", "§7删除: §8右键", "§7左移: §8SHIFT+左键", "§7右移: §8SHIFT+右键")
                        .build());
            }
            map.put(Items.INVENTORY_CENTER[i], page * 28 + i);
        }
        if (page > 0) {
            inventory.setItem(46, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a上一页").lore("", "§7点击").build());
        }
        if (Utils.next(page, list.size(), 28)) {
            inventory.setItem(52, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a下一页").lore("", "§7点击").build());
        }
        inventory.setItem(49, new ItemBuilder(Materials.RED_STAINED_GLASS_PANE.parseItem()).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        this.toggle = false;
    }

    protected void close(CloseTask close, InventoryCloseEvent o) {
        listOrigin.clear();
        list.stream().filter(l -> !l.equals("$append")).forEach(listOrigin::add);
        if (close != null) {
            close.run(o);
        }
    }

    protected void editString(Player player, String display, String origin, EditTask edit, Candidate candidate) {
        Catchers.call(player, new Catchers.Catcher() {
            @Override
            public Catchers.Catcher before() {
                toggle = true;
                player.closeInventory();
                TellrawJson.create().append("§7§l[§f§lCronus§7§l] §7在对话框中输入新的" + display + ". ")
                        .append("§8(取消)").hoverText("§7点击").clickCommand("cancel")
                        .send(player);
                TellrawJson.create().append("§7§l[§f§lCronus§7§l] §7当前: ")
                        .append("§f" + Utils.NonNull(origin)).hoverText("§7点击").clickSuggest(Utils.NonNull(origin))
                        .send(player);
                if (candidate != null) {
                    Map<String, String> candidateMap = candidate.run();
                    if (!candidateMap.isEmpty()) {
                        TellrawJson json = TellrawJson.create().append("§7§l[§f§lCronus§7§l] §7候选: §f");
                        int i = 1;
                        for (Map.Entry<String, String> entry : candidateMap.entrySet()) {
                            json.append("§f" + entry.getKey()).hoverText("§f" + entry.getValue()).clickSuggest(TLocale.Translate.setUncolored(entry.getValue()));
                            if (i++ < candidateMap.size()) {
                                json.append("§8, ");
                            }
                        }
                        json.send(player);
                    }
                }
                return this;
            }

            @Override
            public boolean after(String s) {
                edit.run(s);
                open(player, page, close);
                return false;
            }

            @Override
            public void cancel() {
                open(player, page, close);
            }
        });
    }

    @Override
    public String getDisplay() {
        return display;
    }

    public List<String> getList() {
        return list;
    }

    public List<String> getListOrigin() {
        return listOrigin;
    }

    public Player getPlayer() {
        return player;
    }

    public CloseTask getClose() {
        return close;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public int getPage() {
        return page;
    }

    public boolean isToggle() {
        return toggle;
    }

    public boolean isAppend() {
        return append;
    }

    public Map<Integer, Integer> getMap() {
        return map;
    }

    public ClickTask getDefaultEdit() {
        return defaultEdit;
    }

    public ClickTask getDefaultDelete() {
        return defaultDelete;
    }

    public ClickTask getDefaultMoveLeft() {
        return defaultMoveLeft;
    }

    public ClickTask getDefaultMoveRight() {
        return defaultMoveRight;
    }

    public interface Candidate {

        Map<String, String> run();
    }

    public interface ClickTask {

        void run(int index);
    }
}
