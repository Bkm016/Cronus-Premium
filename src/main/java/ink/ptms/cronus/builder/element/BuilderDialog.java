package ink.ptms.cronus.builder.element;

import com.google.common.collect.Lists;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.builder.Builders;
import ink.ptms.cronus.builder.element.condition.MatchEntry;
import ink.ptms.cronus.builder.element.dialog.Dialog;
import ink.ptms.cronus.builder.task.data.Entity;
import ink.ptms.cronus.internal.api.Helper;
import ink.ptms.cronus.service.selector.EntitySelector;
import io.izzel.taboolib.util.Files;
import io.izzel.taboolib.util.item.ItemBuilder;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.lite.Catchers;
import io.izzel.taboolib.util.lite.Materials;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * @Author 坏黑
 * @Since 2019-06-24 14:21
 */
public class BuilderDialog extends BuilderQuest {

    private String title = "对话";
    private String target;
    private MatchEntry condition;
    private List<String> actionOpen = Lists.newArrayList();
    private List<String> actionClose = Lists.newArrayList();
    private Dialog dialog;

    public BuilderDialog(String id) {
        super(id);
    }

    public BuilderDialog(File file) {
        super("");
        YamlConfiguration yaml = Files.loadYaml(file);
        for (String id : yaml.getKeys(false)) {
            import0(yaml.getConfigurationSection(id));
            return;
        }
    }

    public void import0(ConfigurationSection section) {
        id = section.getName();
        title = section.getString("title", "对话");
        target = section.getString("target");
        if (section.contains("open")) {
            actionOpen = section.getStringList("open");
        }
        if (section.contains("close")) {
            actionClose = section.getStringList("close");
        }
        if (section.contains("condition")) {
            condition = new MatchEntry(section.get("condition"));
        }
        if (section.contains("dialog")) {
            dialog = new Dialog(false, null);
            dialog.import0(section.getConfigurationSection("dialog").getValues(false));
        }
    }

    @Override
    public void export() {
        File file = Files.file(new File(Cronus.getCronusService().getService(ink.ptms.cronus.service.dialog.Dialog.class).getFolder(), "builder/" + id + ".yml"));
        YamlConfiguration yaml = Files.loadYaml(file);
        yaml.set(id + ".title", title);
        yaml.set(id + ".target", target);
        yaml.set(id + ".dialog", dialog.export0());
        if (!actionOpen.isEmpty()) {
            yaml.set(id + ".open", actionOpen);
        }
        if (!actionClose.isEmpty()) {
            yaml.set(id + ".close", actionClose);
        }
        if (condition != null) {
            yaml.set(id + ".condition", condition.toObject());
        }
        try {
            yaml.save(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void open(Player player) {
        if (dialog == null) {
            dialog = new Dialog(false, null);
        }
        Inventory inventory = Builders.normal("对话构建 : " + id,
                e -> {
                    if (e.getClickType() == ClickType.CLICK) {
                        e.castClick().setCancelled(true);
                        switch (e.castClick().getRawSlot()) {
                            case 10:
                                editString(e.getClicker(), "对话标题", title, r -> title = r);
                                break;
                            case 11:
                                if (e.castClick().isLeftClick()) {
                                    Catchers.call(e.getClicker(), new Entity.EntitySelect(player, target, r -> target = r, () -> open(player)));
                                } else {
                                    target = null;
                                    open(player);
                                }
                                break;
                            case 12:
                                BuilderCondition condition = new BuilderCondition(this.condition, e.getClicker(), "特殊条件");
                                condition.setCloseTask(c -> {
                                    this.condition = condition.getEntry();
                                    Bukkit.getScheduler().runTaskLater(Cronus.getInst(), () -> open(e.getClicker()), 1);
                                });
                                condition.open(player, 0);
                                break;
                            case 13:
                                new BuilderListEffect("对话开始动作", actionOpen).open(e.getClicker(), 0, c -> open(e.getClicker()), this::getEffect);
                                break;
                            case 14:
                                new BuilderListEffect("对话结束动作", actionClose).open(e.getClicker(), 0, c -> open(e.getClicker()), this::getEffect);
                                break;
                            case 15:
                                dialog.open(player, c -> open(player));
                                break;
                            case 49:
                                player.closeInventory();
                                Helper.normal(player, "正在导出...");
                                try {
                                    export();
                                    Helper.normal(player, "导出完成!");
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    Helper.error(player, "导出失败: " + t.getMessage());
                                }
                                break;
                        }
                    }
                },
                e -> {

                });
        inventory.setItem(10, new ItemBuilder(Material.NAME_TAG)
                .name("§b对话标题")
                .lore("", "§f" + title)
                .build());
        inventory.setItem(11, new ItemBuilder(Material.NAME_TAG)
                .name("§b对话目标")
                .lore("", "§f" + (target == null ? "无" : getTargetDisplay(player)), "§8§m                  ", "§7选择: §8左键", "§7删除: §8左键")
                .build());
        inventory.setItem(12, new ItemBuilder(Objects.requireNonNull(Materials.TRIPWIRE_HOOK.parseMaterial()))
                .name("§b对话条件")
                .lore(toLore(condition == null ? Lists.newArrayList() : condition.asList(0)))
                .build());
        inventory.setItem(13, new ItemBuilder(Objects.requireNonNull(Materials.REPEATER.parseMaterial()))
                .name("§b对话开始动作")
                .lore(toLore(actionOpen))
                .build());
        inventory.setItem(14, new ItemBuilder(Objects.requireNonNull(Materials.REPEATER.parseMaterial()))
                .name("§b对话结束动作")
                .lore(toLore(actionClose))
                .build());
        inventory.setItem(15, new ItemBuilder(Objects.requireNonNull(Materials.BOOK.parseMaterial()))
                .name("§b对话结构")
                .lore("", dialog == null ? "§f无" : "§f...")
                .build());
        inventory.setItem(49, new ItemBuilder(Objects.requireNonNull(Materials.WRITABLE_BOOK.parseMaterial()))
                .name("§a保存配置")
                .lore("", "§7文件位置", "§8§nplugins/Cronus/dialog/builder/" + id + ".yml")
                .build());
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    protected String getTargetDisplay(Player player) {
        return Cronus.getCronusService().getService(EntitySelector.class).getSelectDisplay(target, player);
    }
}
