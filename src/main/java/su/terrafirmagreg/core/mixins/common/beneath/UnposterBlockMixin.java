package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.beneath.common.blocks.UnposterBlock;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.util.Helpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = UnposterBlock.class, remap = false)
public class UnposterBlockMixin {
    @Unique
    private static final TagKey<Block> tfg$TFCCropTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "crops"));

    // Makes the unposter grow anything from a new tag, instead of being hardcoded to mushrooms

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/Helpers;isBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/tags/TagKey;)Z"), remap = true)
    private boolean tfg$randomTick(BlockState state, TagKey<Block> tag) {
        return Helpers.isBlock(state, TFGTags.Blocks.UNPOSTER_GROWABLE) && !Helpers.isBlock(state, tfg$TFCCropTag);
    }
}
