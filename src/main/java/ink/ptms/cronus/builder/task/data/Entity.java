package ink.ptms.cronus.builder.task.data;

import com.google.common.collect.Lists;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.element.BuilderQuest;
import ink.ptms.cronus.builder.element.BuilderTaskData;
import ink.ptms.cronus.builder.task.TaskData;
import ink.ptms.cronus.service.selector.EntitySelector;
import ink.ptms.cronus.util.Utils;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.lite.Catchers;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @Author 坏黑
 * @Since 2019-07-01 13:01
 */
public class Entity extends TaskData {

    public Entity(Player player, BuilderTaskData builderTaskData) {
        super(player, builderTaskData);
    }

    @Override
    public String getKey() {
        return "entity";
    }

    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Material.EGG)
                .name("§7目标实体")
                .lore(
                        "",
                        "§f" + (data == null ? "无" : Cronus.getCronusService().getService(EntitySelector.class).getSelectDisplay(String.valueOf(data), player))
                ).build();
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Catchers.call(player, new Entity.EntitySelect(player, data, this::saveData, this::open));
    }

    @Override
    public Object defaultValue() {
        return null;
    }

    public static class EntitySelect implements Catchers.Catcher {

        private final Player player;
        private final Object origin;
        private final BuilderQuest.EditTask editTask;
        private final Runnable closeTask;

        public EntitySelect(Player player, Object origin, BuilderQuest.EditTask editTask, Runnable closeTask) {
            this.player = player;
            this.origin = origin;
            this.editTask = editTask;
            this.closeTask = closeTask;
        }

        @Override
        public Catchers.Catcher before() {
            player.closeInventory();
            TellrawJson.create().append("§7§l[§f§lCronus§7§l] §7请用")
                    .append("§f下界之星").hoverText("§7点击获取").clickCommand("/give " + player.getName() + " nether_star")
                    .append("§7点击目标实体或输入匹配规则. ")
                    .append("§a(完成)").hoverText("§7点击").clickCommand("quit()")
                    .send(player);
            TellrawJson.create().append("§7§l[§f§lCronus§7§l] §7当前: ")
                    .append("§f" + Utils.NonNull(origin)).hoverText("§7点击").clickSuggest(Utils.NonNull(origin))
                    .send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7可用：")
                    .send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7- ")
                    .append(format("type=[type]"))
                    .hoverText(String.join("\n", Lists.newArrayList(
                            "§7生物类型",
                            "§7根据生物名称判断目标",
                            "",
                            "§7示范:",
                            "§ftype=zombie §8(所有类型为 \"zombie\" 的实体均可作为目标)",
                            "",
                            "§c生物类型和名称可同时存在"
                    ))).clickSuggest("type=[type]").send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7- ")
                    .append(format("name=[name]"))
                    .hoverText(String.join("\n", Lists.newArrayList(
                            "§7生物名称",
                            "§7根据生物名称判断目标",
                            "",
                            "§7示范:",
                            "§fname=僵尸 §8(所有名字中含有 \"僵尸\" 的实体均可作为目标)",
                            "",
                            "§c生物类型和名称可同时存在"
                    ))).clickSuggest("name=nbv[name]").send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7- ")
                    .append(format("citizens=[id]"))
                    .hoverText(String.join("\n", Lists.newArrayList(
                            "§7Citizens 扩展支持",
                            "§7根据 Citizens 序号判断目标",
                            "",
                            "§7别名:",
                            "§fnpc=[id]",
                            "",
                            "§7示范:",
                            "§fnpc=0 §8(序号为 \"0\" 的 Citizens 实体作为目标)"
                    ))).clickSuggest("citizens=[id]").send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7- ")
                    .append(format("mythicmob=[id]"))
                    .hoverText(String.join("\n", Lists.newArrayList(
                            "§7Mythicmobs 扩展支持",
                            "§7根据 Mythicmobs 序号判断目标",
                            "",
                            "§7别名:",
                            "§fmm=[id]",
                            "",
                            "§7示范:",
                            "§fmm=mob_0 §8(序号为 \"mob_0\" 的 Mythicmobs 实体作为目标)"
                    ))).clickSuggest("mythicmob=[id]").send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7- ")
                    .append(format("shopkeeper=[uuid]"))
                    .hoverText(String.join("\n", Lists.newArrayList(
                            "§7Shopkeepers 扩展支持",
                            "§7根据 Shopkeepers 序号判断目标",
                            "",
                            "§7别名:",
                            "§fshop=[uuid]",
                            "",
                            "§7示范:",
                            "§fshop=ea23 ... §8(序号为 \"ea23 ...\" 的 Shopkeepers 实体作为目标)"
                    ))).clickSuggest("shopkeeper=[uuid]").send(player);
            TellrawJson.create()
                    .append("§7§l[§f§lCronus§7§l] §7- ")
                    .append(format("anpc=[id]"))
                    .hoverText(String.join("\n", Lists.newArrayList(
                            "§7Adyeshach 扩展支持",
                            "§7根据 Adyeshach 序号判断目标",
                            "",
                            "§7别名:",
                            "§fady=[id]",
                            "",
                            "§7示范:",
                            "§fanpc=ady_1 ... §8(序号为 \"ady_1\" 的 Adyeshach 实体作为目标)"
                    ))).clickSuggest("anpc=[id]").send(player);
            return this;
        }

        @Override
        public boolean after(String s) {
            editTask.run(s);
            closeTask.run();
            return false;
        }

        @Override
        public void cancel() {
            closeTask.run();
        }

        public Player getPlayer() {
            return player;
        }

        public BuilderQuest.EditTask getEditTask() {
            return editTask;
        }

        public Runnable getCloseTask() {
            return closeTask;
        }

        public String format(String example) {
            return "§8" + example.replaceAll("[.:]", "§f$0§8").replaceAll("\\[(.+?)]", "§8[§7$1§8]§8");
        }
    }
}
