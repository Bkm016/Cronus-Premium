package ink.ptms.cronus.builder.task.item;

import ink.ptms.cronus.builder.task.TaskEntry;
import ink.ptms.cronus.builder.task.data.Action;
import ink.ptms.cronus.builder.task.data.Count;
import ink.ptms.cronus.builder.task.data.Item;
import ink.ptms.cronus.builder.task.data.Location;
import ink.ptms.cronus.internal.QuestTask;
import ink.ptms.cronus.internal.task.item.TaskItemUse;
import ink.ptms.cronus.uranus.annotations.Auto;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * @Author 坏黑
 * @Since 2019-06-22 22:19
 */
@Auto
public class ItemUse extends TaskEntry {

    public ItemUse() {
        objective.add(Location.class);
        objective.add(Count.class);
        objective.add(Item.class);
        objective.add(Action.class);
    }

    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Materials.STICK.parseMaterial()).name("§f物品使用").lore("", "§7点击选择").flags(ItemFlag.values()).build();
    }

    @Override
    public Class<? extends QuestTask> getTask() {
        return TaskItemUse.class;
    }
}
