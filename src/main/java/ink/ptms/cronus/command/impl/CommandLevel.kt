package ink.ptms.cronus.command.impl

import ink.ptms.cronus.Cronus
import ink.ptms.cronus.command.CronusCommand
import ink.ptms.cronus.service.custom.CustomLevel
import io.izzel.taboolib.module.command.base.Argument
import io.izzel.taboolib.module.command.base.BaseCommand
import io.izzel.taboolib.module.command.base.BaseSubCommand
import io.izzel.taboolib.module.command.base.SubCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.NumberConversions

/**
 * @Author 坏黑
 * @Since 2019-07-26 18:16
 */
@BaseCommand(name = "CronusLevel", aliases = ["cLevel", "cl"], permission = "*")
class CommandLevel : CronusCommand() {

    @SubCommand
    var addExp: BaseSubCommand = object : BaseSubCommand() {
        override fun getDescription(): String {
            return "赋予经验"
        }

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("键") { Cronus.getCronusService().getService(CustomLevel::class.java).registeredLevel.keys.toList() }, Argument("值"))
        }

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            Cronus.getCronusService().getService(CustomLevel::class.java).addExp(player, args[1], NumberConversions.toInt(args[2]))
        }
    }

    @SubCommand
    var setExp: BaseSubCommand = object : BaseSubCommand() {
        override fun getDescription(): String {
            return "修改经验"
        }

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("键") { Cronus.getCronusService().getService(CustomLevel::class.java).registeredLevel.keys.toList() }, Argument("值"))
        }

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            Cronus.getCronusService().getService(CustomLevel::class.java).setExp(player, args[1], NumberConversions.toInt(args[2]))
        }
    }

    @SubCommand
    var addLevel: BaseSubCommand = object : BaseSubCommand() {
        override fun getDescription(): String {
            return "赋予等级"
        }

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("键") { Cronus.getCronusService().getService(CustomLevel::class.java).registeredLevel.keys.toList() }, Argument("值"))
        }

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            Cronus.getCronusService().getService(CustomLevel::class.java).addLevel(player, args[1], NumberConversions.toInt(args[2]))
        }
    }

    @SubCommand
    var setLevel: BaseSubCommand = object : BaseSubCommand() {
        override fun getDescription(): String {
            return "修改经验"
        }

        override fun getArguments(): Array<Argument> {
            return arrayOf(Argument("玩家"), Argument("键") { Cronus.getCronusService().getService(CustomLevel::class.java).registeredLevel.keys.toList() }, Argument("值"))
        }

        override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>) {
            val player = Bukkit.getPlayerExact(args[0])
            if (player == null) {
                error(sender, "玩家 &7" + args[0] + " &c离线.")
                return
            }
            Cronus.getCronusService().getService(CustomLevel::class.java).setLevel(player, args[1], NumberConversions.toInt(args[2]))
        }
    }
}