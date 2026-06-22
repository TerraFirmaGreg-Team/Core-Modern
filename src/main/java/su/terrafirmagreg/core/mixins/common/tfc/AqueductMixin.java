package su.terrafirmagreg.core.mixins.common.tfc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.rock.AqueductBlock;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;

@Mixin(value = AqueductBlock.class, remap = false)
public abstract class AqueductMixin extends HorizontalDirectionalBlock implements IFluidLoggable {

    protected AqueductMixin(Properties properties) {
        super(properties);
    }

    /**
     * @author Pyritie
     * @reason Lets aqueducts be filled with any of our fluids
     */
    @Overwrite(remap = false)
    public @NotNull FluidProperty getFluidProperty() {
        return TFGBlockProperties.SPACE_WATER_AND_LAVA;
    }

    // Prevent picking up lava from aqueducts, as that would allow you to get infinite lava

    @Inject(method = "pickupBlock", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$pickupBlock(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        if (state.getValue(getFluidProperty()).getFluid().isSame(Fluids.LAVA)) {
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1f, 1f);
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    // Copy over some methods from Beneath's aqueduct to handle lava

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(getFluidProperty()).getFluid().getFluidType().getLightLevel();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (state.getValue(getFluidProperty()).getFluid().isSame(Fluids.LAVA) && !entity.fireImmune() && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Nullable
    @Override
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob entity) {
        return state.getValue(getFluidProperty()).getFluid().isSame(Fluids.LAVA) ? BlockPathTypes.DAMAGE_FIRE : null;
    }
}
