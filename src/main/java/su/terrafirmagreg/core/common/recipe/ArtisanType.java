package su.terrafirmagreg.core.common.recipe;

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
import com.simibubi.create.AllTags;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;

/**
 * Defines Artisan Table Recipe Types.
 */
public class ArtisanType {

    /**
     * Wrapper class for ingredients that can be either an ItemStack or a TagKey<Item>.
     */
    public static class Ingredient {
        @Nullable
        private final ItemStack itemStack;
        @Nullable
        private final TagKey<Item> tag;

        private Ingredient(@Nullable ItemStack itemStack, @Nullable TagKey<Item> tag) {
            if ((itemStack == null) == (tag == null)) {
                throw new IllegalArgumentException("Ingredient must have exactly one of ItemStack or Tag");
            }
            this.itemStack = itemStack;
            this.tag = tag;
        }

        public static Ingredient of(ItemStack itemStack) {
            return new Ingredient(itemStack, null);
        }

        public static Ingredient of(TagKey<Item> tag) {
            return new Ingredient(null, tag);
        }

        public boolean isItemStack() {
            return itemStack != null;
        }

        public boolean isTag() {
            return tag != null;
        }

        @Nullable
        public ItemStack getItemStack() {
            return itemStack;
        }

        @Nullable
        public TagKey<Item> getTag() {
            return tag;
        }
    }

    @Getter
    private final ResourceLocation id;
    @Getter
    private final ArrayList<Ingredient> inputIngredients;
    @Getter
    private final ArrayList<Ingredient> toolRequirements;
    @Getter
    @Nullable
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
     * @param inputA The primary input.
     * @param inputB The secondary input (nullable).
     * @param toolA The primary tool required.
     * @param toolB The secondary tool required.
     * @param activeTexture The texture when active (nullable).
     * @param inactiveTexture The texture when inactive (nullable).
     * @param clickSound The sound event played on click.
     * @param borderTexture The border texture (nullable).
     * @param clickVolume The volume of the click sound.
     * @param clickPitch The pitch of the click sound.
     */
    public ArtisanType(String name, Ingredient inputA, @Nullable Ingredient inputB,
            Ingredient toolA, Ingredient toolB, @Nullable ResourceLocation activeTexture,
            @Nullable ResourceLocation inactiveTexture, SoundEvent clickSound,
            @Nullable ResourceLocation borderTexture, float clickVolume, float clickPitch) {
        this.id = TFGCore.id(name);
        this.inputIngredients = new ArrayList<>(Stream.of(inputA, inputB).filter(Objects::nonNull).toList());
        this.toolRequirements = new ArrayList<>(Arrays.asList(toolA, toolB));
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
            Ingredient.of(GTItems.SHAPE_EMPTY.get().getDefaultInstance()),
            null,
            Ingredient.of(CustomTags.HAMMERS),
            Ingredient.of(CustomTags.MALLETS),
            textureLocation("casting_mold_active"),
            textureLocation("casting_mold_inactive"),
            TFCSounds.ANVIL_HIT.get(),
            textureLocation("casting_mold_border"),
            0.1f,
            2.0f);
    public static final ArtisanType EXTRUDER_MOLD = new ArtisanType(
            "extruder_mold",
            Ingredient.of(GTItems.SHAPE_EMPTY.get().getDefaultInstance()),
            null,
            Ingredient.of(CustomTags.WIRE_CUTTERS),
            Ingredient.of(CustomTags.FILES),
            textureLocation("extruder_mold_active"),
            null,
            GTSoundEntries.WIRECUTTER_TOOL.getMainEvent(),
            textureLocation("extruder_mold_border"),
            0.5f,
            0.7f);
    public static final ArtisanType RESIN_BOARD = new ArtisanType(
            "resin_board",
            Ingredient.of(GTItems.COATED_BOARD.get().getDefaultInstance()),
            Ingredient.of(new ItemStack(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Copper).getItem(), 8)),
            Ingredient.of(CustomTags.SCREWDRIVERS),
            Ingredient.of(CustomTags.WIRE_CUTTERS),
            textureLocation("blank_resin_board"),
            textureLocation("printed_resin_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("resin_board_border"),
            0.8f,
            2.0f);
    public static final ArtisanType RESIN_BOARD_FOUR = new ArtisanType(
            "resin_board_4x",
            Ingredient.of(new ItemStack(GTItems.COATED_BOARD.get().getDefaultInstance().getItem(), 4)),
            Ingredient.of(new ItemStack(ChemicalHelper.get(TagPrefix.wireGtQuadruple, GTMaterials.Copper).getItem(), 8)),
            Ingredient.of(CustomTags.SCREWDRIVERS),
            Ingredient.of(CustomTags.WIRE_CUTTERS),
            textureLocation("blank_resin_board"),
            textureLocation("printed_resin_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("resin_board_border_4x"),
            0.8f,
            2.0f);
    public static final ArtisanType PHENOLIC_BOARD = new ArtisanType(
            "phenolic_board",
            Ingredient.of(GTItems.PHENOLIC_BOARD.get().getDefaultInstance()),
            Ingredient.of(new ItemStack(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Silver).getItem(), 8)),
            Ingredient.of(CustomTags.SCREWDRIVERS),
            Ingredient.of(CustomTags.WIRE_CUTTERS),
            textureLocation("blank_phenolic_board"),
            textureLocation("printed_phenolic_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("phenolic_board_border"),
            0.8f,
            2.0f);
    public static final ArtisanType PHENOLIC_BOARD_FOUR = new ArtisanType(
            "phenolic_board_4x",
            Ingredient.of(new ItemStack(GTItems.PHENOLIC_BOARD.get().getDefaultInstance().getItem(), 4)),
            Ingredient.of(new ItemStack(ChemicalHelper.get(TagPrefix.wireGtQuadruple, GTMaterials.Silver).getItem(), 8)),
            Ingredient.of(CustomTags.SCREWDRIVERS),
            Ingredient.of(CustomTags.WIRE_CUTTERS),
            textureLocation("blank_phenolic_board"),
            textureLocation("printed_phenolic_board"),
            GTSoundEntries.WRENCH_TOOL.getMainEvent(),
            textureLocation("phenolic_board_border_4x"),
            0.8f,
            2.0f);

    public static final ArtisanType GLASS_LENS = new ArtisanType(
            "optical_borosilicate",
            Ingredient.of(new ItemStack(Objects.requireNonNullElse(ForgeRegistries.ITEMS.getValue(TFGCore.id("optical_borosilicate_blank")), Items.AIR))),
            Ingredient.of(new ItemStack(TFCItems.POWDERS.get(Powder.FLUX).get(), 8)),
            Ingredient.of(AllTags.AllItemTags.SANDPAPER.tag),
            Ingredient.of(new ItemStack(TFCItems.GEM_SAW.get(), 1)),
            null,
            textureLocation("glass_chipped"),
            SoundEvents.GLASS_BREAK,
            textureLocation("glass_border"),
            0.5f,
            2.0f);

    public static final ArtisanType ROAD_MARKING_STENCIL = new ArtisanType(
            "road_marking_stencil",
            Ingredient.of(ChemicalHelper.get(TagPrefix.plate, GTMaterials.TreatedWood, 1)),
            null,
            Ingredient.of(CustomTags.KNIVES),
            Ingredient.of(CustomTags.SAWS),
            textureLocation("blank_phenolic_board"),
            null,
            GTSoundEntries.SAW_TOOL.getMainEvent(),
            textureLocation("phenolic_board_border"),
            0.5f,
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
        initNewType(GLASS_LENS);
        initNewType(ROAD_MARKING_STENCIL);
    }
}
