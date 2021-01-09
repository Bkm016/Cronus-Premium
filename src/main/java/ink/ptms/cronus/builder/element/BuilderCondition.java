package ink.ptms.cronus.builder.element;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.builder.element.condition.MatchAppend;
import ink.ptms.cronus.builder.element.condition.MatchEntry;
import ink.ptms.cronus.builder.element.condition.MatchType;
import ink.ptms.cronus.internal.api.Helper;
import ink.ptms.cronus.internal.condition.Cond;
import ink.ptms.cronus.internal.condition.CondNull;
import ink.ptms.cronus.internal.condition.ConditionParser;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.module.locale.TLocale;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickTask;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.item.inventory.CloseTask;
import io.izzel.taboolib.util.lite.Catchers;
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
 * @Since 2019-06-20 23:39
 */
public class BuilderCondition implements Helper {

    private final Player player;
    private final String display;
    private MatchEntry entry;
    private BuilderCondition parent;
    private final Map<Integer, MatchEntry> entryMap = Maps.newHashMap();
    private ClickTask clickTask;
    private CloseTask closeTask;
    private boolean toggle;
    private int page;

    public BuilderCondition(MatchEntry entry, Player player, String display) {
        this.entry = entry;
        this.player = player;
        this.display = display;
        this.clickTask = e -> {
            if (e.getClickType() == ClickType.DRAG) {
                return;
            }
            e.castClick().setCancelled(true);
            // 关闭界面
            if (e.castClick().getRawSlot() == 49) {
                toggle = true;
                // 执行动作
                if (closeTask != null) {
                    closeTask.run(null);
                }
                return;
            }
            // 下一页
            else if (e.castClick().getRawSlot() == 52 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                open(player, page + 1);
                return;
            }
            // 上一页
            else if (e.castClick().getRawSlot() == 46 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(e.castClick().getCurrentItem())) {
                open(player, page - 1);
                return;
            }
            // 新建内容
            if (this.entry == null) {
                switch (e.castClick().getRawSlot()) {
                    case 20: {
                        editString(player, null, s -> this.entry = new MatchEntry(MatchType.SINGLE, s));
                        break;
                    }
                    case 22: {
                        this.toggle = true;
                        this.entry = new MatchEntry(MatchType.ALL_MATCH, Lists.newArrayList());
                        BuilderCondition builder = new BuilderCondition(this.entry, player, display);
                        builder.setCloseTask(closeTask);
                        builder.open(player, page);
                        break;
                    }
                    case 24: {
                        this.toggle = true;
                        this.entry = new MatchEntry(MatchType.ANY_MATCH, Lists.newArrayList());
                        BuilderCondition builder = new BuilderCondition(this.entry, player, display);
                        builder.setCloseTask(closeTask);
                        builder.open(player, page);
                        break;
                    }
                }
            }
            // 单项条件
            else if (this.entry.getType() == MatchType.SINGLE && e.castClick().getRawSlot() == 22) {
                // 修改
                if (e.castClick().isLeftClick()) {
                    editString(player, this.entry.getSingle(), s -> this.entry = new MatchEntry(MatchType.SINGLE, s));
                }
                // 删除
                else if (e.castClick().isRightClick()) {
                    this.entry = null;
                    open(player, page);
                }
            }
            // 多项条件
            else if (this.entry.getType() != MatchType.SINGLE) {
                MatchEntry matchEntry = entryMap.get(e.castClick().getRawSlot());
                // 增加条件
                if (matchEntry instanceof MatchAppend) {
                    // 左键
                    if (e.castClick().isLeftClick()) {
                        BuilderCondition condition = new BuilderCondition(null, e.getClicker(), display);
                        condition.setParent(this);
                        condition.setCloseTask(c -> {
                            if (condition.getEntry() != null) {
                                this.entry.getCollect().add(condition.getEntry());
                            }
                            Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> open(e.getClicker(), page), 1);
                        });
                        this.toggle = true;
                        condition.open(player, 0);
                    }
                    // 右键
                    else if (e.castClick().isRightClick() && entryMap.size() == 1) {
                        this.entry = null;
                        open(player, page);
                    }
                }
                // 修改条件
                else if (matchEntry != null) {
                    // 左键
                    if (e.castClick().isLeftClick()) {
                        // 单项
                        if (matchEntry.getType() == MatchType.SINGLE) {
                            editString(player, matchEntry.getSingle(), matchEntry::setSingle);
                        }
                        // 多项
                        else {
                            BuilderCondition condition = new BuilderCondition(matchEntry, e.getClicker(), display);
                            condition.setCloseTask(c -> {
                                matchEntry.setType(condition.getEntry().getType());
                                matchEntry.setCollect(condition.getEntry().getCollect());
                                Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> open(e.getClicker(), page), 1);
                            });
                            this.toggle = true;
                            condition.open(player, 0);
                        }
                    }
                    // 右键
                    else if (e.castClick().isRightClick()) {
                        this.entry.getCollect().remove(matchEntry);
                        open(player, 0);
                    }
                }
            }
        };
    }

    public void open(Player pLayer) {
        open(player, page);
    }

    public void open(Player player, int page) {
        this.page = page;
        this.toggle = true;
        Inventory inventory = Builders.normal("结构编辑 : " + display, clickTask, e -> {
            if (!toggle && closeTask != null) {
                closeTask.run(e);
            }
        });
        if (entry == null) {
            inventory.setItem(20, MatchType.SINGLE.getItemStack());
            inventory.setItem(22, MatchType.ALL_MATCH.getItemStack());
            inventory.setItem(24, MatchType.ANY_MATCH.getItemStack());
        } else if (entry.getType() == MatchType.SINGLE) {
            inventory.setItem(4, entry.getType().getItemStack());
            inventory.setItem(22, new ItemBuilder(Material.PAPER).name("§f" + entry.getSingleTranslate()).lore("", "§7左键编辑 §8| §7右键删除").build());
        } else {
            entryMap.clear();
            // 新增内容标记
            List<MatchEntry> entryCollect = Lists.newArrayList(entry.getCollect());
            entryCollect.add(new MatchAppend());
            // 获取有效内容
            List<MatchEntry> iterator = Indexed.INSTANCE.subList(entryCollect, page * 28, (page + 1) * 28 - 1);
            for (int i = 0; i < iterator.size(); i++) {
                MatchEntry entry = iterator.get(i);
                if (entry instanceof MatchAppend) {
                    if (entryCollect.size() == 1) {
                        inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.MAP).name("§f增加新的" + display).lore("", "§7左键增加 §8| §7右键删除").build());
                    } else {
                        inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.MAP).name("§f增加新的" + display).lore("", "§7点击").build());
                    }
                } else if (entry.getType() == MatchType.SINGLE) {
                    inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(entry.getType().getMaterial()).name("§f" + entry.getSingleTranslate()).lore("", "§7左键编辑 §8| §7右键删除").build());
                } else {
                    inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(entry.getType().getMaterial()).name("§f...").lore("", "§7左键编辑 §8| §7右键删除").build());
                }
                entryMap.put(Items.INVENTORY_CENTER[i], entry);
            }
            if (page > 0) {
                inventory.setItem(46, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a上一页").lore("", "§7点击").build());
            }
            if (Utils.next(page, entryCollect.size(), 28)) {
                inventory.setItem(52, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a下一页").lore("", "§7点击").build());
            }
            inventory.setItem(4, entry.getType().getItemStack());
        }
        inventory.setItem(49, new ItemBuilder(Materials.RED_STAINED_GLASS_PANE.parseItem()).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        this.toggle = false;
    }

    protected void editString(Player player, String origin, BuilderQuest.EditTask edit) {
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
                Map<String, String> conditionMap = Maps.newTreeMap();
                Cronus.getCronusService().getRegisteredCondition().forEach((key, value) -> {
                    Cond annotation = value.getCondition().getAnnotation(Cond.class);
                    conditionMap.put(value.getCondition().getSimpleName().replace("Cond", ""), format(annotation.example()));
                });
                if (!conditionMap.isEmpty()) {
                    TellrawJson json = TellrawJson.create().append("§7§l[§f§lCronus§7§l] §7候选: §f");
                    int i = 1;
                    for (Map.Entry<String, String> entry : conditionMap.entrySet()) {
                        json.append("§f" + entry.getKey()).hoverText("§f" + entry.getValue()).clickSuggest(TLocale.Translate.setUncolored(entry.getValue()));
                        if (i++ < conditionMap.size()) {
                            json.append("§8, ");
                        }
                    }
                    json.send(player);
                }
                return this;
            }

            @Override
            public boolean after(String s) {
                if (ConditionParser.parse(s) instanceof CondNull) {
                    Helper.error(player, "条件格式错误.");
                    return true;
                }
                edit.run(s);
                if (parent != null) {
                    parent.getEntry().getCollect().add(entry);
                    parent.open(player);
                } else {
                    open(player, page);
                }
                return false;
            }

            @Override
            public void cancel() {
                open(player, page);
            }
        });
    }

    private String format(String example) {
        return "§7" + example.replaceAll("\\.", "§8$0§7").replaceAll("\\[(\\S+)]", "§e[§6$1§e]§7");
    }

    public void setClickTask(ClickTask clickTask) {
        this.clickTask = clickTask;
    }

    public void setCloseTask(CloseTask closeTask) {
        this.closeTask = closeTask;
    }

    public void setParent(BuilderCondition parent) {
        this.parent = parent;
    }

    public MatchEntry getEntry() {
        return entry;
    }

    public Player getPlayer() {
        return player;
    }

    public String getDisplay() {
        return display;
    }

    public BuilderCondition getParent() {
        return parent;
    }

    public Map<Integer, MatchEntry> getEntryMap() {
        return entryMap;
    }

    public ClickTask getClickTask() {
        return clickTask;
    }

    public CloseTask getCloseTask() {
        return closeTask;
    }

    public boolean isToggle() {
        return toggle;
    }

    public int getPage() {
        return page;
    }
}
