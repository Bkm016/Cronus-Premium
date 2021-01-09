package ink.ptms.cronus.uranus.function.impl

import com.google.common.collect.Maps
import ink.ptms.cronus.Cronus
import ink.ptms.cronus.internal.program.effect.impl.script.EffectScriptImport
import ink.ptms.cronus.uranus.Uranus
import ink.ptms.cronus.uranus.annotations.Auto
import ink.ptms.cronus.uranus.event.UranusScriptEvalEvent
import ink.ptms.cronus.uranus.program.Program
import io.izzel.taboolib.module.locale.logger.TLoggerManager
import io.izzel.taboolib.util.lite.Scripts
import javax.script.CompiledScript
import javax.script.ScriptException
import javax.script.SimpleBindings

/**
 * @Author 坏黑
 * @Since 2019-05-11 13:16
 */
@Auto
class FunctionScript : ink.ptms.cronus.uranus.function.Function() {

    val scripts: MutableMap<String, CompiledScript> = Maps.newHashMap()

    override fun allowArguments(): Boolean {
        return false
    }

    override fun getName(): String {
        return "script"
    }

    override fun eval(program: Program, vararg args: String): Any {
        try {
            UranusScriptEvalEvent(program, SimpleBindings(mapOf(
                    "sender" to program.sender,
                    "player" to program.sender,
                    "server" to Uranus.getInst().server,
                    "bukkit" to Uranus.getInst().server,
                    "plugin" to Uranus.getInst(),
                    "program" to program,
                    *program.scriptDefinedMap.map { it.key to it.value }.toTypedArray(),
                    *EffectScriptImport.API.imports.map { it.value.javaClass.simpleName to it.value }.toTypedArray()
            )), args[0], scripts.computeIfAbsent(args[0]) { Scripts.compile(args[0]) }).call().run {
                return this.compiledScript.eval(this.bindings)
            }
        } catch (t: ScriptException) {
            TLoggerManager.getLogger(Cronus.getInst()).error("脚本执行出错: " + t.message)
        }
        return "<Null.Script>"
    }
}