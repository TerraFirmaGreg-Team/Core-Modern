package su.terrafirmagreg.core.common.block.asphalt;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import su.terrafirmagreg.core.common.data.TFGTags;

public final class AsphaltRoadHelper {

    /**
     * Scales GregTech {@link com.gregtechceu.gtceu.utils.EntityDamageUtil#applyTemperatureDamage}-style heat from
     * {@link AsphaltRoadHotBlock#TEMPERATURE_KELVIN}; ~0.2 matches prior ~0.5 vanilla damage per throttle when
     * standing on the block.
     */
    private static final float HEAT_DAMAGE_MULTIPLIER = 0.2F;

    public static final long THROTTLE_TICKS = 20L;

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
        if (level.getGameTime() % THROTTLE_TICKS != 0L) {
            return;
        }

        ItemStack feet = living.getItemBySlot(EquipmentSlot.FEET);
        if (feet.is(TFGTags.Items.HotProtectionEquipment)) {
            return;
        }

        int tempK = AsphaltRoadHotBlock.TEMPERATURE_KELVIN;
        if (tempK <= 320) {
            return;
        }

        float mult = HEAT_DAMAGE_MULTIPLIER;
        ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && chest.getItem() instanceof ArmorComponentItem armorItem) {
            mult *= armorItem.getArmorLogic().getHeatResistance();
        }

        // Same baseline as EntityDamageUtil.applyTemperatureDamage for T > 300 K (hot fluid / pipe contact).
        float damage = mult * (tempK - 300) / 50.0F;
        if (damage <= 0.0F) {
            return;
        }
        if (!living.isAlive()) {
            return;
        }
        if (living.getType().is(CustomTags.HEAT_IMMUNE)) {
            return;
        }
        if (living.getEffect(MobEffects.FIRE_RESISTANCE) != null) {
            return;
        }

        living.hurt(GTDamageTypes.HEAT.source(level), damage);
    }
}
