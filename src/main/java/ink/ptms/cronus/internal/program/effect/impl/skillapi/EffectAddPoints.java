package ink.ptms.cronus.internal.program.effect.impl.skillapi;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerData;
import ink.ptms.cronus.internal.program.QuestProgram;
import ink.ptms.cronus.uranus.annotations.Auto;
import ink.ptms.cronus.uranus.program.Program;
import ink.ptms.cronus.uranus.program.effect.Effect;
import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.module.locale.logger.TLogger;
import org.bukkit.util.NumberConversions;

import java.util.regex.Matcher;

/**
 * @Author 坏黑
 * @Since 2019-05-11 17:06
 */
@Auto
public class EffectAddPoints extends Effect {

    @TInject
    private static TLogger logger;
    private ExpSource source;
    private int count;

    @Override
    public String pattern() {
        return "skillapi\\.addpoint(s)?\\.(?<source>\\S+) (?<count>.+)";
    }

    @Override
    public String getExample() {
        return "skillapi.addpoints.[expsource] [count]";
    }

    @Override
    public void match(Matcher matcher) {
        try {
            source = ExpSource.valueOf(matcher.group("source").toUpperCase());
        } catch (Throwable t) {
            source = ExpSource.SPECIAL;
            logger.warn("Invalid ExpSource: " + matcher.group("source"));
        }
        count = NumberConversions.toInt(matcher.group("count"));
    }

    @Override
    public void eval(Program program) {
        if (program instanceof QuestProgram) {
            PlayerData playerData = SkillAPI.getPlayerData(((QuestProgram) program).getPlayer());
            playerData.givePoints(count, source);
        }
    }

    @Override
    public String toString() {
        return "EffectAddPoints{" +
                "source=" + source +
                ", count=" + count +
                '}';
    }
}
