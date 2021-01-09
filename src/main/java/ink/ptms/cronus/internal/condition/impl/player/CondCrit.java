package ink.ptms.cronus.internal.condition.impl.player;

import ink.ptms.cronus.database.data.DataQuest;
import ink.ptms.cronus.internal.condition.Cond;
import ink.ptms.cronus.internal.condition.special.CondBoolean;
import io.izzel.taboolib.module.locale.TLocale;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

/**
 * @Author 坏黑
 * @Since 2019-06-17 20:21
 */
@Cond(name = "player.crit", pattern = "player\\.crit", example = "player.crit")
public class CondCrit extends CondBoolean {

    @Override
    public boolean getBoolean(Player player, DataQuest quest, Event event) {
        return (player.getVelocity().getY() + 0.0784000015258789) >= 0 && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && player.getVehicle() == null;
    }

    @Override
    public String translate() {
        if (!negative) {
            return TLocale.asString("translate-condition-crit0");
        } else {
            return TLocale.asString("translate-condition-crit1");
        }
    }

    @Override
    public String toString() {
        return "CondCrit{" +
                "negative=" + negative +
                '}';
    }
}
