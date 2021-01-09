package ink.ptms.cronus.uranus.function.impl;

import ink.ptms.cronus.uranus.annotations.Auto;
import ink.ptms.cronus.uranus.function.Function;
import ink.ptms.cronus.uranus.program.Program;
import org.bukkit.util.NumberConversions;

import java.text.DecimalFormat;

/**
 * @Author 坏黑
 * @Since 2019-05-12 15:24
 */
@Auto
public class FunctionFormatNumber extends Function {

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @Override
    public String getName() {
        return "format.number";
    }

    @Override
    public Object eval(Program program, String... args) {
        return decimalFormat.format(NumberConversions.toDouble(args[0]));
    }
}
