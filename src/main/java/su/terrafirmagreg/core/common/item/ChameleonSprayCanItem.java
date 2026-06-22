package su.terrafirmagreg.core.common.item;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.item.behavior.ChameleonSprayCanBehaviour;
import su.terrafirmagreg.core.config.TFGConfig;

public class ChameleonSprayCanItem extends Item {

    private final ChameleonSprayCanBehaviour behaviour = new ChameleonSprayCanBehaviour();

    public ChameleonSprayCanItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        var temporaryHandler = FluidUtil.getFluidHandler(level, pos, side);
        if (!temporaryHandler.isPresent()) {
            temporaryHandler = FluidUtil.getFluidHandler(level, pos, null);
        }

        if (temporaryHandler.isPresent()) {
            final var blockFluidHandler = temporaryHandler;
            boolean filledTank = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(itemCap -> blockFluidHandler.map(blockCap -> {
                int emptySpace = itemCap.getTankCapacity(0) - itemCap.getFluidInTank(0).getAmount();
                if (emptySpace <= 0)
                    return false;

                FluidStack transferable = blockCap.drain(emptySpace, IFluidHandler.FluidAction.SIMULATE);
                if (!transferable.isEmpty() && itemCap.isFluidValid(0, transferable)) {
                    FluidStack drained = blockCap.drain(emptySpace, IFluidHandler.FluidAction.EXECUTE);
                    itemCap.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
                return false;
            }).orElse(false)).orElse(false);

            if (filledTank) {
                assert context.getPlayer() != null;
                level.playSound(null, context.getPlayer().getX(), context.getPlayer().getY(), context.getPlayer().getZ(),
                        SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return this.behaviour.onItemUseFirst(stack, context);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof BucketPickup bucketPickup) {
                if (state.getFluidState().getType() == TFGFluids.PRISMATIC_PAINT.getSource()) {

                    boolean success = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(itemCap -> {

                        FluidStack fillStack = new FluidStack(TFGFluids.PRISMATIC_PAINT.getSource(), FluidType.BUCKET_VOLUME);
                        int filled = itemCap.fill(fillStack, IFluidHandler.FluidAction.SIMULATE);

                        if (filled == FluidType.BUCKET_VOLUME) {
                            itemCap.fill(fillStack, IFluidHandler.FluidAction.EXECUTE);
                            return true;
                        }
                        return false;
                    }).orElse(false);

                    if (success) {
                        bucketPickup.pickupBlock(level, pos, state);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                    }
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack heldItem, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player,
            @NotNull SlotAccess slotAccess) {
        if (action != ClickAction.SECONDARY || heldItem.isEmpty())
            return false;
        return handleSlotFluidTransfer(stack, heldItem, player, slotAccess::set);
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (action != ClickAction.SECONDARY)
            return false;
        ItemStack targetContainer = slot.getItem();
        if (targetContainer.isEmpty())
            return false;
        return handleSlotFluidTransfer(stack, targetContainer, player, slot::set);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        return this.behaviour.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        String chromCode = ChameleonSprayCanBehaviour.getChromaticCode(stack);
        if (chromCode != null) {
            tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.color_mode.chromatic", chromCode));
        } else {
            DyeColor normalColor = ChameleonSprayCanBehaviour.getColor(stack);
            if (normalColor != null) {
                String rawName = normalColor.getSerializedName();
                String stylizedName = rawName.substring(0, 1).toUpperCase() + rawName.substring(1);
                tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.color_mode.standard", stylizedName));
            } else {
                tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.color_mode.clear"));
            }
        }

        this.behaviour.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.literal(""));

        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.controls.header"));
        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.controls.paint_block"));
        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.controls.cycle_colors"));
        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.controls.open_radial"));

        tooltipComponents.add(Component.literal(""));

        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.features.header"));
        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.features.mass_paint_line1"));
        tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.features.mass_paint_line2"));

        double discountPct = (1.0 - TFGConfig.SERVER.CHAMELEON_SPRAY_CAN_BULK_MULTIPLIER.get()) * 100;
        if (discountPct > 0) {
            tooltipComponents.add(Component.translatable("tooltip.tfg.chameleon_spray_can.features.discount", (int) discountPct));
        }
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(fluidHandler -> {
                    int max = fluidHandler.getTankCapacity(0);
                    int current = fluidHandler.getFluidInTank(0).getAmount();
                    return max == 0 ? 0 : Math.round(13.0F * current / max);
                }).orElse(0);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return 0x00FFFF;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<FluidHandlerItemStack> holder = LazyOptional.of(() -> {
                int capacity = TFGConfig.SERVER.CHAMELEON_SPRAY_CAN_CAPACITY.get();
                return new FluidHandlerItemStack(stack, capacity) {
                    @Override
                    public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
                        return fluidStack.getFluid() == TFGFluids.PRISMATIC_PAINT.getSource();
                    }
                };
            });

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return holder.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    private boolean handleSlotFluidTransfer(ItemStack canStack, ItemStack containerStack, Player player, java.util.function.Consumer<ItemStack> containerUpdater) {
        return canStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(canCap -> FluidUtil.getFluidHandler(containerStack).map(containerCap -> {
            var transferred = FluidUtil.tryFluidTransfer(canCap, containerCap, canCap.getTankCapacity(0), true);
            if (!transferred.isEmpty()) {
                containerUpdater.accept(containerCap.getContainer());
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }
            return false;
        }).orElse(false)).orElse(false);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return super.getName(stack);
    }
}
