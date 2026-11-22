package su.terrafirmagreg.core.common.datagen;

import com.alekiponi.firmaciv.util.FirmacivTags;
import com.tterrag.registrate.providers.RegistrateTagsProvider;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.world.level.block.Block;

import su.terrafirmagreg.core.common.data.TFGWatercraftMaterial;

public class TFGBlockTagGenerator {
    public static void generateTags(RegistrateTagsProvider.IntrinsicImpl<Block> blockIntrinsic) {
        var ginkgoStrippedLog = TFGWatercraftMaterial.GINKGO.getWood().getBlock(Wood.BlockType.STRIPPED_LOG).get();

        //We need to add these tags to the Ginkgo logs, otherwise making canoes out of them becomes impossible.
        blockIntrinsic.addTag(FirmacivTags.Blocks.CAN_MAKE_CANOE)
                .add(ginkgoStrippedLog);
        blockIntrinsic.addTag(FirmacivTags.Blocks.CAN_MAKE_CANOE_UNRESTRICTED)
                .add(ginkgoStrippedLog);
    }
}
