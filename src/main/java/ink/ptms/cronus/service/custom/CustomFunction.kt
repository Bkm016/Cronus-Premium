package ink.ptms.cronus.service.custom

import ink.ptms.cronus.Cronus
import ink.ptms.cronus.internal.program.NoneProgram
import ink.ptms.cronus.internal.program.effect.EffectParser
import ink.ptms.cronus.service.Service
import ink.ptms.cronus.uranus.annotations.Auto
import ink.ptms.cronus.uranus.program.effect.Effect
import org.bukkit.entity.Player

/**
 * @Author sky
 * @Since 2020-07-11 9:14
 */
@Auto
class CustomFunction : Service {

    val functions = HashMap<String, List<Effect>>()

    override fun init() {
        functions.clear()
        functions.putAll(Cronus.getConf().getConfigurationSection("CustomFunction")?.getKeys(false)?.map {
            it to Cronus.getConf().getStringList("CustomFunction.$it").map { effect -> EffectParser.parse(effect) }
        }?.toMap() ?: emptyMap())
    }

    fun get(id: String, player: Player, vararg args: Any): Any? {
        val function = functions[id] ?: return null
        val program = NoneProgram(player)
        args.forEachIndexed { index, any -> program.data.set("args.$index", any) }
        program.eval(function)
        return program.result
    }
}