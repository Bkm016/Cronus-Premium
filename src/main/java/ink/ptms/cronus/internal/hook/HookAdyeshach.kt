package ink.ptms.cronus.internal.hook

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.api.event.AdyeshachEntityInteractEvent
import ink.ptms.cronus.Cronus
import ink.ptms.cronus.builder.task.data.Entity.EntitySelect
import ink.ptms.cronus.service.dialog.Dialog
import ink.ptms.cronus.service.selector.EntitySelector
import ink.ptms.cronus.service.selector.impl.Selector
import io.izzel.taboolib.module.inject.TInject
import io.izzel.taboolib.module.inject.TListener
import io.izzel.taboolib.util.lite.Catchers
import io.izzel.taboolib.util.lite.cooldown.Cooldown
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Cronus
 * ink.ptms.cronus.internal.hook.ListenerAdyeshach
 *
 * @author sky
 * @since 2021/1/9 8:18 下午
 */
@TListener(depend = ["Adyeshach"])
class HookAdyeshach : Listener {

    @TInject
    private val cooldown = Cooldown("Adyeshach|dialog", 200)

    init {
        Cronus.getCronusService().getService(EntitySelector::class.java).selectors.add(SelectorAdyeshach())
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun e(e: AdyeshachEntityInteractEvent) {
        if (e.isMainHand) {
            // 管理员工具
            if (e.player.itemInHand.type == Material.NETHER_STAR) {
                try {
                    Catchers.getPlayerdata()[e.player.name]?.forEach {
                        if (it is EntitySelect) {
                            e.isCancelled = true
                            e.player.chat("anpc=${e.entity.uniqueId}")
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            // 对话
            if (!cooldown.isCooldown(e.player.name, 0)) {
                Cronus.getCronusService().getService(Dialog::class.java).dialogs.forEach {
                    if (it.target != null && (it.target.startsWith("anpc=") || it.target.startsWith("ady="))) {
                        if (e.entity.id == it.target.split("=")[1] && (it.condition == null || it.condition.check(e.player))) {
                            try {
                                it.dialog.display(e.player)
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                            return
                        }
                    }
                }
            }
        }
    }

    class SelectorAdyeshach : Selector() {

        override fun getPrefix(): Array<String> {
            return arrayOf("anpc", "ady")
        }

        override fun getPlugin(): String {
            return "Adyeshach"
        }

        override fun getDisplay(str: String, player: Player): String {
            return AdyeshachAPI.getEntityFromId(str, player)?.getCustomName() ?: "?"
        }

        override fun fromEntity(entity: Entity?): String {
            TODO("Not yet implemented")
        }

        override fun isSelect(entity: Entity?, `in`: String?): Boolean {
            TODO("Not yet implemented")
        }
    }
}