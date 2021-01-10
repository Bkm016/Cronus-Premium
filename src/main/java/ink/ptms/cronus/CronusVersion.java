package ink.ptms.cronus;

import ink.ptms.cronus.util.Utils;
import org.bukkit.util.NumberConversions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author 坏黑
 * @Since 2019-07-26 0:01
 */
public class CronusVersion {

    private static final Pattern pattern = Pattern.compile("(?<v1>\\d+)\\.(?<v2>\\d+)\\.(?<v3>\\d+)\\.(?<year>\\d+)\\.(?<month>\\d+)\\.(?<day>\\d+)([- ](?<type>.+))?");
    private final int v1;
    private final int v2;
    private final int v3;
    private final int year;
    private final int month;
    private final int day;
    private final String type;

    CronusVersion(int v1, int v2, int v3, int year, int month, int day, String type) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.year = year;
        this.month = month;
        this.day = day;
        this.type = type;
    }

    public boolean isAfter(CronusVersion version) {
        return compareTo(version) >= 0;
    }

    public boolean isBefore(CronusVersion version) {
        return compareTo(version) < 0;
    }

    public int compareTo(CronusVersion version) {
        if (v1 != version.v1) {
            return Integer.compare(v1, version.v1);
        } else if (v2 != version.v2) {
            return Integer.compare(v2, version.v2);
        } else if (v3 != version.v3) {
            return Integer.compare(v3, version.v3);
        } else if (year != version.year) {
            return Integer.compare(year, version.year);
        } else if (month != version.month) {
            return Integer.compare(month, version.month);
        } else {
            return Integer.compare(day, version.day);
        }
    }

    public static CronusVersion fromString(String in) {
        Matcher matcher = pattern.matcher(in);
        if (matcher.find()) {
            return new CronusVersion(
                    NumberConversions.toInt(matcher.group("v1")),
                    NumberConversions.toInt(matcher.group("v2")),
                    NumberConversions.toInt(matcher.group("v3")),
                    NumberConversions.toInt(matcher.group("year")),
                    NumberConversions.toInt(matcher.group("month")),
                    NumberConversions.toInt(matcher.group("day")),
                    Utils.NonNull(matcher.group("type")));
        } else {
            return new CronusVersion(0, 0, 0, 0, 0, 0, "Error");
        }
    }

    @Override
    public String toString() {
        return v1 + "." + v2 + "." + v3 + "." + year + "." + month + "." + day + " " + type;
    }

    public int getV1() {
        return v1;
    }

    public int getV2() {
        return v2;
    }

    public int getV3() {
        return v3;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getType() {
        return type;
    }
}
