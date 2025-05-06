package su.terrafirmagreg.core.mixins.common.create;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionPacket;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import su.terrafirmagreg.core.compat.create.ChainGTMaterialInterface;

@Mixin(value = ChainConveyorConnectionPacket.class)
public abstract class ChainConveyorConnectionPacketMixin extends BlockEntityConfigurationPacket<ChainConveyorBlockEntity> {
    @Shadow private ItemStack chain;
    @Shadow private BlockPos targetPos;

    public ChainConveyorConnectionPacketMixin(BlockPos pos) {
        super(pos);
    }

    @WrapOperation( method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;addConnectionTo(Lnet/minecraft/core/BlockPos;)Z"), remap = false)
    private boolean tfg$applySettings$addConnectionTo(ChainConveyorBlockEntity instance, BlockPos target, Operation<Boolean> original)
    {
        MaterialStack chainMatStack = ChemicalHelper.getMaterial(chain.getItem());
        if (chainMatStack != null )
        {
            Material chainMat = chainMatStack.material();
            ChainGTMaterialInterface cgtinstance = (ChainGTMaterialInterface) instance;
            cgtinstance.addConnectionMaterial(target, chainMat);
        }
        return original.call(instance, target);
    }


    @ModifyArg( method = "applySettings(Lnet/minecraft/server/level/ServerPlayer;Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;)V"))
    private ItemStack tfg$applySettings$placeItemBackInInventory(ItemStack pStack, @Local(ordinal = 0, argsOnly = true) ChainConveyorBlockEntity be)
    {
        BlockPos localPos = targetPos.subtract(be.getBlockPos());
        ChainGTMaterialInterface be_GTMaterialInterface = (ChainGTMaterialInterface) be;
        Item chainItem = be_GTMaterialInterface.getConnectionChainItem(localPos);
        return new ItemStack(chainItem, pStack.getCount());
    }
}
