package su.terrafirmagreg.core.compat.kjs;

import com.notenoughmail.kubejs_tfc.util.ResourceUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;

import su.terrafirmagreg.core.common.data.blocks.SandLayerBlock;

public class LayerBlockBuilder extends BlockBuilder {

    public LayerBlockBuilder(ResourceLocation i) {
        super(i);

        noCollision = false;
        hardness = 0.2f;
        fullBlock = false;
        opaque = true;
        soundType = SoundType.SAND;
        notSolid = true;
        viewBlocking = false;
        suffocating = false;

        mapColor(MapColor.NONE);
        tagBlock(ResourceLocation.fromNamespaceAndPath("minecraft", "mineable/shovel"));
    }

    @Override
    public SandLayerBlock createObject() {
        return new SandLayerBlock(createProperties());
    }

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        ResourceUtils.lootTable(b -> b.addPool(p -> {
        }), generator, this);
    }
}
