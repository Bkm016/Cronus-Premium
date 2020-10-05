package ink.ptms.cronus.service.guide;

import com.google.common.collect.Lists;
import ink.ptms.cronus.Cronus;
import ink.ptms.cronus.event.CronusGuideWayUpdateEvent;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.hologram.Hologram;
import io.izzel.taboolib.module.hologram.THologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @Author 坏黑
 * @Since 2019-05-29 19:32
 */
public class GuideWayData {

    private static final GuideWay service = Cronus.getCronusService().getService(GuideWay.class);
    private static final DecimalFormat doubleFormat = new DecimalFormat("#.##");
    private final Player player;
    private final String owner;
    private final Location target;
    private final List<Hologram> entity;
    private final List<String> text;
    private final double distance;

    GuideWayData(String owner, Location target, List<Hologram> entity, List<String> text, double distance) {
        this.owner = owner;
        this.player = Bukkit.getPlayerExact(owner);
        this.target = target;
        this.entity = entity;
        this.text = text;
        this.distance = distance;
    }

    public static GuideWayData create(Player player, Location target, List<String> text, double distance) {
        Location midPoint = getMidPoint(player.getEyeLocation(), target, distance);
        if (midPoint != null) {
            String dis = doubleFormat.format(player.getLocation().distance(target));
            List<Hologram> list = Lists.newArrayList();
            for (int i = 0; i < text.size(); i++) {
                list.add(THologram.create(midPoint.clone().add(0, i * -0.3 - 0.5, 0), text.get(i).replace("{distance}", dis), player));
            }
            return new GuideWayData(player.getName(), target, list, text, distance);
        }
        return null;
    }

    public void update() {
        try {
            CronusGuideWayUpdateEvent event = new CronusGuideWayUpdateEvent(this).call();
            if (event.isCancelled()) {
                return;
            }
            if (player == null || !player.isOnline()) {
                cancel();
                return;
            }
            Location midPoint = getMidPoint(player.getEyeLocation(), target, distance);
            if (midPoint != null) {
                String distance = doubleFormat.format(player.getLocation().distance(target));
                Bukkit.getScheduler().runTask(Cronus.getInst(), () -> {
                    for (int i = 0; i < entity.size(); i++) {
                        entity.get(i).flash(text.get(i).replace("{distance}", distance));
                        entity.get(i).flash(midPoint.clone().add(0, i * -0.3 - 0.5, 0));
                    }
                });
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void cancel() {
        entity.forEach(Hologram::delete);
    }

    public void display() {
        if (player != null && player.isOnline()) {
            player.spawnParticle(Version.isAfter(Version.v1_9) ? Particle.END_ROD : Particle.CLOUD, target, 500, 0.05, 100, 0.05, 0);
        }
    }

    public static Location getMidPoint(Location start, Location end, double distance) {
        if (!start.getWorld().equals(end.getWorld())) {
            return null;
        }
        if (start.distance(end) < distance) {
            return end.clone();
        }
        Vector vectorAB = end.clone().subtract(start).toVector().normalize();
        return start.clone().add(vectorAB.clone().multiply(distance));
    }

    public String getOwner() {
        return owner;
    }

    public Location getTarget() {
        return target;
    }

    public List<Hologram> getHologram() {
        return entity;
    }

    public List<String> getText() {
        return text;
    }

    public double getDistance() {
        return distance;
    }
}