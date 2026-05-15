package su.terrafirmagreg.core.common.block.asphalt;

import com.gregtechceu.gtceu.utils.EntityDamageUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class AsphaltRoadHelper {

    public static final int TEMPERATURE = 430;

    public static final int HOT_TICKS_UNTIL_SET = 200;

    public static final long HEAT_DAMAGE_INTERVAL_TICKS = 20L;
    public static final int HOT_CONTACT_MAX_DAMAGE = 1;
    public static final float HOT_CONTACT_DAMAGE_MULTIPLIER = 1.0F;
    public static final float HOT_ITEM_DAMAGE = 1.0F;

    public static final int POURING_SPREAD_MAX_BLOCKS = 20;
    public static final int POURING_SPREAD_BATCH_PER_TICK = 1;
    public static final int POURING_TICKS_UNTIL_HOT = 10;
    public static final int POURING_TICK_DELAY = 1;
    public static final int FIELD_POUR_MB = 1000;
    public static final int PATCH_POUR_MB = 50;

    // Asphalt Road Properties
    public static final EnumProperty<AsphaltRoadMarkingMask> MASK = EnumProperty.create("mask", AsphaltRoadMarkingMask.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    private AsphaltRoadHelper() {
    }

    /**
     * Client-side ambient particles for hot asphalt (pouring + cooling hot road).
     */
    public static void spawnHotAsphaltAmbient(Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            return;
        }
        double baseY = pos.getY() + 0.94 + random.nextDouble() * 0.06;
        int puffs = random.nextInt(2);
        for (int i = 0; i < puffs; i++) {
            double x = pos.getX() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double rise = 0.05 + random.nextDouble() * 0.04;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, baseY, z, 0.0, rise, 0.0);
        }
    }

    public static void tickBurn(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (level.getGameTime() % HEAT_DAMAGE_INTERVAL_TICKS != 0L) {
            return;
        }

        EntityDamageUtil.applyTemperatureDamage(living, TEMPERATURE, HOT_CONTACT_DAMAGE_MULTIPLIER,
                HOT_CONTACT_MAX_DAMAGE);
    }
}
