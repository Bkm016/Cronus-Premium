package ink.ptms.cronus.event

import ink.ptms.cronus.service.guide.GuideWayData
import io.izzel.taboolib.module.event.EventCancellable
import io.izzel.taboolib.module.event.EventNormal
import org.bukkit.Bukkit

class CronusGuideWayUpdateEvent(val guideWayData: GuideWayData) : EventCancellable<CronusGuideWayUpdateEvent>() {

    init {
        async(!Bukkit.isPrimaryThread())
    }
}