package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.util.entry.BlockEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.dries007.tfc.common.blocks.wood.ToolRackBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ModelFile;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.utils.ModelUtils;

import java.util.Map;
import java.util.function.Supplier;

public class TFGBlocks_Wood {


    public enum WoodType {
        GLACIAN("glacian", ResourceLocation.fromNamespaceAndPath("ad_astra","glacian_planks"), MapColor.NONE),
        STROPHAR("strophar", ResourceLocation.fromNamespaceAndPath("ad_astra","strophar_planks"), MapColor.NONE),
        AERONOS("aeronos", ResourceLocation.fromNamespaceAndPath("ad_astra","aeronos_planks"), MapColor.NONE),
        GINKGO("ginkgo", ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts","ginkgo_planks"), MapColor.NONE);
        public final String name;
        public final ResourceLocation plankBlock;
        public final RegistryWood registryWood;

        WoodType(String name, ResourceLocation plankBlock, MapColor col) {
            this.name = name;
            this.plankBlock = plankBlock;

            // This is just needed for the TFC wood block ctors, only the colour method is used.
            this.registryWood = new RegistryWood() {
                @Override
                public MapColor woodColor() {
                    return col;
                }

                @Override
                public MapColor barkColor() {
                    return col;
                }

                @Override
                public TFCTreeGrower tree() {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public int daysToGrow() {
                    return 0;
                }

                @Override
                public int autumnIndex() {
                    return 0;
                }

                @Override
                public Supplier<Block> getBlock(Wood.BlockType blockType) {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public BlockSetType getBlockSet() {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public net.minecraft.world.level.block.state.properties.WoodType getVanillaWoodType() {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public String getSerializedName() {
                    return "";
                }
            };
        }
    }

    public static final Map<WoodType, Map<Wood.BlockType, BlockEntry<? extends Block>>> WOOD_BLOCKS = new Object2ObjectOpenHashMap<>();

    public static void init() {

    }

    private static void registerBlocks(WoodType woodType) {
        var blocks = WOOD_BLOCKS.computeIfAbsent(woodType, new Object2ObjectOpenHashMap<>());

        var woodName = woodType.name;
        var planksBlock = woodType.plankBlock;

        // Tool Rack
        var toolRackBlock = Wood.BlockType.TOOL_RACK.create(woodType.registryWood).get();
        var toolRack = TFGCore.REGISTRATE.block("wood/" + woodName + "_tool_rack", p -> toolRackBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent("wood/" + woodName + "_tool_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/tool_rack"))
                            .texture("texture", planksBlock)
                            .texture("particle", planksBlock);

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem()
                .register();

        blocks.put(Wood.BlockType.TOOL_RACK, toolRack);

    }

}
