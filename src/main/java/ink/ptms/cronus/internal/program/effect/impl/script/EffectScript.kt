package ink.ptms.cronus.internal.program.effect.impl.script

import ink.ptms.cronus.Cronus
import ink.ptms.cronus.uranus.Uranus
import ink.ptms.cronus.uranus.annotations.Auto
import ink.ptms.cronus.uranus.event.UranusScriptEvalEvent
import ink.ptms.cronus.uranus.program.Program
import ink.ptms.cronus.uranus.program.effect.Effect
import io.izzel.taboolib.module.locale.logger.TLoggerManager
import io.izzel.taboolib.util.lite.Scripts
import java.util.regex.Matcher
import javax.script.CompiledScript
import javax.script.ScriptException
import javax.script.SimpleBindings

/**
 * @Author 坏黑
 * @Since 2019-05-11 17:06
 */
@Auto
class EffectScript : Effect() {

    var script: String? = null
    var compiledScript: CompiledScript? = null

    override fun pattern(): String {
        return "(script|js) (?<script>.+)"
    }

    override fun getExample(): String {
        return "script [script]"
    }

    override fun match(matcher: Matcher) {
        script = matcher.group("script")
        compiledScript = Scripts.compile(matcher.group("script"))
    }

    override fun eval(program: Program) {
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
            )), script, compiledScript).call().run {
                this.compiledScript.eval(this.bindings)
            }
        } catch (t: ScriptException) {
            TLoggerManager.getLogger(Cronus.getInst()).error("脚本执行出错: " + t.message)
        }
    }

    override fun toString(): String {
        return "EffectScript{compiledScript=$compiledScript}"
    }
}
