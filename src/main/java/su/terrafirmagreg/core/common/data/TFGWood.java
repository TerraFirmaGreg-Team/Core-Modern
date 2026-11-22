package su.terrafirmagreg.core.common.data;

import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Wood;

public enum TFGWood implements RegistryWood {
    ARAUCARIA(MapColor.TERRACOTTA_WHITE, MapColor.WOOD, 10, 0, WoodKind.SOFT),
    BEECH(MapColor.TERRACOTTA_CYAN, MapColor.WOOD, 10, 0, WoodKind.HARD),
    MAHOE(MapColor.TERRACOTTA_BLUE, MapColor.WOOD, 10, 0, WoodKind.HARD),

    GLACIAN(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/glacian_planks"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/glacian_log"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/stripped_glacian_log"),
            MapColor.NONE, TFGWood::AdAstraGlacianSpecialBlockHandler, WoodKind.HARD),
    STROPHAR(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_planks"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_stem"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_stem"),
            MapColor.NONE, TFGWood::AdAstraStemSpecialBlockHandler, WoodKind.NONE),
    AERONOS(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_planks"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_stem"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_stem"),
            MapColor.NONE, TFGWood::AdAstraStemSpecialBlockHandler, WoodKind.NONE),
    GINKGO(ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/ginkgo_planks"),
            ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/ginkgo_log"),
            ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/stripped_ginkgo_log"),
            MapColor.NONE, TFGWood::WansSpecialBlockHandler, WoodKind.SOFT);

    public enum WoodKind {
        //Makes canoes
        SOFT,
        //Makes Rowboats and Sloops
        HARD,
        //Cant be used for boats
        NONE;
    }

    public static final TFGWood[] VALUES = values();

    public final boolean generateWood;
    public final String serializedName;
    public final MapColor woodColor;
    public final MapColor barkColor;
    @Nullable
    public final TFCTreeGrower tree;
    public final int daysToGrow;
    public final BlockSetType blockSet;
    public final WoodType woodType;
    public final int autumnIndex;
    public final ResourceLocation plankTexture;
    public final ResourceLocation logTexture;
    public final ResourceLocation strippedLogTexture;
    public final String baseModNamespace;
    public final WoodKind woodKind;
    @Nullable
    public final BiFunction<TFGWood, Wood.BlockType, Optional<Block>> blockTypeSpecialHandler;

    TFGWood(MapColor woodColor, MapColor barkColor, int daysToGrow, int autumnIndex, WoodKind woodKind) {
        this.generateWood = true;
        this.serializedName = this.name().toLowerCase(Locale.ROOT);
        this.woodColor = woodColor;
        this.barkColor = barkColor;
        this.tree = new TFCTreeGrower(ResourceLocation.fromNamespaceAndPath("tfc", "tree/" + this.serializedName),
                ResourceLocation.fromNamespaceAndPath("tfc", "tree/" + this.serializedName + "_large"));
        this.daysToGrow = daysToGrow;
        this.blockSet = new BlockSetType(serializedName);
        this.woodType = new WoodType(TFGCore.id(serializedName).toString(), blockSet);
        this.autumnIndex = autumnIndex;
        this.plankTexture = TFGCore.id("block/wood/planks/" + serializedName);
        this.logTexture = TFGCore.id("block/wood/log/" + serializedName);
        this.strippedLogTexture = TFGCore.id("block/wood/stripped_log/" + serializedName);
        this.baseModNamespace = "";
        this.woodKind = woodKind;
        this.blockTypeSpecialHandler = null;
    }

    TFGWood(ResourceLocation plank, ResourceLocation log, ResourceLocation stripped_log, MapColor mapColor, @Nullable BiFunction<TFGWood, Wood.BlockType, Optional<Block>> blockTypeSpecialHandler,
            WoodKind woodKind) {
        this.generateWood = false;
        this.serializedName = this.name().toLowerCase(Locale.ROOT);
        this.woodColor = mapColor;
        this.barkColor = mapColor;
        this.autumnIndex = 0;
        this.tree = null;
        this.daysToGrow = 0;
        this.blockSet = new BlockSetType(serializedName);
        this.woodType = new WoodType(TFGCore.id(serializedName).toString(), blockSet);
        this.plankTexture = plank;
        this.logTexture = log;
        this.strippedLogTexture = stripped_log;
        this.baseModNamespace = plank.getNamespace();
        this.woodKind = woodKind;
        this.blockTypeSpecialHandler = blockTypeSpecialHandler;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    @Override
    public MapColor woodColor() {
        return woodColor;
    }

    @Override
    public MapColor barkColor() {
        return barkColor;
    }

    @Override
    public Supplier<Block> getBlock(Wood.BlockType type) {
        return () -> {
            //If we generate the blocks ourselves we can get it directly. If not, make sure the type is within the map
            if (generateWood || TFGBlocks_Wood.WOODS.get(this).containsKey(type)) {
                return TFGBlocks_Wood.WOODS.get(this).get(type).get();
            } else { //We need to do a special handling for this.
                    //If we have a special block type handler, get try to get the block that way.
                if (blockTypeSpecialHandler != null) {
                    Optional<Block> block = blockTypeSpecialHandler.apply(this, type);
                    if (block.isPresent()) {
                        return block.get();
                    }
                }
                //Otherwise return it from the registry.
                return ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(baseModNamespace, getSerializedName() + "_" + type.name().toLowerCase()));
            }
        };
    }

    @Override
    public BlockSetType getBlockSet() {
        return blockSet;
    }

    @Override
    public WoodType getVanillaWoodType() {
        return woodType;
    }

    public TFCTreeGrower tree() {
        return tree;
    }

    public int daysToGrow() {
        return defaultDaysToGrow();
    }

    @Override
    public int autumnIndex() {
        return autumnIndex;
    }

    public int defaultDaysToGrow() {
        return daysToGrow;
    }

    public static void registerBlockSetTypes() {
        for (TFGWood wood : VALUES) {
            BlockSetType.register(wood.blockSet);
            WoodType.register(wood.woodType);
        }
    }

    private static Optional<Block> AdAstraStemSpecialBlockHandler(TFGWood wood, Wood.BlockType blockType) {
        Optional<Block> result = Optional.empty();
        switch (blockType) {
            case LOG, STRIPPED_LOG, WOOD, STRIPPED_WOOD ->
                result = Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(wood.baseModNamespace, wood.getSerializedName() + "_stem")));
        }
        return result;
    }

    private static Optional<Block> WansSpecialBlockHandler(TFGWood wood, Wood.BlockType blockType) {
        Optional<Block> result = Optional.empty();
        switch (blockType) {
            case STRIPPED_LOG ->
                result = Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(wood.baseModNamespace, "stripped_" + wood.getSerializedName() + "_log")));
            case STRIPPED_WOOD ->
                result = Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(wood.baseModNamespace, "stripped_" + wood.getSerializedName() + "_wood")));
        }
        return result;
    }

    private static Optional<Block> AdAstraGlacianSpecialBlockHandler(TFGWood wood, Wood.BlockType blockType) {
        BiFunction<String, String, Optional<Block>> getFromAdAstraOrKJS = (kjsPath, adAstraPath) -> {
            ResourceLocation kjsResourceLocation = ResourceLocation.fromNamespaceAndPath("tfg", kjsPath);
            ResourceLocation adAstraResourceLocation = ResourceLocation.fromNamespaceAndPath("ad_astra", adAstraPath);

            if (ForgeRegistries.BLOCKS.containsKey(adAstraResourceLocation)) {
                return Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(adAstraResourceLocation));
            } else if (ForgeRegistries.BLOCKS.containsKey(kjsResourceLocation)) {
                return Optional.ofNullable(ForgeRegistries.BLOCKS.getValue(kjsResourceLocation));
            }
            return Optional.empty();
        };

        Optional<Block> result = Optional.empty();
        switch (blockType) {
            case LOG -> result = getFromAdAstraOrKJS.apply("glacian_log", "glacian_log");
            case STRIPPED_LOG -> result = getFromAdAstraOrKJS.apply("glacian_log_stripped", "stripped_glacian_log");
            case WOOD -> result = getFromAdAstraOrKJS.apply("glacian_wood", "glacian_log");
            case STRIPPED_WOOD -> result = getFromAdAstraOrKJS.apply("glacian_wood_stripped", "stripped_glacian_log");
        }
        return result;
    }
}
