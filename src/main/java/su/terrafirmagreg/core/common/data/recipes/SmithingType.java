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

public class SmithingType {

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

    private static final String TEXTURE_PREFIX = "textures/gui/smithing/";

    //This is cursed

    public SmithingType(String name, ItemStack inputItemA, TagKey<Item> toolA, TagKey<Item> toolB, ResourceLocation activeTexture, ResourceLocation inactiveTexture, SoundEvent clickSound,
            ResourceLocation borderTexture) {
        this(name, inputItemA, ItemStack.EMPTY, toolA, toolB, activeTexture, inactiveTexture, clickSound, borderTexture);
    }

    public SmithingType(String name, ItemStack inputItemA, TagKey<Item> toolA, TagKey<Item> toolB, ResourceLocation activeTexture, SoundEvent clickSound, ResourceLocation borderTexture) {
        this(name, inputItemA, ItemStack.EMPTY, toolA, toolB, activeTexture, null, clickSound, borderTexture);
    }

    public SmithingType(String name, ItemStack inputItemA, ItemStack inputItemB, TagKey<Item> toolA, TagKey<Item> toolB, ResourceLocation activeTexture, ResourceLocation inactiveTexture,
            SoundEvent clickSound) {
        this(name, inputItemA, inputItemB, toolA, toolB, activeTexture, inactiveTexture, clickSound, null);
    }

    public SmithingType(String name, ItemStack inputItemA, @Nullable ItemStack inputItemB, TagKey<Item> toolA, TagKey<Item> toolB, ResourceLocation activeTexture,
            @Nullable ResourceLocation inactiveTexture, SoundEvent clickSound, @Nullable ResourceLocation borderTexture) {
        this.id = TFGCore.id(name);
        inputItems = new ArrayList<>(Stream.of(inputItemA, inputItemB).filter(Objects::nonNull).toList());
        toolTags = new ArrayList<>(Arrays.asList(toolA, toolB));
        this.activeTexture = activeTexture;
        this.inactiveTexture = inactiveTexture;
        this.clickSound = clickSound;
        this.borderTexture = borderTexture;
    }

    private static ResourceLocation textureLocation(String name) {
        if (!name.endsWith(".png"))
            name += ".png";
        return TFGCore.id(TEXTURE_PREFIX + name);
    }

    public static HashMap<ResourceLocation, SmithingType> SMITHING_TYPES = new HashMap<>();

    public static final SmithingType CASTING_MOLD = new SmithingType(
            "casting_mold",
            GTItems.SHAPE_EMPTY.get().getDefaultInstance(),
            CustomTags.HAMMERS,
            CustomTags.MALLETS,
            textureLocation("mold_active"),
            textureLocation("mold_inactive"),
            TFCSounds.ANVIL_HIT.get(),
            textureLocation("mold_border"));
    public static final SmithingType EXTRUDER_MOLD = new SmithingType(
            "extruder_mold",
            GTItems.SHAPE_EMPTY.get().getDefaultInstance(),
            CustomTags.WIRE_CUTTERS,
            CustomTags.FILES,
            textureLocation("mold_active"),
            GTSoundEntries.WIRECUTTER_TOOL.getMainEvent(),
            textureLocation("mold_border"));
    public static final SmithingType RESIN_BOARD = new SmithingType(
            "resin_board",
            GTItems.COATED_BOARD.get().getDefaultInstance(),
            new ItemStack(ChemicalHelper.get(TagPrefix.wireGtSingle, GTMaterials.Copper).getItem(), 9),
            CustomTags.SCREWDRIVERS,
            CustomTags.WIRE_CUTTERS,
            textureLocation("blank_resin_board"),
            textureLocation("printed_resin_board"),
            GTSoundEntries.ELECTROLYZER.getMainEvent());

    private static void initNewType(SmithingType type) {
        SMITHING_TYPES.put(type.id, type);
    }

    static {
        initNewType(CASTING_MOLD);
        initNewType(EXTRUDER_MOLD);
        initNewType(RESIN_BOARD);
    }
}
