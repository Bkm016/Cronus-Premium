package ink.ptms.cronus.builder.editor.module.impl;

import ink.ptms.cronus.builder.editor.EditorAPI;
import ink.ptms.cronus.builder.editor.data.PlayerData;
import ink.ptms.cronus.builder.editor.data.PlayerDataHandler;
import ink.ptms.cronus.builder.editor.module.IModule;
import ink.ptms.cronus.builder.editor.module.action.*;
import ink.ptms.cronus.builder.editor.module.tag.TagParser;
import ink.ptms.cronus.builder.editor.module.tag.TagResult;
import io.izzel.taboolib.kotlin.Indexed;
import io.izzel.taboolib.module.tellraw.TellrawJson;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-03-12 0:38
 */
public class ContentEdit extends IModule {

    public void eval(Player sender) {
        eval(sender, "");
    }

    @Override
    public String[] getCommand() {
        return array("edit", "e");
    }

    @Override
    public void eval(Player sender, String body) {
        // 开启编辑模式
        EditorAPI.setEditMode(sender, true);
        // 获取内容
        TagResult result = TagParser.parser(body);
        PlayerData playerData = PlayerDataHandler.getPlayerData(sender);
        // 初始化
        if (playerData.getLines() == null) {
            sender.sendMessage("§c§l[§4§lCronus§c§l] §c未初始化.");
            return;
        }
        // 关闭
        if (result.getTags().contains("q")) {
            if (playerData.getCloseTask() != null) {
                playerData.getCloseTask().run();
            }
            // 关闭编辑模式
            EditorAPI.setEditMode(sender, false);
        }
        // 保存
        if (result.getTags().contains("s")) {
            if (playerData.isSaved()) {
                return;
            }
            if (playerData.getSaveTask() != null) {
                playerData.getSaveTask().run();
            }
            playerData.setSaved(true);
            eval(sender, "");
            return;
        }
        // 修改
        if (result.getTags().contains("e")) {
            if (!result.getAttributes().containsKey("l")) {
                sender.sendMessage("§c[PurtmarsEditor] §7无效的参数.");
                return;
            }
            int line = NumberConversions.toInt(result.getAttributes().get("l"));
            if (result.getResult().isEmpty() || result.getTags().contains("d")) {
                playerData.getActionEdit().add(new ActionEditDelete(line, playerData.getLines().get(line)));
                playerData.getLines().remove(line);
                playerData.setSaved(false);
                eval(sender, "");
                return;
            }
            while (line >= playerData.getLines().size()) {
                playerData.getActionEdit().add(new ActionEditAppend(playerData.getLines().size(), ""));
                playerData.getLines().add("");
            }
            playerData.getActionEdit().add(new ActionEditSet(line, playerData.getLines().get(line)));
            playerData.getLines().set(line, result.getResult());
            playerData.setSaved(false);
            eval(sender, "");
            return;
        }
        // 插入
        if (result.getTags().contains("i")) {
            if (!result.getAttributes().containsKey("l")) {
                sender.sendMessage("§c[PurtmarsEditor] §7无效的参数.");
                return;
            }
            int line = NumberConversions.toInt(result.getAttributes().get("l"));
            if (line >= playerData.getLines().size()) {
                sender.sendMessage("§c[PurtmarsEditor] §7无效的行数.");
                return;
            }
            playerData.getActionEdit().add(new ActionEditInsert(line, result.getResult()));
            playerData.getLines().add(line, result.getResult());
            playerData.setSaved(false);
            eval(sender, "");
            return;
        }
        // 撤销
        if (result.getTags().contains("u")) {
            if (playerData.getActionEdit() == null || playerData.getActionEdit().isEmpty()) {
                return;
            }
            IActionEdit lastAction = playerData.getActionEdit().remove(playerData.getActionEdit().size() - 1);
            if (lastAction instanceof ActionEditAppend || lastAction instanceof ActionEditInsert) {
                playerData.getLines().remove(lastAction.getLine());
            } else if (lastAction instanceof ActionEditDelete) {
                playerData.getLines().add(lastAction.getLine(), lastAction.getValue());
            } else if (lastAction instanceof ActionEditSet) {
                playerData.getLines().set(lastAction.getLine(), lastAction.getValue());
            }
            playerData.setSaved(false);
            eval(sender, String.valueOf(lastAction.getLine() / EditorAPI.getListLength()));
            return;
        }
        if (!result.getResult().isEmpty()) {
            playerData.setIndex(NumberConversions.toInt(result.getResult()));
        }
        List<String> list = Indexed.INSTANCE.subList(
                playerData.getLines(),
                playerData.getIndex() * EditorAPI.getListLength(),
                (playerData.getIndex() + 1) * EditorAPI.getListLength() - 1
        );
        sender.sendMessage(TITLE[0]);
        if (playerData.getIndex() > 0) {
            TellrawJson.create()
                    .append(" ")
                    .append("§7§l⇑⇑⇑").hoverText("§f上一页").clickCommand(":edit " + (playerData.getIndex() - 1))
                    .append(" §f| ")
                    .append("§8§l⇑⇑⇑").hoverText("§f首页").clickCommand(":edit 0")
                    .send(sender);
        } else {
            TellrawJson.create()
                    .append(" ")
                    .append("§7§l⇖⇖⇖").hoverText("§f关闭\n\n§7未保存的内容将会丢失!").clickCommand(":edit /q")
                    .send(sender);
        }
        for (int i = 0; i < list.size(); i++) {
            int index = (playerData.getIndex() * EditorAPI.getListLength() + i);
            String line = list.get(i).replace("§", "&");
            TellrawJson.create()
                    .append("  ")
                    .append(line.isEmpty() ? "§8 " : (playerData.getActionEdit().stream().anyMatch(action -> action.getLine() == index) ? "§a" : "§7") + line).hoverText(line.isEmpty() ? "§8添加内容" : "§7编辑内容").clickSuggest(":edit /e /l:" + index + " " + (line.isEmpty() ? "" : line.replace("/", "//")))
                    .append(" ")
                    .append("§8↵").hoverText("§8插入内容").clickSuggest(":edit /i /l:" + index + " ")
                    .send(sender);
        }
        for (int i = 0; i < EditorAPI.getListLength() - list.size(); i++) {
            int index = (playerData.getIndex() * EditorAPI.getListLength() + i + list.size());
            TellrawJson.create()
                    .append("  ")
                    .append("§8+").hoverText("§8添加内容").clickSuggest(":edit /e /l:" + index + " ")
                    .send(sender);
        }
        if (playerData.getLines().size() / EditorAPI.getListLength() > playerData.getIndex()) {
            TellrawJson.create()
                    .append(" ")
                    .append("§7§l⇓⇓⇓").hoverText("§f下一页").clickCommand(":edit " + (playerData.getIndex() + 1))
                    .append(" §f| ")
                    .append("§8§l⇓⇓⇓").hoverText("§f尾页").clickCommand(":edit " + (playerData.getLines().size() / EditorAPI.getListLength()))
                    .send(sender);
        } else {
            TellrawJson.create()
                    .append("  ")
                    .append("§a✍")
                    .append((playerData.isSaved() ? " §7" : " §f") + "[保存]").hoverText("§7保存修改").clickCommand(":edit /s")
                    .append((playerData.getActionEdit() == null || playerData.getActionEdit().isEmpty() ? " §7" : " §f") + "[撤销]").hoverText("§7撤销更改").clickCommand(":edit /u")
                    .send(sender);
        }
        sender.sendMessage(TITLE[1]);
    }
}
