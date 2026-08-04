package su.terrafirmagreg.core.common.entity.camels;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.Temptable;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.util.Helpers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class TFGAbstractCamel extends Camel implements MammalProperties, Temptable {
    protected TFGAbstractCamel(EntityType<? extends Camel> entityType, Level level)
    {
        super(entityType, level);
    }

    @Override
    public @Nullable TFGAbstractCamel getBreedOffspring(ServerLevel level, AgeableMob other)
    {
        final AgeableMob mob = MammalProperties.super.getBreedOffspring(level, other);
        return mob instanceof TFGAbstractCamel camel ? camel : null;
    }

    @Override
    public TagKey<Item> getFoodTag()
    {
        // TODO: ADD PROPER TAG
        return TFCTags.Items.FOODS;
    }

    public boolean vanillaParentingCheck(AbstractHorse camel)
    {
        return !camel.isVehicle() && !camel.isPassenger();
    }

    @Override
    public boolean isFood(ItemStack stack)
    {
        return MammalProperties.super.isFood(stack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
        InteractionResult result = MammalProperties.super.mobInteract(player, hand);
        return result == InteractionResult.PASS ? super.mobInteract(player, hand) : result;
    }

    @Override
    public EntityType<?> getEntityTypeForBaby()
    {
        return MammalProperties.super.getEntityTypeForBaby();
    }

    @Override
    protected float getRiddenSpeed(Player player)
    {
        float sprintSpeedBonus = 0.12F; // Vanilla: 0.1F
        float f = player.isSprinting() && this.getJumpCooldown() == 0 ? sprintSpeedBonus : 0.0F;
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
    }

    // Dromedary camels sprinting on dry blocks are a bit faster than an average horse (0.264 vs 0.225)
    // Bactrian camels sprinting on dry blocks are the same as the average horse (0.225 vs 0.225)
    @Override
    protected float getBlockSpeedFactor()
    {
        // TODO: ADD PROPER TAG FOR FASTER BLOCKS
        if ((Helpers.isBlock(level().getBlockState(blockPosition().below()), TFCTags.Blocks.FARMLAND)))
        {
            return 1.2F;
        }
        else return super.getBlockSpeedFactor();
    }
}
