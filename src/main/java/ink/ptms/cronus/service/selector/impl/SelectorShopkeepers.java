package ink.ptms.cronus.service.selector.impl;

import com.nisovin.shopkeepers.Shopkeeper;
import com.nisovin.shopkeepers.ShopkeepersPlugin;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @Author 坏黑
 * @Since 2019-07-01 12:02
 */
public class SelectorShopkeepers extends Selector {

    @Override
    public String[] getPrefix() {
        return new String[] {"shopkeepers", "shopkeeper", "shop"};
    }

    @Override
    public String getPlugin() {
        return "Shopkeepers";
    }

    @Override
    public String getDisplay(String in, Player player) {
        try {
            Shopkeeper shopkeeper = ((ShopkeepersPlugin) plugin).getShopkeeper(UUID.fromString(in));
            return shopkeeper == null ? "?" : shopkeeper.getName();
        } catch (Throwable ignored) {
        }
        return "Invalid UUID";
    }

    @Override
    public String fromEntity(Entity entity) {
        ActiveMob activeMob = ((MythicMobs) plugin).getMobManager().getActiveMob(entity.getUniqueId()).orElse(null);
        return activeMob != null ? activeMob.getType().getInternalName() : null;
    }

    @Override
    public boolean isSelect(Entity entity, String in) {
        Shopkeeper shopkeeperByEntity = ((ShopkeepersPlugin) plugin).getShopkeeperByEntity(entity);
        return shopkeeperByEntity != null && shopkeeperByEntity.getUniqueId().toString().equals(in);
    }
}
