package ink.ptms.cronus.builder.task.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.builder.element.BuilderTaskData;
import ink.ptms.cronus.builder.task.Cache;
import ink.ptms.cronus.builder.task.TaskData;
import ink.ptms.cronus.internal.bukkit.parser.BukkitParser;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.item.inventory.MenuBuilder;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author 坏黑
 * @Since 2019-06-22 22:24
 */
public class Block extends TaskData {

    protected List<ItemStack> selected = Lists.newArrayList();
    protected Map<Integer, ItemStack> mapSelect = Maps.newHashMap();
    protected Map<Integer, ItemStack> mapDelete = Maps.newHashMap();
    private boolean toggle;

    public Block(Player player, BuilderTaskData builderTaskData) {
        super(player, builderTaskData);
    }

    @Override
    public void setData(Object data) {
        this.data = data;
        this.selected = Lists.newArrayList();
        // 导入数据
        if (data == null || Strings.isEmpty((String) data)) {
            return;
        }
        for (ink.ptms.cronus.internal.bukkit.Block.Point point : BukkitParser.toBlock(data).getPoints()) {
            selected.add(Materials.isNewVersion() ? new ItemStack(Items.asMaterial(point.getName())) : new ItemStack(Items.asMaterial(point.getName()), 1, (short) point.getData()));
        }
    }

    @Override
    public String getKey() {
        return "block";
    }

    @Override
    public ItemStack getItem() {
        List<String> lore = Lists.newArrayList("");
        if (selected.isEmpty()) {
            lore.add("§f无");
        }
        for (int i = 0; i < selected.size() && i < 8; i++) {
            lore.add("§f" + Items.getName(selected.get(i)));
        }
        if (selected.size() > 8) {
            lore.add("§f...");
        }
        lore.add("§8§m                  ");
        lore.add("§7选择: §8左键");
        lore.add("§7导入: §8右键");
        lore.add("§7删除: §8中键");
        return new ItemBuilder(Materials.GRASS_BLOCK.parseMaterial())
                .name("§7方块类型")
                .lore(lore).build();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        // 删除
        if (e.getClick().isCreativeAction()) {
            openDelete(0);
        }
        // 选择
        else if (e.isLeftClick()) {
            openSelect(0);
        }
        // 导入
        else if (e.isRightClick()) {
            player.openInventory(MenuBuilder.builder(Cronus.getInst())
                    .title("实例选择 : 方块导入")
                    .rows(3)
                    .items()
                    .close(c -> {
                        int i = 0;
                        for (ItemStack itemStack : c.getInventory()) {
                            if (Items.isNull(itemStack) || !itemStack.getType().isBlock()) {
                                continue;
                            }
                            if (!selected.contains(Materials.isNewVersion() ? new ItemStack(itemStack.getType()) : new ItemStack(itemStack.getType(), 1, itemStack.getDurability()))) {
                                selected.add(itemStack);
                                i++;
                            }
                        }
                        if (i > 0) {
                            player.sendMessage("§7§l[§f§lCronus§7§l] §7导入 §f" + i + " §7种方块.");
                        }
                        Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> {
                            save();
                            builderTaskData.open();
                        }, 1);
                    }).build());
        }
    }

    @Override
    public Object defaultValue() {
        return null;
    }

    public void save() {
        String block = selected.stream().filter(Objects::nonNull).map(s -> Materials.isNewVersion() ? s.getType().name() : s.getType().name() + ":" + s.getDurability()).collect(Collectors.joining(","));
        saveData(!block.isEmpty() ? block : null);
    }

    public void openSelect(int page) {
        toggle = true;
        mapSelect.clear();
        Inventory inventory = Builders.normal("实例选择 : 方块类型",
                c -> {
                    if (c.getClickType() == ClickType.CLICK) {
                        c.castClick().setCancelled(true);
                        if (c.castClick().getRawSlot() == 46 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(c.castClick().getCurrentItem())) {
                            openSelect(page - 1);
                        } else if (c.castClick().getRawSlot() == 52 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(c.castClick().getCurrentItem())) {
                            openSelect(page + 1);
                        } else if (c.castClick().getRawSlot() == 49) {
                            toggle = true;
                            save();
                            builderTaskData.open();
                        } else {
                            ItemStack selectItem = mapSelect.get(c.castClick().getRawSlot());
                            if (selectItem != null) {
                                if (selected.contains(selectItem)) {
                                    selected.remove(selectItem);
                                } else {
                                    selected.add(selectItem);
                                }
                                openSelect(page);
                            }
                        }
                    }
                },
                c -> {
                    if (!toggle) {
                        Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> {
                            save();
                            builderTaskData.open();
                        }, 1);
                    }
                });
        List<ItemStack> iterator = Indexed.INSTANCE.subList(Cache.BLOCKS, page * 28, (page + 1) * 28 - 1);
        for (int i = 0; i < iterator.size(); i++) {
            ItemStack parseItem = iterator.get(i).clone();
            try {
                if (selected.contains(parseItem)) {
                    inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(parseItem).lore("", "§8取消").shiny().build());
                } else {
                    inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(parseItem).lore("", "§8选择").build());
                }
                mapSelect.put(Items.INVENTORY_CENTER[i], parseItem);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (page > 0) {
            inventory.setItem(46, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a上一页").lore("", "§7点击").build());
        }
        if (Utils.next(page, Cache.BLOCKS.size(), 28)) {
            inventory.setItem(52, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a下一页").lore("", "§7点击").build());
        }
        inventory.setItem(49, new ItemBuilder(Materials.RED_STAINED_GLASS_PANE.parseItem()).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        toggle = false;
    }

    public void openDelete(int page) {
        toggle = true;
        mapDelete.clear();
        Inventory inventory = Builders.normal("实例删除 : 方块类型",
                c -> {
                    if (c.getClickType() == ClickType.CLICK) {
                        c.castClick().setCancelled(true);
                        if (c.castClick().getRawSlot() == 46 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(c.castClick().getCurrentItem())) {
                            openDelete(page - 1);
                        } else if (c.castClick().getRawSlot() == 52 && Materials.GREEN_STAINED_GLASS_PANE.isSimilar(c.castClick().getCurrentItem())) {
                            openDelete(page + 1);
                        } else if (c.castClick().getRawSlot() == 49) {
                            toggle = true;
                            save();
                            builderTaskData.open();
                        } else {
                            ItemStack item = mapDelete.get(c.castClick().getRawSlot());
                            if (item != null) {
                                selected.remove(item);
                                openDelete(page);
                            }
                        }
                    }
                },
                c -> {
                    if (!toggle) {
                        Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> {
                            save();
                            builderTaskData.open();
                        }, 1);
                    }
                });
        List<ItemStack> iterator = Indexed.INSTANCE.subList(selected, page * 28, (page + 1) * 28 - 1);
        for (int i = 0; i < iterator.size(); i++) {
            try {
                inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(iterator.get(i)).lore("", "§8删除").build());
                mapDelete.put(Items.INVENTORY_CENTER[i], iterator.get(i));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (page > 0) {
            inventory.setItem(46, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a上一页").lore("", "§7点击").build());
        }
        if (Utils.next(page, selected.size(), 28)) {
            inventory.setItem(52, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a下一页").lore("", "§7点击").build());
        }
        inventory.setItem(49, new ItemBuilder(Materials.RED_STAINED_GLASS_PANE.parseItem()).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        toggle = false;
    }
}
