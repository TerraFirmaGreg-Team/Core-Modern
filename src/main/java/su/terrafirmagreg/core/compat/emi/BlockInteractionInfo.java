package su.terrafirmagreg.core.compat.emi;

import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.simibubi.create.AllTags;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGTags;

public class BlockInteractionInfo {
    private static final Item pumice_item = TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.PUMICE).get().asItem();
    private static final Item incoloy_frame = ChemicalHelper.get(TagPrefix.frameGt, GTMaterials.IncoloyMA956).getItem();

    private static final Supplier<Item> glacian_frame = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "glacian_wool_frame")).asItem();
    private static final Supplier<Item> aes_frame = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "aes_insulation_frame")).asItem();
    private static final Supplier<Item> impure_moderate_frame = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "impure_moderate_core_frame")).asItem();
    private static final Supplier<Item> moderate_frame = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "moderate_core_frame")).asItem();

    private static final Supplier<Item> glacian_wool = () -> ForgeRegistries.ITEMS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "glacian_wool")).asItem();
    private static final Supplier<Item> aes_roll = () -> ForgeRegistries.ITEMS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "aes_insulation_roll")).asItem();
    private static final Supplier<Item> impure_moderator = () -> ForgeRegistries.ITEMS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "impure_graphite_moderator")).asItem();
    private static final Supplier<Item> moderator = () -> ForgeRegistries.ITEMS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "graphite_moderator")).asItem();

    public static BlockInteractionRecipe[] RECIPES = {
            //Brick -> cracked
            new BlockInteractionRecipe("cracked_bricks", TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONCRACKEDBRICK, CustomTags.HAMMERS),
            new BlockInteractionRecipe("cracked_brick_stairs", TFGTags.Items.INTERACTIONBRICKSTAIR, TFGTags.Items.INTERACTIONCRACKEDSTAIR, CustomTags.HAMMERS),
            new BlockInteractionRecipe("cracked_brick_slabs", TFGTags.Items.INTERACTIONBRICKSLAB, TFGTags.Items.INTERACTIONCRACKEDSLAB, CustomTags.HAMMERS),
            new BlockInteractionRecipe("cracked_brick_walls", TFGTags.Items.INTERACTIONBRICKWALL, TFGTags.Items.INTERACTIONCRACKEDWALL, CustomTags.HAMMERS),
            //Brick -> Mossy
            new BlockInteractionRecipe("mossy_bricks", TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONMOSSYBRICK, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe("mossy_brick_stairs", TFGTags.Items.INTERACTIONBRICKSTAIR, TFGTags.Items.INTERACTIONMOSSYSTAIR, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe("mossy_brick_slabs", TFGTags.Items.INTERACTIONBRICKSLAB, TFGTags.Items.INTERACTIONMOSSYSLAB, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe("mossy_brick_walls", TFGTags.Items.INTERACTIONBRICKWALL, TFGTags.Items.INTERACTIONMOSSYWALL, TFCTags.Items.COMPOST_GREENS_LOW),
            //Mossy -> cracked
            new BlockInteractionRecipe("mossy_to_cracked_bricks", TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONCRACKEDBRICK, CustomTags.HAMMERS),
            new BlockInteractionRecipe("mossy_to_cracked_stairs", TFGTags.Items.INTERACTIONMOSSYSTAIR, TFGTags.Items.INTERACTIONCRACKEDSTAIR, CustomTags.HAMMERS),
            new BlockInteractionRecipe("mossy_to_cracked_slabs", TFGTags.Items.INTERACTIONMOSSYSLAB, TFGTags.Items.INTERACTIONCRACKEDSLAB, CustomTags.HAMMERS),
            new BlockInteractionRecipe("mossy_to_cracked_walls", TFGTags.Items.INTERACTIONMOSSYWALL, TFGTags.Items.INTERACTIONCRACKEDWALL, CustomTags.HAMMERS),
            //Mossy -> brick
            new BlockInteractionRecipe("mossy_to_normal_bricks", TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONBRICK, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_normal_stairs", TFGTags.Items.INTERACTIONMOSSYSTAIR, TFGTags.Items.INTERACTIONBRICKSTAIR, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_normal_slabs", TFGTags.Items.INTERACTIONMOSSYSLAB, TFGTags.Items.INTERACTIONBRICKSLAB, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_normal_walls", TFGTags.Items.INTERACTIONMOSSYWALL, TFGTags.Items.INTERACTIONBRICKWALL, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_normal_bricks_pumice", TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONBRICK, pumice_item),
            new BlockInteractionRecipe("mossy_to_normal_stairs_pumice", TFGTags.Items.INTERACTIONMOSSYSTAIR, TFGTags.Items.INTERACTIONBRICKSTAIR, pumice_item),
            new BlockInteractionRecipe("mossy_to_normal_slabs_pumice", TFGTags.Items.INTERACTIONMOSSYSLAB, TFGTags.Items.INTERACTIONBRICKSLAB, pumice_item),
            new BlockInteractionRecipe("mossy_to_normal_walls_pumice", TFGTags.Items.INTERACTIONMOSSYWALL, TFGTags.Items.INTERACTIONBRICKWALL, pumice_item),
            //cracked -> brick
            new BlockInteractionRecipe("cracked_to_normal_bricks", TFGTags.Items.INTERACTIONCRACKEDBRICK, TFGTags.Items.INTERACTIONBRICK, TFCItems.MORTAR.get()),
            new BlockInteractionRecipe("cracked_to_normal_stairs", TFGTags.Items.INTERACTIONCRACKEDSTAIR, TFGTags.Items.INTERACTIONBRICKSTAIR, TFCItems.MORTAR.get()),
            new BlockInteractionRecipe("cracked_to_normal_slabs", TFGTags.Items.INTERACTIONCRACKEDSLAB, TFGTags.Items.INTERACTIONBRICKSLAB, TFCItems.MORTAR.get()),
            new BlockInteractionRecipe("cracked_to_normal_walls", TFGTags.Items.INTERACTIONCRACKEDWALL, TFGTags.Items.INTERACTIONBRICKWALL, TFCItems.MORTAR.get()),
            //brick -> smooth
            new BlockInteractionRecipe("normal_to_smooth_bricks", TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONSMOOTHBRICK, AllTags.AllItemTags.SANDPAPER.tag),
            new BlockInteractionRecipe("mossy_to_smooth_bricks",TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONSMOOTHBRICK, AllTags.AllItemTags.SANDPAPER.tag),
            new BlockInteractionRecipe("cracked_to_normal_bricks", TFGTags.Items.INTERACTIONCRACKEDBRICK, TFGTags.Items.INTERACTIONSMOOTHBRICK, AllTags.AllItemTags.SANDPAPER.tag),
            //cobble -> mossy
            new BlockInteractionRecipe("cobble_to_mossy", TFGTags.Items.INTERACTIONCOBBLE, TFGTags.Items.INTERACTIONMOSSYCOBBLE, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe("cobble_to_mossy_stairs", TFGTags.Items.INTERACTIONCOBBLESTAIR, TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe("cobble_to_mossy_slabs", TFGTags.Items.INTERACTIONCOBBLESLAB, TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe("cobble_to_mossy_walls", TFGTags.Items.INTERACTIONCOBBLEWALL, TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, TFCTags.Items.COMPOST_GREENS_LOW),
            //mossy -> cobble
            new BlockInteractionRecipe("mossy_to_cobble", TFGTags.Items.INTERACTIONMOSSYCOBBLE, TFGTags.Items.INTERACTIONCOBBLE, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_cobble_stairs", TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, TFGTags.Items.INTERACTIONCOBBLESTAIR, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_cobble_slabs", TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, TFGTags.Items.INTERACTIONCOBBLESLAB, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_cobble_walls", TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, TFGTags.Items.INTERACTIONCOBBLEWALL, CustomTags.KNIVES),
            new BlockInteractionRecipe("mossy_to_cobble_pumice", TFGTags.Items.INTERACTIONMOSSYCOBBLE, TFGTags.Items.INTERACTIONCOBBLE, pumice_item),
            new BlockInteractionRecipe("mossy_to_cobble_stairs_pumice", TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, TFGTags.Items.INTERACTIONCOBBLESTAIR, pumice_item),
            new BlockInteractionRecipe("mossy_to_cobble_slabs_pumice", TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, TFGTags.Items.INTERACTIONCOBBLESLAB, pumice_item),
            new BlockInteractionRecipe("mossy_to_cobble_walls_pumice", TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, TFGTags.Items.INTERACTIONCOBBLEWALL, pumice_item),

            //Insulation Add
            new BlockInteractionRecipe("insulation_aes", incoloy_frame, aes_frame.get(), aes_roll.get()),
            new BlockInteractionRecipe("insulation_glacian", incoloy_frame, glacian_frame.get(), new ItemStack(glacian_wool.get(), 2)),
            new BlockInteractionRecipe("insulation_impure_moderate", incoloy_frame, impure_moderate_frame.get(), impure_moderator.get()),
            new BlockInteractionRecipe("insulation_moderate", incoloy_frame, moderate_frame.get(), moderator.get()),

            //Insulation Remove
            new BlockInteractionRecipe("insulation_aes_remove", aes_frame.get(), incoloy_frame, CustomTags.WIRE_CUTTERS),
            new BlockInteractionRecipe("insulation_glacian_remove", glacian_frame.get(), incoloy_frame, CustomTags.WIRE_CUTTERS),
            new BlockInteractionRecipe("insulation_impure_moderate_remove", impure_moderate_frame.get(), incoloy_frame, CustomTags.WIRE_CUTTERS),
            new BlockInteractionRecipe("insulation_moderate_remove", moderate_frame.get(), incoloy_frame, CustomTags.WIRE_CUTTERS),
    };

}
