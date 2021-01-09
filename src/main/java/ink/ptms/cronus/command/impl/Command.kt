package ink.ptms.cronus.command.impl

import com.google.common.collect.Lists
import ink.ptms.cronus.Cronus
import ink.ptms.cronus.CronusAPI
import ink.ptms.cronus.CronusMirror
import ink.ptms.cronus.command.CronusCommand
import ink.ptms.cronus.database.DataMigration
import ink.ptms.cronus.database.data.DataQuest
import ink.ptms.cronus.event.CronusVisibleToggleEvent
import ink.ptms.cronus.internal.program.Action
import ink.ptms.cronus.internal.program.QuestProgram
import ink.ptms.cronus.service.dialog.Dialog
import ink.ptms.cronus.util.Utils
import io.izzel.taboolib.module.command.base.*
import io.izzel.taboolib.module.tellraw.TellrawJson
import io.izzel.taboolib.util.book.BookFormatter
import io.izzel.taboolib.util.book.builder.PageBuilder
import io.izzel.taboolib.util.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author 坏黑
 * @Since 2019-05-29 17:25
 */
@BaseCommand(name = "Cronus", aliases = ["CronusQuest", "cQuest", "cq"], permission = "*")
open class Command : CronusCommand() {

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val quests: List<String>
        get() {
            val label = ArrayList<String>()
            for (quest in Cronus.getCronusService().registeredQuest.values) {
                label.add(quest.id)
                label.add(quest.label)
            }
            return label.filter { Objects.nonNull(it) }
        }

    @SubCommand
    var info: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"))
        }

        override fun getDescription(): String {
            return "查看玩家任务数据."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            normal(sender, "正在创建缓存...")
            val playerData = CronusAPI.getData(player)
            val bookBuilder = BookFormatter.writtenBook()
            Bukkit.getScheduler().runTaskAsynchronously(Cronus.getInst(), Runnable {
                bookBuilder.addPages(PageBuilder()
                        .add("").newLine()
                        .add("").newLine()
                        .add("      §lPlayer Database").newLine()
                        .add("").newLine()
                        .add("         玩家数据").newLine()
                        .build())
                bookBuilder.addPages(ComponentSerializer.parse(TellrawJson.create()
                        .append("").newLine()
                        .append(" §l基本数据").newLine()
                        .append("").newLine()
                        .append(" 全局变量 ").append("§7[...]").hoverText(Utils.NonNull(playerData.dataGlobal.build())).append(" §c[player.val]").newLine()
                        .append(" 临时变量 ").append("§7[...]").hoverText(Utils.NonNull(playerData.dataTemp.build())).append(" §c[player.var]").newLine()
                        .append("").newLine()
                        .append(" 完成任务 ").append("§7[...]").hoverText(Utils.NonNull(playerData.questCompleted.map { "${it.key} : ${dateFormat.format(it.value)}" }.joinToString("\n"))).newLine()
                        .append(" 隐藏任务 ").append("§7[...]").hoverText(Utils.NonNull(playerData.questHide.joinToString("\n"))).newLine()
                        .append("").newLine()
                        .append(" 物品冷却 ").append("§7[...]").hoverText(Utils.NonNull(playerData.itemCooldown.map { "${it.key} : ${dateFormat.format(it.value)}" }.joinToString("\n"))).newLine()
                        .toRawMessage()))
                var index = 1
                playerData.quest.forEach { k, v ->
                    bookBuilder.addPages(ComponentSerializer.parse(TellrawJson.create()
                            .append("").newLine()
                            .append(" §l任务数据 §r§7(${index++}/${playerData.quest.size})").newLine()
                            .append(" §8§n$k").newLine()
                            .append("").newLine()
                            .append(" 任务变量 ").append("§7[...]").hoverText(Utils.NonNull(v.dataQuest.build())).append(" §c[quest.val]").newLine()
                            .append(" 阶段变量 ").append("§7[...]").hoverText(Utils.NonNull(v.dataStage.build())).append(" §c[quest.var]").newLine()
                            .append("").newLine()
                            .append(" 当前阶段 §7${v.currentStage}").newLine()
                            .append("").newLine()
                            .append(" 开始时间 ").append("§7[...]").hoverText(dateFormat.format(v.timeStart)).newLine()
                            .append(" 结束时间 ").append("§7[...]").hoverText(if (playerData.isQuestCompleted(k)) dateFormat.format(playerData.questCompleted[k]) else "-").newLine()
                            .toRawMessage()))
                }
                Bukkit.getScheduler().runTask(Cronus.getInst(), Runnable {
                    BookFormatter.forceOpen(sender as Player, bookBuilder.build())
                })
            })
        }

        override fun getType(): CommandType {
            return CommandType.PLAYER
        }

        fun FileConfiguration.build(): String {
            return this.getValues(true).toList().filter { it.second !is ConfigurationSection }.joinToString("\n") {
                if (it.second is List<*>) {
                    "${it.first.colored()}§8: \n${(it.second as List<Any>).joinToString("\n") { element -> "§8- §f${element}" }}"
                } else {
                    "${it.first.colored()}§8: §f${it.second}"
                }
            }
        }
        
        fun String.colored(): String {
            val i = this.indexOf(".")
            return if (i == -1) {
                "§7§n$this"
            } else {
                "§7§n${this.substring(0, i)}.§7${this.substring(i).replace(".", "§8.§7")}"
            }
        }
    }

    @SubCommand
    var accept: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务") { Cronus.getCronusService().registeredQuest.keys.toList() })
        }

        override fun getDescription(): String {
            return "使玩家接受任务."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            when (CronusAPI.acceptQuest(player, args[1])) {
                CronusAPI.AcceptResult.ACCEPTED -> error(sender, "玩家 &7" + args[0] + " &c已接受该任务.")
                CronusAPI.AcceptResult.INVALID -> error(sender, "任务 &7" + args[1] + " &c无效.")
                CronusAPI.AcceptResult.INVALID_CONFIG -> error(sender, "任务 &7" + args[1] + " &c缺少必要配置.")
                else -> {
                }
            }
        }
    }

    @SubCommand
    var quit: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务/标签") { quests })
        }

        override fun getDescription(): String {
            return "使玩家放弃任务."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            when (CronusAPI.failureQuest(player, args[1])) {
                CronusAPI.FailureResult.NOT_ACCEPT -> error(sender, "玩家 &7" + args[0] + " &c未接受该任务.")
                CronusAPI.FailureResult.COMPLETED -> error(sender, "玩家 &7" + args[0] + " &c已完成该任务.")
                else -> {
                }
            }
        }
    }

    @SubCommand
    var stop: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务/标签") { quests })
        }

        override fun getDescription(): String {
            return "使玩家停止任务."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val dataPlayer = CronusAPI.getData(player)
            if (args[1].equals("all", ignoreCase = true)) {
                dataPlayer.stopQuest()
            } else {
                val dataQuest = dataPlayer.getQuest(args[1])
                if (dataQuest == null) {
                    error(sender, "玩家 &7" + args[0] + " &c未接受该任务.")
                    return
                }
                dataPlayer.stopQuest(dataQuest.quest)
            }
            dataPlayer.push()
        }
    }

    @SubCommand
    var complete: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务/标签") { quests })
        }

        override fun getDescription(): String {
            return "使玩家完成任务."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val dataPlayer = CronusAPI.getData(player)
            val dataQuest = dataPlayer.getQuest(args[1])
            if (dataQuest == null) {
                error(sender, "玩家 &7" + args[0] + " &c未接受该任务.")
                return
            }
            if (dataPlayer.isQuestCompleted(args[1])) {
                error(sender, "玩家 &7" + args[0] + " &c已完成该任务.")
                return
            }
            dataPlayer.completeQuest(dataQuest.quest)
            dataPlayer.push()
        }
    }

    @SubCommand
    var reset: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务") { Cronus.getCronusService().registeredQuest.keys.toList() })
        }

        override fun getDescription(): String {
            return "重置任务完成时间."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val dataPlayer = CronusAPI.getData(player)
            dataPlayer.questCompleted.remove(args[1])
            dataPlayer.push()
        }
    }

    @SubCommand
    var open: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务/标签") { quests })
        }

        override fun getDescription(): String {
            return "使玩家打开日志."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val dataQuest = CronusAPI.getData(player).getQuest(args[1])
            if (dataQuest == null) {
                error(sender, "玩家 &7" + args[0] + " &c未接受该任务.")
                return
            }
            dataQuest.open(player)
        }
    }

    @SubCommand
    var book: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("纵览") { Cronus.getCronusService().registeredQuestBook.keys.toList() })
        }

        override fun getDescription(): String {
            return "使玩家打开纵览."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val questBook = Cronus.getCronusService().registeredQuestBook[args[1]]
            if (questBook == null) {
                error(sender, "纵览 &7" + args[1] + " &c无效.")
                return
            }
            questBook.open(player)
        }
    }

    @SubCommand
    var visible: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务") { Cronus.getCronusService().registeredQuest.keys.toList() })
        }

        override fun getDescription(): String {
            return "使玩家切换任务可见状态."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val quest = Cronus.getCronusService().registeredQuest[args[1]]
            if (quest == null) {
                error(sender, "任务 &7" + args[1] + " &c无效.")
                return
            }
            val playerData = CronusAPI.getData(player)
            if (!playerData.quest.containsKey(args[1])) {
                error(sender, "玩家 &7" + args[0] + " &c未接受该任务.")
                return
            }
            if (playerData.questHide.contains(quest.id)) {
                playerData.questHide.remove(quest.id)
            } else {
                playerData.questHide.add(quest.id)
            }
            playerData.push()
            CronusVisibleToggleEvent.call(sender as Player)
        }
    }

    @SubCommand
    var action: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("任务") { Lists.newArrayList(Cronus.getCronusService().registeredQuest.keys) }, Argument("状态") { Action.v().map { it.name } })
        }

        override fun getDescription(): String {
            return "使玩家执行任务动作."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val quest = Cronus.getCronusService().registeredQuest[args[1]]
            if (quest == null) {
                error(sender, "任务 &7" + args[1] + " &c无效.")
                return
            }
            val action = Action.fromName(args[2])
            if (action == null) {
                error(sender, "状态 &7" + args[2] + " &c无效.")
                return
            }
            quest.eval(QuestProgram(player, CronusAPI.getData(player).quest.getOrDefault(quest.id, DataQuest(quest.id, quest.firstStage))), action)
        }
    }

    @SubCommand
    var dialog: BaseSubCommand = object : BaseSubCommand() {

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("对话") { Cronus.getCronusService().getService(Dialog::class.java).dialogs.map { it.id } })
        }

        override fun getDescription(): String {
            return "使玩家打开任务对话."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            val dialog = Cronus.getCronusService().getService(Dialog::class.java).getDialog(args[1])
            if (dialog == null) {
                error(sender, "对话 &7" + args[1] + " &c无效.")
                return
            }
            if (dialog.condition == null || dialog.condition.check(player)) {
                dialog.dialog.display(player)
            }
        }
    }

    @SubCommand
    var mirror: BaseSubCommand = object : BaseSubCommand() {

        override fun getDescription(): String {
            return "耗能监控."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            normal(sender, "正在创建统计...")
            val bookBuilder = BookFormatter.writtenBook()
            bookBuilder.addPages(PageBuilder()
                    .add("").newLine()
                    .add("").newLine()
                    .add("      §lCronus Mirror").newLine()
                    .add("").newLine()
                    .add("         性能监控").newLine()
                    .build())
            CronusMirror.getMirrors().keys.toList().sortedByDescending { CronusMirror.getMirror(it).timeTotal }.forEach { k ->
                val v = CronusMirror.getMirror(k)
                val type = k.substring(0, k.indexOf(":"))
                val name = k.substring(k.indexOf(":") + 1)
                bookBuilder.addPages(ComponentSerializer.parse(TellrawJson.create()
                        .append("  §1§l§n" + Utils.toSimple(type)).hoverText(type).newLine()
                        .append("  §1§l§n" + Utils.toSimple(name)).hoverText(name).newLine()
                        .append("").newLine()
                        .append("  执行 " + v.times + " 次").newLine()
                        .append("  平均 " + v.timeLatest + " 毫秒").newLine()
                        .append("  总计 " + v.timeTotal + " 毫秒").newLine()
                        .toRawMessage(sender as Player)))
            }
            normal(sender, "创建完成!")
            BookFormatter.forceOpen(sender as Player, bookBuilder.build())
        }

        override fun getType(): CommandType {
            return CommandType.PLAYER
        }
    }

    @SubCommand
    var reload: BaseSubCommand = object : BaseSubCommand() {

        override fun getDescription(): String {
            return "重载任务."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            Cronus.getConf().reload()
            Cronus.reloadQuest()
            normal(sender, "重载完成.")
        }
    }

    @SubCommand
    var migration: BaseSubCommand = object : BaseSubCommand() {

        override fun getType(): CommandType {
            return CommandType.CONSOLE
        }

        override fun hideInHelp(): Boolean {
            return true
        }

        override fun getDescription(): String {
            return "迁移数据."
        }

        override fun onCommand(sender: CommandSender, command: org.bukkit.command.Command, s: String, args: Array<String>) {
            DataMigration.toMongoDB()
        }
    }
}
