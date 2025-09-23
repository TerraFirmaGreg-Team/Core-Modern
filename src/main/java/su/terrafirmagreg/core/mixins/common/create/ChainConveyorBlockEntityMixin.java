package su.terrafirmagreg.core.mixins.common.create;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.compat.create.ChainGTMaterialInterface;

@Mixin(value = ChainConveyorBlockEntity.class, remap = false)
public abstract class ChainConveyorBlockEntityMixin extends KineticBlockEntity
        implements TransformableBlockEntity, ChainGTMaterialInterface {
    @Unique
    public Map<BlockPos, Material> tfg$connectionMaterialStats = new HashMap<>();

    @Shadow
    BlockPos chainDestroyedEffectToSend;

    public ChainConveyorBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    @Unique
    public void addConnectionMaterial(BlockPos connection, Material chainMat) {
        BlockPos localTarget = connection.subtract(worldPosition);
        tfg$connectionMaterialStats.put(localTarget, chainMat);
    }

    @Override
    @Unique
    public Material getConnectionMaterial(BlockPos connection) {
        return tfg$connectionMaterialStats.get(connection);
    }

    @Override
    @Unique
    public Item getConnectionChainItem(BlockPos connection) {
        Material chainMat = tfg$connectionMaterialStats.get(connection);
        return ChemicalHelper.get(TagPrefix.get("chain"), (chainMat != null) ? chainMat : GTMaterials.Copper).getItem();
    }

    @Inject(method = "removeConnectionTo(Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;notifyUpdate()V"), remap = false)
    private void tfg$removeConnectionTo(BlockPos target, CallbackInfoReturnable<Boolean> cir) {
        BlockPos localTarget = target.subtract(worldPosition);
        tfg$connectionMaterialStats.remove(localTarget);
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Z)V", at = @At("TAIL"), remap = false)
    private void tfg$write(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        compound.put("ChainMaterials", NBTHelper.writeCompoundList(tfg$connectionMaterialStats.entrySet(), entry -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("Target", NbtUtils.writeBlockPos(entry.getKey()));
            compoundTag.putString("Material", entry.getValue().getName());
            return compoundTag;
        }));
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Z)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/chainConveyor/ChainConveyorBlockEntity;updateBoxWorldPositions()V"), remap = false)
    private void tfg$read(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        tfg$connectionMaterialStats.clear();
        NBTHelper.iterateCompoundList(compound.getList("ChainMaterials", Tag.TAG_COMPOUND),
                c -> tfg$connectionMaterialStats.put(NbtUtils.readBlockPos(c.getCompound("Target")),
                        GTMaterials.get(c.getString("Material"))));
        // Debug print
        //        for (BlockPos pos : tfg$connectionMaterialStats.keySet())
        //        {
        //            String matName = tfg$connectionMaterialStats.get(pos).getName();
        //            System.out.println("This map lives in entity at: " + this.getBlockPos());
        //            System.out.println("Pos: " + pos.toString() + "; Material: " + matName);
        //        }
    }

    @Inject(method = "chainDestroyed(Lnet/minecraft/core/BlockPos;ZZ)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$chainDestroyed(BlockPos target, boolean spawnDrops, boolean sendEffect, CallbackInfo ci) {
        int chainCount = ChainConveyorBlockEntity.getChainCost(target);
        if (sendEffect) {
            chainDestroyedEffectToSend = target;
            sendData();
        }
        if (!spawnDrops) {
            ci.cancel();
            return;
        }
        Item chainItem = getConnectionChainItem(target);
        ChainConveyorBlockEntity be = ((ChainConveyorBlockEntity) (Object) this);
        if (level != null && !be.forPointsAlongChains(target, chainCount,
                vec -> level.addFreshEntity(new ItemEntity(level, vec.x, vec.y, vec.z, new ItemStack(chainItem))))) {
            while (chainCount > 0) {
                Block.popResource(level, worldPosition, new ItemStack(chainItem, Math.min(chainCount, 64)));
                chainCount -= 64;
            }
        }
        ci.cancel();
    }
}
