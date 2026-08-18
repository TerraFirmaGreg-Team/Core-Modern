package su.terrafirmagreg.core.common.event;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class MobSpawnEvent {

    // This prevents mobs from spawning on "non-natural blocks" in other dimensions.
    // Mostly copied from TFC's handling for the overworld

    @SubscribeEvent
    public static void onLivingSpawnCheck(net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn event) {

        final LivingEntity entity = event.getEntity();
        final LevelAccessor level = event.getLevel();
        final MobSpawnType spawn = event.getSpawnType();

        // Let TFC handle the overworld
        if (level == Level.OVERWORLD)
            return;

        // We only care about "natural" spawns
        if (spawn == MobSpawnType.NATURAL || spawn == MobSpawnType.CHUNK_GENERATION || spawn == MobSpawnType.REINFORCEMENT) {
            if (!Helpers.isBlock(level.getBlockState(entity.blockPosition().below()), TFCTags.Blocks.MONSTER_SPAWNS_ON)) {
                event.setSpawnCancelled(true);
                event.setCanceled(true);
            }
        }
    }
}
