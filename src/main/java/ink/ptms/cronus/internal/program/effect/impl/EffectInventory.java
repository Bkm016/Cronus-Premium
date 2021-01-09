package ink.ptms.cronus.internal.program.effect.impl;

import ink.ptms.cronus.internal.bukkit.ItemStack;
import ink.ptms.cronus.internal.bukkit.parser.BukkitParser;
import ink.ptms.cronus.internal.program.QuestProgram;
import ink.ptms.cronus.uranus.annotations.Auto;
import ink.ptms.cronus.uranus.program.Program;
import ink.ptms.cronus.uranus.program.effect.Effect;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.locale.logger.TLogger;
import io.izzel.taboolib.util.item.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.regex.Matcher;

/**
 * @Author 坏黑
 * @Since 2019-05-11 17:06
 */
@Auto
public class EffectInventory extends Effect {

    @TInject
    private static TLogger logger;
    private String action;
    private String symbol;
    private String value;
    private ItemStack valueItem;

    @Override
    public String pattern() {
        return "inventory\\.(?<action>\\S+)( (?<symbol>\\S+) (?<value>.+))?";
    }

    @Override
    public String getExample() {
        return "inventory.[action] <symbol> <value>";
    }

    @Override
    public void match(Matcher matcher) {
        action = String.valueOf(matcher.group("action")).toLowerCase();
        symbol = String.valueOf(matcher.group("symbol")).toLowerCase();
        value = matcher.group("value");
        try {
            valueItem = BukkitParser.toItemStack(value);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void eval(Program program) {
        if (program instanceof QuestProgram) {
            Player player = ((QuestProgram) program).getPlayer();
            switch (action) {
                case "close":
                    player.closeInventory();
                    break;
                case "hand":
                case "main":
                case "mainhand":
                    player.setItemInHand(evalItem(player.getItemInHand()));
                    break;
                case "off":
                case "offhand":
                    player.getInventory().setItemInOffHand(evalItem(player.getInventory().getItemInOffHand()));
                    break;
                case "head":
                case "helmet":
                    player.getInventory().setHelmet(evalItem(player.getInventory().getHelmet()));
                    break;
                case "chest":
                case "chestplate":
                    player.getInventory().setChestplate(evalItem(player.getInventory().getChestplate()));
                    break;
                case "leg":
                case "legs":
                case "leggings":
                    player.getInventory().setLeggings(evalItem(player.getInventory().getLeggings()));
                    break;
                case "feet":
                case "boot":
                case "boots":
                    player.getInventory().setBoots(evalItem(player.getInventory().getBoots()));
                    break;
                default:
                    logger.warn("Invalid Action: " + action + " " + symbol + " " + value);
                    break;
            }
        }
    }

    public org.bukkit.inventory.ItemStack evalItem(org.bukkit.inventory.ItemStack item) {
        // 物品赋予
        if (symbol.startsWith("=")) {
            return valueItem.getBukkitItem();
        }
        // 物品不存在
        else if (Items.isNull(item)) {
            return item;
        }
        // 数量增加
        else if (symbol.startsWith("+")) {
            item.setAmount(item.getAmount() + NumberConversions.toInt(value));
            return item.getAmount() < 0 ? new org.bukkit.inventory.ItemStack(Material.AIR) : item;
        }
        // 数量减少
        else if (symbol.startsWith("-")) {
            item.setAmount(item.getAmount() - NumberConversions.toInt(value));
            return item.getAmount() < 0 ? new org.bukkit.inventory.ItemStack(Material.AIR) : item;
        }
        // 无效符号
        else {
            logger.warn("Invalid Symbol: " + symbol);
            return item;
        }
    }

    @Override
    public String toString() {
        return "EffectInventory{" +
                "action='" + action + '\'' +
                ", symbol='" + symbol + '\'' +
                ", value='" + value + '\'' +
                ", valueItem=" + valueItem +
                '}';
    }
}
