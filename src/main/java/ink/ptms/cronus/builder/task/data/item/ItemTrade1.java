package ink.ptms.cronus.builder.task.data.item;

import ink.ptms.cronus.builder.element.BuilderTaskData;
import ink.ptms.cronus.builder.task.data.Item;
import ink.ptms.cronus.internal.bukkit.parser.BukkitParser;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.entity.Player;

/**
 * @Author 坏黑
 * @Since 2019-06-30 16:00
 */
public class ItemTrade1 extends Item {

    public ItemTrade1(Player player, BuilderTaskData builderTaskData) {
        super(player, builderTaskData);
    }

    @Override
    public org.bukkit.inventory.ItemStack getItem() {
        ink.ptms.cronus.internal.bukkit.ItemStack cronusItem = data == null ? null : BukkitParser.toItemStack(data);
        return new ItemBuilder(Materials.EMERALD.parseMaterial())
                .name("§7目标物品 (交易位置 1)")
                .lore(
                        "",
                        "§f" + (data == null ? "无" : (cronusItem.getBukkitItem() == null ? cronusItem.asString() : "bukkit:" + Items.getName(cronusItem.getBukkitItem()))),
                        "§8§m                  ",
                        "§7物品导入: §8左键",
                        "§7模糊判断: §8右键"
                ).build();
    }

    @Override
    public String getKey() {
        return "item1";
    }
}
