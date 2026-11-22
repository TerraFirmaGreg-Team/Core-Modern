package su.terrafirmagreg.core.common.datagen;

import com.alekiponi.firmaciv.util.FirmacivTags;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_FirmaCiv;

public class TFGItemTagGenerator {

    public static void generateTags(RegistrateItemTagsProvider registrateItemTagsProvider) {
        TFGBlocks_FirmaCiv.WOODEN_BOAT_FRAME_FLAT.forEach((watercraftMaterial, firmacivFlatWoodenBoatFrameBlockBlockEntry) -> {
            //If we're not generating the planks block, add the tag here.
            //getDeckItem returns the deckBlock as an item, which is the Planks block.
            if (!watercraftMaterial.tfgWood.generateWood) {
                registrateItemTagsProvider.addTag(FirmacivTags.Items.HARD_WOOD)
                        .add(watercraftMaterial.getDeckItem());
            }
        });
    }
}
