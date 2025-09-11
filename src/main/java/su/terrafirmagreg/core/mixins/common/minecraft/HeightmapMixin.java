package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = Heightmap.Types.class, remap = true)
public class HeightmapMixin {

    // This heightmap takes things that block collision into account, including stuff like mushroom caps.
    // This mixin lets us blacklist things with a tag, so then other features that use the heightmap
    // (mostly tfc's forest feature groundcover) can place them underneath.

    @Shadow
    @Final
    @Mutable
    private Predicate<BlockState> isOpaque;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void inject$init(String key, int usage, String opaque, Heightmap.Usage usageType,
            Predicate<BlockState> predicate, CallbackInfo ci) {
        if (key.equals("OCEAN_FLOOR")) {
            final Predicate<BlockState> finalPredicate = predicate;
            isOpaque = state -> finalPredicate.test(state) && !state.is(TFGTags.Blocks.HeightmapIgnore);
        }
    }
}
