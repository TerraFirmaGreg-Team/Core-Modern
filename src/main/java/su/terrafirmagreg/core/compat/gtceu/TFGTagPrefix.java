package su.terrafirmagreg.core.compat.gtceu;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import su.terrafirmagreg.core.TFGCore;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.oreTagPrefix;

public final class TFGTagPrefix {

    /* Stone Types */
    public static final TagPrefix oreGabbro;
    public static final TagPrefix oreShale;
    public static final TagPrefix oreClaystone;
    public static final TagPrefix oreLimestone;
    public static final TagPrefix oreConglomerate;
    public static final TagPrefix oreDolomite;
    public static final TagPrefix oreChert;
    public static final TagPrefix oreRhyolite;
    public static final TagPrefix oreDacite;
    public static final TagPrefix oreQuartzite;
    public static final TagPrefix oreSlate;
    public static final TagPrefix orePhyllite;
    public static final TagPrefix oreSchist;
    public static final TagPrefix oreGneiss;
    public static final TagPrefix oreMarble;
    public static final TagPrefix oreBasalt;
    public static final TagPrefix oreDiorite;
    public static final TagPrefix oreAndesite;
    public static final TagPrefix oreGranite;
    public static final TagPrefix oreChalk;

    static {
        // Делаем все в статическом конструкторе
        // Для начала удаляем греговское дерьмо
        // После добавляем наши вкусные стоунтайпы

        // TagPrefix.ORES.remove(TagPrefix.rawOreBlock);

//        TagPrefix.ORES.remove(TagPrefix.ore);
//        TagPrefix.ORES.remove(TagPrefix.oreDeepslate);
//        TagPrefix.ORES.remove(TagPrefix.oreTuff);
//        TagPrefix.ORES.remove(TagPrefix.oreSand);
//        TagPrefix.ORES.remove(TagPrefix.oreRedSand);
//        TagPrefix.ORES.remove(TagPrefix.oreRedGranite);
//        TagPrefix.ORES.remove(TagPrefix.oreGravel);
//        TagPrefix.ORES.remove(TagPrefix.oreEndstone);
//
//        TagPrefix.ORES.remove(TagPrefix.oreBasalt);
//        TagPrefix.ORES.remove(TagPrefix.oreAndesite);
//        TagPrefix.ORES.remove(TagPrefix.oreDiorite);
//        TagPrefix.ORES.remove(TagPrefix.oreGranite);

        /* Stone Types */
        oreGabbro = registerOreTagPrefix(Rock.GABBRO);
        oreShale = registerOreTagPrefix(Rock.SHALE);
        oreClaystone = registerOreTagPrefix(Rock.CLAYSTONE);
        oreLimestone = registerOreTagPrefix(Rock.LIMESTONE);
        oreConglomerate = registerOreTagPrefix(Rock.CONGLOMERATE);
        oreDolomite = registerOreTagPrefix(Rock.DOLOMITE);
        oreChert = registerOreTagPrefix(Rock.CHERT);
        oreRhyolite = registerOreTagPrefix(Rock.RHYOLITE);
        oreDacite = registerOreTagPrefix(Rock.DACITE);
        oreQuartzite = registerOreTagPrefix(Rock.QUARTZITE);
        oreSlate = registerOreTagPrefix(Rock.SLATE);
        orePhyllite = registerOreTagPrefix(Rock.PHYLLITE);
        oreSchist = registerOreTagPrefix(Rock.SCHIST);
        oreGneiss = registerOreTagPrefix(Rock.GNEISS);
        oreMarble = registerOreTagPrefix(Rock.MARBLE);
        oreBasalt = registerOreTagPrefix(Rock.BASALT);
        oreDiorite = registerOreTagPrefix(Rock.DIORITE);
        oreAndesite = registerOreTagPrefix(Rock.ANDESITE);
        oreGranite = registerOreTagPrefix(Rock.GRANITE);
        oreChalk = registerOreTagPrefix(Rock.CHALK);
    }

    private static TagPrefix registerOreTagPrefix(Rock rockType) {
//        var material = GTCEuAPI.materialManager.getMaterial(TFGCore.MOD_ID + ":" + rockType.getSerializedName());
//        if (material == null) {
//            material = GTCEuAPI.materialManager.getMaterial(rockType.getSerializedName());
//        }
//
//        if (material == null) throw new IllegalArgumentException("Bad material in ore generation for: " + rockType.getSerializedName());
//
//        final Material fMaterial = material;

        final var staticMaterial = GTMaterials.Copper;

        var tag = oreTagPrefix(rockType.getSerializedName(), BlockTags.MINEABLE_WITH_PICKAXE)
                .registerOre(
                        () -> TFCBlocks.ROCK_BLOCKS.get(rockType).get(Rock.BlockType.RAW).orElse(Blocks.STONE).defaultBlockState(),
                        () -> staticMaterial,
                        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F),
                        new ResourceLocation(TerraFirmaCraft.MOD_ID, "block/rock/raw/" + rockType.getSerializedName())
                );
        tag.addSecondaryMaterial(new MaterialStack(staticMaterial, GTValues.M));

        return tag;
    }

    public static void init() {}
}