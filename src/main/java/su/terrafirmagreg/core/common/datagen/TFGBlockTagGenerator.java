package su.terrafirmagreg.core.common.datagen;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.alekiponi.firmaciv.util.FirmacivTags;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_FirmaCiv;

public class TFGBlockTagGenerator extends BlockTagsProvider {
    public TFGBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TFGCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        TFGBlocks_FirmaCiv.CANOE_COMPONENT_BLOCKS.forEach(((watercraftMaterial, canoeComponentBlockBlockEntry) -> {
            tag(FirmacivTags.Blocks.CAN_MAKE_CANOE)
                    .addOptional(getResourceLocation(watercraftMaterial.getWood().getBlock(Wood.BlockType.STRIPPED_LOG).get()));
            tag(FirmacivTags.Blocks.CAN_MAKE_CANOE_UNRESTRICTED)
                    .addOptional(getResourceLocation(watercraftMaterial.getWood().getBlock(Wood.BlockType.STRIPPED_LOG).get()));
        }));
    }

    private ResourceLocation getResourceLocation(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }
}
