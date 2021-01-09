package ink.ptms.cronus.internal.task.player.total;

import ink.ptms.cronus.database.data.DataQuest;
import ink.ptms.cronus.event.CronusBroadcastEvent;
import ink.ptms.cronus.internal.task.Task;
import ink.ptms.cronus.internal.task.special.Uncountable;
import ink.ptms.cronus.util.Sxpression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-05-28 17:21
 */
@Task(name = "broadcast")
public class TaskBroadcast extends Uncountable<CronusBroadcastEvent> {

    private String content;

    public TaskBroadcast(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void init(Map<String, Object> data) {
        total = new Sxpression(data.getOrDefault("total", ">0"));
        content = data.containsKey("content") ? data.get("content").toString() : null;
    }

    @Override
    public boolean check(Player player, DataQuest dataQuest, CronusBroadcastEvent e) {
        return (content == null || content.equals(e.getContent()));
    }

    @Override
    public double getCount(Player player, DataQuest dataQuest, CronusBroadcastEvent e) {
        return 1;
    }

    @Override
    public String toString() {
        return "TaskBroadcast{" +
                "content='" + content + '\'' +
                ", total=" + total +
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
}
