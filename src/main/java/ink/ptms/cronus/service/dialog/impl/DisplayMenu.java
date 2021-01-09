package ink.ptms.cronus.service.dialog.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.internal.program.NoneProgram;
import ink.ptms.cronus.service.dialog.DialogPack;
import ink.ptms.cronus.service.dialog.api.DisplayBase;
import ink.ptms.cronus.service.dialog.api.Reply;
import ink.ptms.cronus.service.dialog.api.ReplyMap;
import ink.ptms.cronus.uranus.function.FunctionParser;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.item.inventory.ClickType;
import io.izzel.taboolib.util.item.inventory.MenuBuilder;
import io.izzel.taboolib.util.lite.Materials;
import io.izzel.taboolib.util.lite.Scripts;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.NumberConversions;

import javax.script.CompiledScript;
import javax.script.SimpleBindings;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author 坏黑
 * @Since 2019-08-03 17:20
 */
public class DisplayMenu extends DisplayBase {

    private final CompiledScript rowsScript;
    private final Integer slotMessage;
    private final Integer[] slotReply;

    public DisplayMenu() {
        rowsScript = Scripts.compile(Cronus.getConf().getString("Settings.dialog-chest.rows-script"));
        slotMessage = Cronus.getConf().getInt("Settings.dialog-chest.slot-message");
        slotReply = Cronus.getConf().getIntegerList("Settings.dialog-chest.slot-reply").toArray(new Integer[0]);
    }

    @Override
    public String getName() {
        return "CRONUS_MENU";
    }

    @Override
    public void open(Player player, DialogPack dialogPack) {
    }

    @Override
    public void preReply(Player player, DialogPack replyPack, String id, int index) {
    }

    @Override
    public void preEffect(Player player, Reply reply) {
        player.closeInventory();
    }

    @Override
    public void postReply(Player player, DialogPack dialogPack, ReplyMap replyMap, int index) {
        // 界面物品缓存
        Map<Integer, Reply> slots = Maps.newHashMap();
        // 创建界面
        Inventory inventory = MenuBuilder.builder(Cronus.getInst())
                .title(dialogPack.getParent().getTitle())
                .rows(toRows(player, dialogPack))
                .event(e -> {
                    if (e.getClickType() == ClickType.CLICK) {
                        e.castClick().setCancelled(true);
                        // 执行对话回复
                        if (slots.containsKey(e.castClick().getRawSlot())) {
                            eval(player, slots.get(e.castClick().getRawSlot()));
                        }
                    }
                })
                .close(e -> close(player))
                .items()
                .build();
        // 对话物品
        inventory.setItem(slotMessage, toItem(player, Materials.matchMaterials(dialogPack.getConfig().getOrDefault("item", "BOOK").toString()).orElse(Materials.STONE), dialogPack.getText()));
        // 回复物品
        for (int i = 0; i < slotReply.length && i < replyMap.getReply().size(); i++) {
            DialogPack reply = replyMap.getReply().get(i).getDialogPack();
            inventory.setItem(slotReply[i], toItem(player, Materials.matchMaterials(reply.getConfig().getOrDefault("item", reply.isQuest() ? "MAP" : "PAPER").toString()).orElse(Materials.STONE), reply.getText()));
            slots.put(slotReply[i], replyMap.getReply().get(i));
        }
        player.openInventory(inventory);
    }

    public int toRows(Player player, DialogPack dialogPack) {
        try {
            return NumberConversions.toInt(rowsScript.eval(new SimpleBindings(ImmutableMap.of("player", player, "plugin", Cronus.getInst(), "$size", dialogPack.getReply().size()))));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

    public ItemStack toItem(Player player, Materials material, List<String> list) {
        NoneProgram program = new NoneProgram(player);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, FunctionParser.parse(program, list.get(i)).toString());
        }
        ItemStack itemStack = material.parseItem();
        if (Items.isNull(itemStack)) {
            itemStack = new ItemStack(Material.STONE);
        }
        if (list.size() > 0) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(list.get(0));
            List<String> lore = Lists.newArrayList(list);
            lore.remove(0);
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @Override
    public String toString() {
        return "DisplayMenu{" +
                "rowsScript=" + rowsScript +
                ", slotMessage=" + slotMessage +
                ", slotReply=" + Arrays.toString(slotReply) +
                '}';
    }
}
