package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.world.feature.tree.StackedTreeConfig;
import net.dries007.tfc.world.feature.tree.TreeHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Mostly a copy of AFC's NoTrunkStackedTreeFeature
 * https://github.com/Therighthon/ArborFirmaCraft/blob/1.20/src/main/java/com/therighthon/afc/common/NoTrunkStackedTreeFeature.java
 */

public class OffsetStackedTreeFeature extends Feature<OffsetStackedTreeConfig> {

    public OffsetStackedTreeFeature(Codec<OffsetStackedTreeConfig> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OffsetStackedTreeConfig> context) {

        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final RandomSource random = context.random();
        final OffsetStackedTreeConfig config = context.config();
        final ChunkPos chunkPos = new ChunkPos(pos);
        final BlockPos.MutableBlockPos mutablePos = (new BlockPos.MutableBlockPos()).set(pos);
        final StructureTemplateManager manager = TreeHelpers.getStructureManager(level);
        final StructurePlaceSettings settings = TreeHelpers.getPlacementSettings(level, chunkPos, random);

        if (TreeHelpers.isValidGround(level, pos, settings, config.placement())) {
            final boolean placeTree = config.rootSystem().map(
                    roots -> TreeHelpers.placeRoots(level, pos.below(), roots, random)
                            || !roots.required())
                    .orElse(true);

            if (placeTree) {
                config.rootSystem().ifPresent(roots -> TreeHelpers.placeRoots(level, pos.below(), roots, random));

                mutablePos.move(0, config.yOffset(), 0);

                for (StackedTreeConfig.Layer layer : config.layers()) {
                    // Place each layer
                    int layerCount = layer.getCount(random);

                    for (int i = 0; i < layerCount; i++) {
                        final ResourceLocation structureId = layer.templates().get(random.nextInt(layer.templates().size()));
                        final StructureTemplate structure = manager.getOrCreate(structureId);
                        TreeHelpers.placeTemplate(structure, settings, level, mutablePos.subtract(TreeHelpers.transformCenter(structure.getSize(), settings)));
                        mutablePos.move(0, structure.getSize().getY(), 0);
                    }
                }

                return true;
            }
        }

        return false;
    }
}
