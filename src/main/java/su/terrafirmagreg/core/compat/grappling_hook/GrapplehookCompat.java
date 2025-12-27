package su.terrafirmagreg.core.compat.grappling_hook;

import java.util.HashSet;

import com.yyon.grapplinghook.entities.grapplehook.GrapplehookEntity;
import com.yyon.grapplinghook.items.GrapplehookItem;
import com.yyon.grapplinghook.network.GrappleDetachMessage;
import com.yyon.grapplinghook.server.ServerControllerManager;
import com.yyon.grapplinghook.utils.GrapplemodUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;

// Detach any grappling hook controllers before switching dimensions

public class GrapplehookCompat {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(GrapplehookCompat::preDimensionChange);
    }

    public static void preDimensionChange(EntityTravelToDimensionEvent event) {
        // Code modified from com.yyon.grapplinghook.common.CommonEventHandlers.onLivingDeath
        if (event.getEntity() instanceof Player entity && !entity.level().isClientSide) {
            int id = entity.getId();
            HashSet<GrapplehookEntity> grapplehookEntities = ServerControllerManager.allGrapplehookEntities.get(id);
            if (grapplehookEntities != null) {
                for (GrapplehookEntity hookEntity : grapplehookEntities) {
                    hookEntity.removeServer();
                }
                grapplehookEntities.clear();
            }
            ServerControllerManager.attached.remove(id);
            GrapplehookItem.grapplehookEntitiesLeft.remove(entity);
            GrapplehookItem.grapplehookEntitiesRight.remove(entity);

            // Detach GrappleController
            GrapplemodUtils.sendToCorrectClient(new GrappleDetachMessage(id), id, entity.level());
            // Detach AirfrictionController
            GrapplemodUtils.sendToCorrectClient(new GrappleDetachMessage(id), id, entity.level());

        }
    }
}
