package su.terrafirmagreg.core.common.data.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.dries007.tfc.client.TFCSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;

/**
 * Defines Artisan Table Recipe Types.
 */
public class ArtisanType {

    @Getter
    private final ResourceLocation id;
    @Getter
    private final ArrayList<ItemStack> inputItems;
    @Getter
    private final ArrayList<TagKey<Item>> toolTags;
    @Getter
    private final ResourceLocation activeTexture;
    @Getter
    @Nullable
    private final ResourceLocation inactiveTexture;
    @Getter
    @Nullable
    private final ResourceLocation borderTexture;
    @Getter
    private final SoundEvent clickSound;
    @Getter
    private final float clickVolume;
    @Getter
    private final float clickPitch;

    private static final String TEXTURE_PREFIX = "textures/gui/artisan_table/";

    /**
     * Constructs a new ArtisanType.
     *
     * @param name The name for this recipe type.
     * @param inputItemA The primary input item.
     * @param inputItemB The secondary input item (nullable).
     * @param toolA The primary tool tag required.
     * @param toolB The secondary tool tag required.
     * @param activeTexture The texture when active.
     * @param inactiveTexture The texture when inactive (nullable).
     * @param clickSound The sound event played on click.
     * @param borderTexture The border texture (nullable).
     * @param clickVolume The volume of the click sound.
     * @param clickPitch The pitch of the click sound.
     */
    public ArtisanType(String name, ItemStack inputItemA, @Nullable ItemStack inputItemB, TagKey<Item> toolA, TagKey<Item> toolB, ResourceLocation activeTexture,
            @Nullable ResourceLocation inactiveTexture, SoundEvent clickSound, @Nullable ResourceLocation borderTexture, float clickVolume, float clickPitch) {
        this.id = TFGCore.id(name);
        inputItems = new ArrayList<>(Stream.of(inputItemA, inputItemB).filter(Objects::nonNull).toList());
        toolTags = new ArrayList<>(Arrays.asList(toolA, toolB));
        this.activeTexture = activeTexture;
        this.inactiveTexture = inactiveTexture;
        this.clickSound = clickSound;
        this.borderTexture = borderTexture;
        this.clickVolume = clickVolume;
        this.clickPitch = clickPitch;
    }

    /**
     * Returns a ResourceLocation for a texture with the given name.
     * @param name The texture name.
     * @return The ResourceLocation for the texture.
     */
    private static ResourceLocation textureLocation(String name) {
        if (!name.endsWith(".png"))
            name += ".png";
        return TFGCore.id(TEXTURE_PREFIX + name);
    }

    public static HashMap<ResourceLocation, ArtisanType> ARTISAN_TYPES = new HashMap<>();

    public static final ArtisanType CASTING_MOLD = new ArtisanType(
            "casting_mold",
            GTItems.SHAPE_EMPTY.get().getDefaultInstance(),
            ItemStack.EMPTY,
            CustomTags.HAMMERS,
            CustomTags.MALLETS,
            textureLocation("casting_mold_active"),
            textureLocation("casting_mold_inactive"),
            TFCSounds.ANVIL_HIT.get(),
            textureLocation("casting_mold_border"),
            0.1f,
            2.0f);
    public static final ArtisanType EXTRUDER_MOLD = new ArtisanType(
            "extruder_mold",
            GTItems.SHAPE_EMPTY.get().getDefaultInstance(),
            ItemStack.EMPTY,
            CustomTags.WIRE_CUTTERS,
            CustomTags.FILES,
            textureLocation("extruder_mold_active"),
            null,
            GTSoundEntries.WIRECUTTER_TOOL.getMainEvent(),
            textureLocation("extruder_mold_border"),
            0.5f,
            0.7f);
    public static final ArtisanType RESIN_BOARD = new ArtisanType(
            "resin_board",
            GTItems.COATED_BOARD.get().getDefaultInstance(),
            new ItemStack(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Copper).getItem(), 8),
            CustomTags.SCREWDRIVERS,
            CustomTags.WIRE_CUTTERS,
            textureLocation("blank_resin_board"),
            textureLocation("printed_resin_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("resin_board_border"),
            0.8f,
            2.0f);
    public static final ArtisanType RESIN_BOARD_FOUR = new ArtisanType(
            "resin_board_4x",
            new ItemStack(GTItems.COATED_BOARD.get().getDefaultInstance().getItem(), 4),
            new ItemStack(ChemicalHelper.get(TagPrefix.wireGtQuadruple, GTMaterials.Copper).getItem(), 8),
            CustomTags.SCREWDRIVERS,
            CustomTags.WIRE_CUTTERS,
            textureLocation("blank_resin_board"),
            textureLocation("printed_resin_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("resin_board_border_4x"),
            0.8f,
            2.0f);
    public static final ArtisanType PHENOLIC_BOARD = new ArtisanType(
            "phenolic_board",
            GTItems.PHENOLIC_BOARD.get().getDefaultInstance(),
            new ItemStack(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Silver).getItem(), 8),
            CustomTags.SCREWDRIVERS,
            CustomTags.WIRE_CUTTERS,
            textureLocation("blank_phenolic_board"),
            textureLocation("printed_phenolic_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("phenolic_board_border"),
            0.8f,
            2.0f);
    public static final ArtisanType PHENOLIC_BOARD_FOUR = new ArtisanType(
            "phenolic_board_4x",
            new ItemStack(GTItems.PHENOLIC_BOARD.get().getDefaultInstance().getItem(), 4),
            new ItemStack(ChemicalHelper.get(TagPrefix.wireGtQuadruple, GTMaterials.Silver).getItem(), 8),
            CustomTags.SCREWDRIVERS,
            CustomTags.WIRE_CUTTERS,
            textureLocation("blank_phenolic_board"),
            textureLocation("printed_phenolic_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("phenolic_board_border_4x"),
            0.8f,
            2.0f);

    /**
     * Registers a new ArtisanType in the ARTISAN_TYPES map.
     * @param type The ArtisanType to register.
     */
    private static void initNewType(ArtisanType type) {
        ARTISAN_TYPES.put(type.id, type);
    }

    static {
        initNewType(CASTING_MOLD);
        initNewType(EXTRUDER_MOLD);
        initNewType(RESIN_BOARD);
        initNewType(RESIN_BOARD_FOUR);
        initNewType(PHENOLIC_BOARD);
        initNewType(PHENOLIC_BOARD_FOUR);
    }
}
