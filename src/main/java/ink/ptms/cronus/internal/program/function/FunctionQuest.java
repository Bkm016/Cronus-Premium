package ink.ptms.cronus.internal.program.function;

import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.database.data.DataPlayer;
import ink.ptms.cronus.database.data.DataQuest;
import ink.ptms.cronus.internal.Quest;
import ink.ptms.cronus.internal.program.QuestProgram;
import ink.ptms.cronus.uranus.annotations.Auto;
import ink.ptms.cronus.uranus.function.Function;
import ink.ptms.cronus.uranus.program.Program;
import io.izzel.taboolib.module.locale.TLocale;

import java.util.Map;
import java.util.Objects;

/**
 * @Author 坏黑
 * @Since 2019-05-12 15:24
 */
@Auto
public class FunctionQuest extends Function {

    @Override
    public String getName() {
        return "quest";
    }

    @Override
    public Object eval(Program program, String... args) {
        if (program instanceof QuestProgram) {
            DataQuest dataQuest = ((QuestProgram) program).getDataQuest();
            DataPlayer dataPlayer = ((QuestProgram) program).getDataPlayer();
            switch (args[0].toLowerCase()) {
                case "display":
                    return dataQuest.getQuest().getDisplay();
                case "name":
                    return dataQuest.getCurrentQuest();
                case "stage":
                    return dataQuest.getCurrentStage();
                case "stage.next": {
                    Quest quest = dataQuest.getQuest();
                    if (quest == null) {
                        return "<Invalid-Quest>";
                    }
                    int index = dataQuest.getStageIndex(quest) + 1;
                    return index >= quest.getStage().size() ? "-" : quest.getStage().get(index).getId();
                }
                case "time.start":
                    return dataQuest.getTimeStart();
                case "time.complete":
                    return dataPlayer.getQuestCompleted().getOrDefault(dataQuest.getCurrentQuest(), 0L);
                case "visible": {
                    Quest quest = dataQuest.getQuest();
                    if (quest == null) {
                        return "<Invalid-Quest>";
                    }
                    return dataPlayer.getQuestHide().contains(quest.getId()) ? TLocale.asString("quest-placeholder-visible0") : TLocale.asString("quest-placeholder-visible1");
                }
                case "current": {
                    for (Map.Entry<String, DataQuest> pair : dataPlayer.getQuest().entrySet()) {
                        Quest q = pair.getValue().getQuest();
                        if (!dataPlayer.isQuestCompleted(pair.getKey()) && q != null && (q.getId().equals(args[1]) || Objects.equals(q.getLabel(), args[1]) || q.getBookTag().contains(args[1]))) {
                            return "true";
                        }
                    }
                    return "false";
                }
                case "completed": {
                    for (String id : dataPlayer.getQuestCompleted().keySet()) {
                        Quest q = Cronus.getCronusService().getRegisteredQuest().get(id);
                        if (q != null && (q.getId().equals(args[1]) || Objects.equals(q.getLabel(), args[1]) || q.getBookTag().contains(args[1]))) {
                            return "true";
                        }
                    }
                    return "false";
                }
                case "current.count": {
                    int i = 0;
                    for (Map.Entry<String, DataQuest> pair : dataPlayer.getQuest().entrySet()) {
                        Quest q = pair.getValue().getQuest();
                        if (!dataPlayer.isQuestCompleted(pair.getKey()) && q != null && (q.getId().equals(args[1]) || Objects.equals(q.getLabel(), args[1]) || q.getBookTag().contains(args[1]))) {
                            i++;
                        }
                    }
                    return String.valueOf(i);
                }
                case "completed.count": {
                    int i = 0;
                    for (String id : dataPlayer.getQuestCompleted().keySet()) {
                        Quest q = Cronus.getCronusService().getRegisteredQuest().get(id);
                        if (q != null && (q.getId().equals(args[1]) || Objects.equals(q.getLabel(), args[1]) || q.getBookTag().contains(args[1]))) {
                            i++;
                        }
                    }
                    return String.valueOf(i);
                }
                default:
                    return dataQuest.getCurrentQuest();
            }
        }
        return "<Non-Quest>";
    }
}
