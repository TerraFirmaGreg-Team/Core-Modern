/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the License at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.entity.animals.tfclemming;

import net.dries007.tfc.client.TFCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import su.terrafirmagreg.core.common.entity.TFGPest;

public class TFCLemming extends TFGPest {
    public TFCLemming(EntityType<? extends TFGPest> type, Level level) {
        super(type, level, TFCSounds.RAT);
    }

    public static boolean spawnRules(EntityType<?> type, LevelAccessor level, MobSpawnType spawn,
            BlockPos pos, RandomSource rand) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
