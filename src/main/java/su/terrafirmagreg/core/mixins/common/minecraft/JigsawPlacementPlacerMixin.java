package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
@Debug(export = true)
public abstract class JigsawPlacementPlacerMixin {

	// TODO: part of gamestar's unfinished mineshaft code? not sure what this is for -Py

    @Final
    @Shadow
    private RandomSource random;

    @Final
    @Shadow
    private int maxDepth;

    @Final
    @Shadow
    Deque<Object> placing;

    @Inject(method = "tryPlacingChildren", at = @At(value = "INVOKE", target = "java/util/List.add (Ljava/lang/Object;)Z"))
    private void tfg$forceFeaturePlacement(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth, boolean useExpansionHack, LevelHeightAccessor level, RandomState randomState,
            CallbackInfo ci, @Local(name = "list") List<StructurePoolElement> list, @Local(name = "holder") Holder<StructureTemplatePool> holder) {

        System.out.println(piece.getElement());
        if (depth + 1 <= this.maxDepth) {
        }

        /*if (piece.getElement() instanceof FeaturePoolElement element) {
            System.out.println("is a feature piece" + element);
            System.out.println(depth);
        }
        if (depth == maxDepth) {
            if (holder.value().getShuffledTemplates(random).get(0) instanceof FeaturePoolElement) {
                list.addAll(holder.value().getShuffledTemplates(random));
            }
        }*/
    }

}
