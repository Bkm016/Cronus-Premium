package ink.ptms.cronus.builder.element.condition;

import io.izzel.taboolib.util.lite.Materials;
import io.izzel.taboolib.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @Author 坏黑
 * @Since 2019-06-21 0:14
 */
public enum MatchType {

    SINGLE("单项条件", Material.PAPER, new ItemBuilder(Material.PAPER).name("§7单项条件").lore("", "§8玩家需要达成的条件").build()),

    ALL_MATCH("所有条件", Materials.REPEATER.parseMaterial(), new ItemBuilder(Materials.REPEATER.parseMaterial()).name("§7单项条件 §f(完全匹配)").lore("", "§8玩家需要达成的所有条件").build()),

    ANY_MATCH("任意条件", Materials.COMPARATOR.parseMaterial(), new ItemBuilder(Materials.COMPARATOR.parseMaterial()).name("§7多项条件 §f(单项匹配)").lore("", "§8玩家需要达成的任意条件").build());

    String name;
    Material material;
    ItemStack itemStack;

    MatchType(String name, Material material, ItemStack itemStack) {
        this.name = name;
        this.material = material;
        this.itemStack = itemStack;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return this.material;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
