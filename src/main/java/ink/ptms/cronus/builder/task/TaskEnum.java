package ink.ptms.cronus.builder.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.builder.element.BuilderTaskData;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author 坏黑
 * @Since 2019-06-22 22:24
 */
public abstract class TaskEnum<T extends Enum<?>> extends TaskData {

    protected List<T> selected = Lists.newArrayList();
    private boolean toggle;

    public TaskEnum(Player player, BuilderTaskData builderTaskData) {
        super(player, builderTaskData);
    }

    abstract public void init(Object data);

    abstract public T[] values();

    abstract public String getName();

    abstract public Material getMaterial();

    @Override
    public void setData(Object data) {
        this.data = data;
        this.selected = Lists.newArrayList();
        // 导入数据
        if (data == null || Strings.isEmpty((String) data)) {
            return;
        }
        init(data);
    }

    @Override
    public ItemStack getItem() {
        List<String> lore = Lists.newArrayList("");
        if (selected.isEmpty()) {
            lore.add("§f无");
        }
        for (int i = 0; i < selected.size() && i < 8; i++) {
            lore.add("§f" + selected.get(i));
        }
        if (selected.size() > 8) {
            lore.add("§f...");
        }
        return new ItemBuilder(getMaterial())
                .name("§7" + getName())
                .lore(lore).build();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        openSelect(0);
    }

    @Override
    public Object defaultValue() {
        return null;
    }

    public void save() {
        saveData(selected.stream().map(Enum::name).collect(Collectors.joining(",")));
    }

    public void openSelect(int page) {
        toggle = true;
        Map<Integer, T> map = Maps.newHashMap();
        Inventory inventory = Builders.normal("实例选择 : " + getName(),
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
                            T e = map.get(c.castClick().getRawSlot());
                            if (e != null) {
                                if (selected.contains(e)) {
                                    selected.remove(e);
                                } else {
                                    selected.add(e);
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
        List<T> iterator = Indexed.INSTANCE.subList(Lists.newArrayList(values()), page * 28, (page + 1) * 28 - 1);
        for (int i = 0; i < iterator.size(); i++) {
            try {
                T e = iterator.get(i);
                if (selected.contains(e)) {
                    inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.PAPER).name("§f" + e.name()).lore("", "§8取消").shiny().build());
                } else {
                    inventory.setItem(Items.INVENTORY_CENTER[i], new ItemBuilder(Material.PAPER).name("§f" + e.name()).lore("", "§8选择").build());
                }
                map.put(Items.INVENTORY_CENTER[i], e);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (page > 0) {
            inventory.setItem(46, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a上一页").lore("", "§7点击").build());
        }
        if (Utils.next(page, values().length, 28)) {
            inventory.setItem(52, new ItemBuilder(Materials.GREEN_STAINED_GLASS_PANE.parseItem()).name("§a下一页").lore("", "§7点击").build());
        }
        inventory.setItem(49, new ItemBuilder(Materials.RED_STAINED_GLASS_PANE.parseItem()).name("§c上级目录").lore("", "§7点击").build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        toggle = false;
    }
}
