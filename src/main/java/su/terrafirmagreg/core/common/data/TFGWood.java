package su.terrafirmagreg.core.common.data;

import java.util.Locale;
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
    ARAUCARIA(MapColor.TERRACOTTA_WHITE, MapColor.WOOD, 10, 0),
    BEECH(MapColor.TERRACOTTA_CYAN, MapColor.WOOD, 10, 0),
    MAHOE(MapColor.TERRACOTTA_BLUE, MapColor.WOOD, 10, 0),

    GLACIAN(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/glacian_planks"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/glacian_log"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/stripped_glacian_log"),
            "ad_astra",
            MapColor.NONE),
    STROPHAR(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_planks"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_stem"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_stem"),
            "ad_astra",
            MapColor.NONE),
    AERONOS(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_planks"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_stem"),
            ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_stem"),
            "ad_astra",
            MapColor.NONE),
    GINKGO(ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/ginkgo_planks"),
            ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/ginkgo_log"),
            ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/stripped_ginkgo_log"),
            "wan_ancient_beasts",
            MapColor.NONE);

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
    private final String modNamespace;

    TFGWood(MapColor woodColor, MapColor barkColor, int daysToGrow, int autumnIndex) {
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
        this.modNamespace = "";
    }

    TFGWood(ResourceLocation plankTexture, ResourceLocation logTexture, ResourceLocation stripped_logTexture, String modNamespace, MapColor mapColor) {
        this.generateWood = false;
        this.serializedName = this.name().toLowerCase(Locale.ROOT);
        this.woodColor = mapColor;
        this.barkColor = mapColor;
        this.autumnIndex = 0;
        this.tree = null;
        this.daysToGrow = 0;
        this.blockSet = new BlockSetType(serializedName);
        this.woodType = new WoodType(TFGCore.id(serializedName).toString(), blockSet);
        this.plankTexture = plankTexture;
        this.logTexture = logTexture;
        this.strippedLogTexture = stripped_logTexture;
        this.modNamespace = modNamespace;
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
            if (generateWood) {
                return TFGBlocks_Wood.WOODS.get(this).get(type).get();
            } else {
                //No way this works
                return ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(this.modNamespace, getSerializedName() + "_" + type.name().toLowerCase()));
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
}
