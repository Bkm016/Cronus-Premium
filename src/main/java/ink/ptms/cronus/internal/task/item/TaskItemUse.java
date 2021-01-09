package ink.ptms.cronus.internal.task.item;

import ink.ptms.cronus.database.data.DataQuest;
import ink.ptms.cronus.internal.bukkit.ItemStack;
import ink.ptms.cronus.internal.bukkit.Location;
import ink.ptms.cronus.internal.bukkit.parser.BukkitParser;
import ink.ptms.cronus.internal.task.Task;
import ink.ptms.cronus.internal.task.special.Countable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-05-28 17:21
 */
@Task(name = "item_use")
public class TaskItemUse extends Countable<PlayerInteractEvent> {

    private ItemStack item;
    private Location location;
    private Action action;

    public TaskItemUse(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void init(Map<String, Object> data) {
        super.init(data);
        item = data.containsKey("item") ? BukkitParser.toItemStack(data.get("item")) : null;
        location = data.containsKey("location") ? BukkitParser.toLocation(data.get("location")) : null;
        action = data.containsKey("action") ? Action.parse(data.get("action")) : null;
    }

    @Override
    public boolean check(Player player, DataQuest dataQuest, PlayerInteractEvent e) {
        return (action == null || action.isSelect(e.getAction())) && (item == null || item.isItem(e.getItem())) && (location == null || location.inSelect(e.getClickedBlock().getLocation()));
    }

    @Override
    public String toString() {
        return "TaskItemUse{" +
                "item=" + item +
                ", location=" + location +
                ", action=" + action +
                ", count=" + count +
                ", entitySelector=" + entitySelector +
                ", id='" + id + '\'' +
                ", config=" + config +
                ", condition=" + condition +
                ", conditionRestart=" + conditionRestart +
                ", guide=" + guide +
                ", status='" + status + '\'' +
                ", statusInput=" + statusInput +
                ", action=" + action +
                '}';
    }

    enum Action {

        LEFT, RIGHT;

        public boolean isSelect(org.bukkit.event.block.Action a) {
            return this == LEFT ? (a == org.bukkit.event.block.Action.LEFT_CLICK_AIR || a == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) : (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK);
        }

        public static Action parse(Object in) {
            try {
                return valueOf(String.valueOf(in).toUpperCase());
            } catch (Throwable ignored) {
            }
            return null;
        }
    }
}
