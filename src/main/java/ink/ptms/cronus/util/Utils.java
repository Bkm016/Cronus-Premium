package ink.ptms.cronus.util;

import io.izzel.taboolib.module.inject.TInject;
import io.izzel.taboolib.util.Strings;
import io.izzel.taboolib.util.lite.Numbers;
import io.izzel.taboolib.util.lite.cooldown.Cooldown;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.NumberConversions;

import java.util.Optional;

/**
 * @Author 坏黑
 * @Since 2019-05-11 15:42
 */
public class Utils {

    @TInject
    private static final Cooldown actionCooldown = new Cooldown("Cronus:Action", 100);
    private static final Color[] colors = new Color[] {
            Color.WHITE,
            Color.SILVER,
            Color.GRAY,
            Color.BLACK,
            Color.RED,
            Color.MAROON,
            Color.YELLOW,
            Color.OLIVE,
            Color.LIME,
            Color.OLIVE,
            Color.AQUA,
            Color.TEAL,
            Color.NAVY,
            Color.FUCHSIA,
            Color.PURPLE,
            Color.ORANGE
    };

    public static boolean isActionCooldown(CommandSender sender) {
        return actionCooldown.isCooldown(sender.getName(), 0);
    }

    public static boolean isNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static void addItem(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(e -> player.getWorld().dropItem(player.getLocation(), e));
    }

    public static String NonNull(Object in) {
        return in == null || Strings.isBlank(String.valueOf(in)) ? "-" : String.valueOf(in);
    }

    public static String NonNull(String in) {
        return Strings.isBlank(in) ? "-" : in;
    }

    public static ItemStack NonNull(ItemStack itemStack) {
        return Optional.ofNullable(itemStack).orElse(new ItemStack(Material.STONE));
    }

    public static org.bukkit.inventory.ItemStack getUsingItem(Player player, Material material) {
        return player.getItemInHand().getType() == material ? player.getItemInHand() : player.getInventory().getItemInOffHand();
    }

    public static String toSimple(String in) {
        return in.length() > 20 ? in.substring(0, in.length() - (in.length() - 10)) + "..." + in.substring(in.length() - 7) : in;
    }

    public static String fromLocation(Location location) {
        return location.getWorld().getName() + "," + parseInt(location.getX()) + "," + parseInt(location.getY()) + "," + parseInt(location.getZ()) ;
    }

    public static Object parseInt(double in) {
        return isInt(in) ? (long) in : in;
    }

    public static boolean next(int page, int size, int entry) {
        return size / (double) entry > page + 1;
    }

    public static boolean isInt(double in) {
        return NumberConversions.toLong(in) == in;
    }

    public static boolean isInt(String in) {
        try {
            Integer.parseInt(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isLong(String in) {
        try {
            Long.parseLong(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isDouble(String in) {
        try {
            Double.parseDouble(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isBoolean(String in) {
        try {
            Boolean.parseBoolean(in);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static Firework spawnFirework(Location location) {
        Firework entity = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = entity.getFireworkMeta();
        fireworkMeta.setPower(1);
        fireworkMeta.addEffect(FireworkEffect.builder()
                .flicker(Numbers.getRandom().nextBoolean())
                .trail(Numbers.getRandom().nextBoolean())
                .with(FireworkEffect.Type.values()[Numbers.getRandom().nextInt(FireworkEffect.Type.values().length)])
                .withColor(colors[Numbers.getRandom().nextInt(colors.length)])
                .build());
        entity.setFireworkMeta(fireworkMeta);
        return entity;
    }

    public static long toTime(String in) {
        if (in == null) {
            return 0;
        }
        if (in.equalsIgnoreCase("never") || in.equalsIgnoreCase("-1")) {
            return -1;
        }
        long time = 0;
        StringBuilder current = new StringBuilder();
        for (String charAt : in.toLowerCase().split("")) {
            if (Utils.isInt(charAt)) {
                current.append(charAt);
            } else {
                switch (charAt) {
                    case "d":
                        time += NumberConversions.toInt(current.toString()) * 24L * 60L * 60L * 1000L;
                        break;
                    case "h":
                        time += NumberConversions.toInt(current.toString()) * 60L * 60L * 1000L;
                        break;
                    case "m":
                        time += NumberConversions.toInt(current.toString()) * 60L * 1000L;
                        break;
                    case "s":
                        time += NumberConversions.toInt(current.toString()) * 1000L;
                        break;
                }
                current = new StringBuilder();
            }
        }
        return time;
    }
}
