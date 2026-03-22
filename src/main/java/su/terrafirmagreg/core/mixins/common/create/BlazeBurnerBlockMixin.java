package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

@Mixin(value = BlazeBurnerBlock.class)
public abstract class BlazeBurnerBlockMixin {

    /**
     * Play a sound when trying to insert valid fuel in airless dimensions.
     */
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlock;tryInsert(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;ZZZ)Lnet/minecraft/world/InteractionResultHolder;", remap = false))
    private void tfg$playExtinguishInAirlessDimension(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockRayTraceResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (AllTags.AllItemTags.BLAZE_BURNER_FUEL_REGULAR.matches(heldItem)
                    || AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.matches(heldItem)) {
                Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
            }
        }
    }

    /**
     * Intercept tryInsert before it calls tryUpdateFuel. If the item has an IFluidHandlerItem,
     * handle the fluid transfer using Forge's fluid API, then cancel. This prevents
     * Create's shrink(1) from destroying reusable containers like GT drums, and prevents
     * CreateLiquidFuel's mixin from running its copy-without-drain logic.
     */
    @Inject(method = "tryInsert", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity;tryUpdateFuel(Lnet/minecraft/world/item/ItemStack;ZZ)Z", remap = false), cancellable = true, remap = false)
    private static void tfg$handleFluidFuelInsertion(BlockState state, Level world, BlockPos pos, ItemStack stack,
            boolean doNotConsume, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        if (world.dimension() != Level.OVERWORLD && world.dimension() != Level.NETHER) {
            cir.setReturnValue(InteractionResultHolder.fail(ItemStack.EMPTY));
            return;
        }

        IFluidHandlerItem handler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null || handler.getTanks() != 1)
            return;

        FluidStack fluidInItem = handler.getFluidInTank(0);
        if (fluidInItem.isEmpty())
            return;
        if (!BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.containsKey(fluidInItem.getFluid()))
            return;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof BlazeBurnerBlockEntity burnerBE))
            return;

        SmartFluidTank stomach = (SmartFluidTank) burnerBE.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (stomach == null)
            return;

        int accepted = stomach.fill(fluidInItem, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            cir.setReturnValue(InteractionResultHolder.fail(ItemStack.EMPTY));
            return;
        }

        if (simulate) {
            cir.setReturnValue(InteractionResultHolder.success(ItemStack.EMPTY));
            return;
        }

        if (!doNotConsume && !world.isClientSide) {
            FluidStack drained = handler.drain(new FluidStack(fluidInItem.getFluid(), accepted), IFluidHandler.FluidAction.EXECUTE);
            stomach.fill(drained, IFluidHandler.FluidAction.EXECUTE);

            ItemStack container = handler.getContainer();
            if (container == stack) {
                // Container was mutated in-place (e.g. GT drum). Already updated in player's hand.
                cir.setReturnValue(InteractionResultHolder.success(ItemStack.EMPTY));
            } else {
                // Container is a different item (e.g. bucket -> empty bucket). Consume original, return new.
                stack.shrink(1);
                cir.setReturnValue(InteractionResultHolder.success(container));
            }
        } else {
            cir.setReturnValue(InteractionResultHolder.success(ItemStack.EMPTY));
        }
    }
}
