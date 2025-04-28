package su.terrafirmagreg.core.mixins.common.create;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.compat.create.ChainGTMaterialInterface;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = ChainConveyorBlock.class, remap = false)
public abstract class ChainConveyorBlockMixin extends KineticBlock implements IBE<ChainConveyorBlockEntity>, IHaveBigOutline {
    public ChainConveyorBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!pLevel.isClientSide() && pPlayer != null && pPlayer.getItemInHand(pHand).is(TFGTags.Items.Chains))
        {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "onSneakWrenched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$onSneakWrenched(BlockState state, UseOnContext context, CallbackInfoReturnable<InteractionResult> cir)
    {
        Player player = context.getPlayer();
        if (player == null)
        {
            cir.setReturnValue(super.onSneakWrenched(state, context));
            return;
        }
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
            be.cancelDrops = true;
            if (player.isCreative())
                return;
            for (BlockPos targetPos : be.connections) {
                int chainCost = ChainConveyorBlockEntity.getChainCost(targetPos);
                ChainGTMaterialInterface be_GTMaterialInterface = (ChainGTMaterialInterface) be;
                Item chainItem = be_GTMaterialInterface.getConnectionChainItem(targetPos);
                while (chainCost > 0) {
                    player.getInventory()
                            .placeItemBackInInventory(new ItemStack(chainItem, Math.min(chainCost, 64)));
                    chainCost -= 64;
                }
            }
        });

        cir.setReturnValue(super.onSneakWrenched(state, context));
    }
}
