package su.terrafirmagreg.core.mixins.common.gtceu;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.gregtechceu.gtceu.common.item.tool.behavior.LighterBehavior;

import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = LighterBehavior.class, remap = false)
public abstract class LighterBehaviorMixin {

    @Shadow
    @Final
    private boolean canOpen;

    @Shadow
    @Final
    public static String LIGHTER_OPEN;

    @Shadow
    public abstract boolean consumeFuel(@Nullable Player player, ItemStack stack);

    /**
     * @author Pyritie
     * @reason Overwrite's GT's fire starting behaviour to use TFC's system instead, so things like
     * matches can light TFC devices
     */
    @Overwrite
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        CompoundTag tag = itemStack.getOrCreateTag();
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockState state = level.getBlockState(pos);

        if ((!canOpen || tag.getBoolean(LIGHTER_OPEN)) && (player == null || !player.isShiftKeyDown())) {
            if (!consumeFuel(player, itemStack))
                return InteractionResult.PASS;

            boolean cancelled = StartFireEvent.startFire(level, pos, state, clickedFace, player, itemStack, StartFireEvent.FireStrength.STRONG);
            if (!cancelled) {
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
