package su.terrafirmagreg.core.mixins.common.create_additions;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlockEntity;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

@Mixin(value = LiquidBlazeBurnerBlockEntity.class, remap = false)
public abstract class LiquidBlazeBurnerBlockEntityMixin extends BlockEntity {

    @Unique
    LiquidBlazeBurnerBlockEntity be = (LiquidBlazeBurnerBlockEntity) ((Object) this);

    public LiquidBlazeBurnerBlockEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Inject(method = "burningTick", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$burningTick(CallbackInfo cir) {
        cir.cancel();

        if (!be.getLevel().isClientSide()) {
            if (be.first) {
                update(tankInventory.getFluid());
            }
            // TFGCore.LOGGER.info("Remaining Burn Time: {}", remainingBurnTime);

            if (be.getRemainingBurnTime() >= 1 || !recipeCache.isEmpty()) {
                if (this.tankInventory.getFluidAmount() >= 100) {
                    if (remainingBurnTime <= 1000) {
                        try {
                            remainingBurnTime += ((LiquidBurningRecipe) this.recipeCache.get()).getBurnTime(); // BurnTime()
                                                                                                              // / 10
                            activeFuel = ((LiquidBurningRecipe) this.recipeCache.get()).isSuperheated()
                                    ? LiquidBlazeBurnerBlockEntity.FuelType.SPECIAL
                                    : LiquidBlazeBurnerBlockEntity.FuelType.NORMAL;

                        } catch (Exception var2) {
                            return;
                        }

                        this.tankInventory.drain(100, IFluidHandler.FluidAction.EXECUTE);
                        BlazeBurnerBlock.HeatLevel prev = be.getHeatLevelFromBlock();
                        playSound();
                        be.updateBlockState();
                        if (prev != be.getHeatLevelFromBlock()) {
                            be.getLevel().playSound((Player) null, worldPosition, SoundEvents.BLAZE_AMBIENT,
                                    SoundSource.BLOCKS, 0.125F + this.level.random.nextFloat() * 0.125F,
                                    1.15F - this.level.random.nextFloat() * 0.25F);
                            be.spawnParticleBurst(this.activeFuel == LiquidBlazeBurnerBlockEntity.FuelType.SPECIAL);
                        }

                    }
                }
            }
        }

    }

    @Shadow
    protected abstract void update(FluidStack stack);

    @Shadow
    private Optional<LiquidBurningRecipe> recipeCache;
    @Shadow
    protected FluidTank tankInventory;
    @Shadow
    protected int remainingBurnTime;
    @Shadow
    protected LiquidBlazeBurnerBlockEntity.FuelType activeFuel;

    @Shadow
    protected abstract void playSound();

}
