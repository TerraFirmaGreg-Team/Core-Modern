package su.terrafirmagreg.core.mixins.common.create.girders;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.trains.track.BezierConnection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import su.terrafirmagreg.core.common.block.girder.TFGGirderData;

/***
 * Credit: Create: More Girders
 */
@Mixin(value = BezierConnection.class)
public abstract class BezierConnectionMixin implements TFGGirderData {
    @Unique
    @Nullable
    private Block tfg$girderBlock;

    @Override
    @Nullable
    public Block tfg$getGirderBlock() {
        return tfg$girderBlock;
    }

    @Override
    public void tfg$setGirderBlock(@Nullable Block block) {
        tfg$girderBlock = block;
    }

    @Inject(method = "write(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"), remap = false)
    private void tfg$writeNbt(BlockPos pos, CallbackInfoReturnable<CompoundTag> cir) {
        if (tfg$girderBlock != null) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(tfg$girderBlock);
            cir.getReturnValue().putString("TFGGirder", key.toString());
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"), remap = false)
    private void tfg$readNbt(CompoundTag tag, BlockPos pos, CallbackInfo ci) {
        if (tag.contains("TFGGirder")) {
            ResourceLocation id = ResourceLocation.parse(tag.getString("TFGGirder"));
            BuiltInRegistries.BLOCK.getOptional(id).ifPresent(b -> tfg$girderBlock = b);
        }
    }
}
