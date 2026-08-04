package su.terrafirmagreg.core.common.entity.camels;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.entities.Temptable;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import su.terrafirmagreg.core.common.data.TFGTags;

public abstract class TFCAbstractCamel extends Camel implements MammalProperties, Temptable {
    protected TFCAbstractCamel(EntityType<? extends Camel> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean spawnRules(EntityType<? extends TFCAbstractCamel> type, LevelAccessor level, MobSpawnType spawn, BlockPos pos, RandomSource rand) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public @Nullable TFCAbstractCamel getBreedOffspring(ServerLevel level, AgeableMob other) {
        final AgeableMob mob = MammalProperties.super.getBreedOffspring(level, other);
        return mob instanceof TFCAbstractCamel camel ? camel : null;
    }

    @Override
    public TagKey<Item> getFoodTag() {
        return TFGTags.Items.CAMEL_FOOD;
    }

    public boolean vanillaParentingCheck(AbstractHorse camel) {
        return !camel.isVehicle() && !camel.isPassenger();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return MammalProperties.super.isFood(stack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = MammalProperties.super.mobInteract(player, hand);
        return result == InteractionResult.PASS ? super.mobInteract(player, hand) : result;
    }

    @Override
    public EntityType<?> getEntityTypeForBaby() {
        return MammalProperties.super.getEntityTypeForBaby();
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        float sprintSpeedBonus = 0.12F; // Vanilla: 0.1F
        float f = player.isSprinting() && this.getJumpCooldown() == 0 ? sprintSpeedBonus : 0.0F;
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
    }

    @Override
    protected float getBlockSpeedFactor() {
        if ((Helpers.isBlock(level().getBlockState(blockPosition().below()), TFGTags.Blocks.CAMEL_FASTER_ON))) {
            return 1.2F;
        } else
            return super.getBlockSpeedFactor();
    }
}
