package su.terrafirmagreg.core.common.datagen;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.alekiponi.alekiships.util.AlekiShipsTags;
import com.alekiponi.firmaciv.util.FirmacivTags;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGWatercraftMaterial;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_FirmaCiv;

public class TFGItemTagGenerator extends ItemTagsProvider {
    public TFGItemTagGenerator(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, TFGCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        TFGBlocks_FirmaCiv.WOODEN_BOAT_FRAME_FLAT.forEach((watercraftMaterial, firmacivFlatWoodenBoatFrameBlockBlockEntry) -> {
            tag(FirmacivTags.Items.HARD_WOOD)
                    .add(watercraftMaterial.getDeckItem());
        });

        for (var watercraftMaterial : TFGWatercraftMaterial.values()) {
            tag(AlekiShipsTags.Items.CRAFTING_TABLES)
                    .add(watercraftMaterial.getWood().getBlock(Wood.BlockType.WORKBENCH).get().asItem());
            tag(FirmacivTags.Items.CHESTS)
                    .add(watercraftMaterial.getWood().getBlock(Wood.BlockType.CHEST).get().asItem());
        }
    }
}
