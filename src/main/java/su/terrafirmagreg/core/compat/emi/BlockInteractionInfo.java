package su.terrafirmagreg.core.compat.emi;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.simibubi.create.AllTags;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.GroundcoverBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.world.item.Item;

import su.terrafirmagreg.core.common.data.TFGTags;

public class BlockInteractionInfo {
    private static final Item pumice_item = TFCBlocks.GROUNDCOVER.get(GroundcoverBlockType.PUMICE).get().asItem();

    public static BlockInteractionRecipe[] RECIPES = {
            //Brick -> cracked
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONCRACKEDBRICK, CustomTags.HAMMERS),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKSTAIR, TFGTags.Items.INTERACTIONCRACKEDSTAIR, CustomTags.HAMMERS),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKSLAB, TFGTags.Items.INTERACTIONCRACKEDSLAB, CustomTags.HAMMERS),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKWALL, TFGTags.Items.INTERACTIONCRACKEDWALL, CustomTags.HAMMERS),
            //Brick -> Mossy
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONMOSSYBRICK, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKSTAIR, TFGTags.Items.INTERACTIONMOSSYSTAIR, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKSLAB, TFGTags.Items.INTERACTIONMOSSYSLAB, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKWALL, TFGTags.Items.INTERACTIONMOSSYWALL, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONMOSSYBRICK, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKSTAIR, TFGTags.Items.INTERACTIONMOSSYSTAIR, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKSLAB, TFGTags.Items.INTERACTIONMOSSYSLAB, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICKWALL, TFGTags.Items.INTERACTIONMOSSYWALL, TFCTags.Items.COMPOST_GREENS_LOW),
            //Mossy -> cracked
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONCRACKEDBRICK, CustomTags.HAMMERS),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYSTAIR, TFGTags.Items.INTERACTIONCRACKEDSTAIR, CustomTags.HAMMERS),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYSLAB, TFGTags.Items.INTERACTIONCRACKEDSLAB, CustomTags.HAMMERS),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYWALL, TFGTags.Items.INTERACTIONCRACKEDWALL, CustomTags.HAMMERS),
            //Mossy -> brick
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONBRICK, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYSTAIR, TFGTags.Items.INTERACTIONBRICKSTAIR, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYSLAB, TFGTags.Items.INTERACTIONBRICKSLAB, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYWALL, TFGTags.Items.INTERACTIONBRICKWALL, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONBRICK, pumice_item),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYSTAIR, TFGTags.Items.INTERACTIONBRICKSTAIR, pumice_item),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYSLAB, TFGTags.Items.INTERACTIONBRICKSLAB, pumice_item),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYWALL, TFGTags.Items.INTERACTIONBRICKWALL, pumice_item),
            //cracked -> brick
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCRACKEDBRICK, TFGTags.Items.INTERACTIONBRICK, TFCItems.MORTAR.get()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCRACKEDSTAIR, TFGTags.Items.INTERACTIONBRICKSTAIR, TFCItems.MORTAR.get()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCRACKEDSLAB, TFGTags.Items.INTERACTIONBRICKSLAB, TFCItems.MORTAR.get()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCRACKEDWALL, TFGTags.Items.INTERACTIONBRICKWALL, TFCItems.MORTAR.get()),
            //brick -> smooth
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONBRICK, TFGTags.Items.INTERACTIONSMOOTHBRICK, AllTags.AllItemTags.SANDPAPER.tag),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYBRICK, TFGTags.Items.INTERACTIONSMOOTHBRICK, AllTags.AllItemTags.SANDPAPER.tag),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCRACKEDBRICK, TFGTags.Items.INTERACTIONSMOOTHBRICK, AllTags.AllItemTags.SANDPAPER.tag),
            //smooth -> brick
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONSMOOTHBRICK, TFGTags.Items.INTERACTIONBRICK, CustomTags.FILES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONSMOOTHBRICK, TFGTags.Items.INTERACTIONMOSSYBRICK, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONSMOOTHBRICK, TFGTags.Items.INTERACTIONMOSSYBRICK, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONSMOOTHBRICK, TFGTags.Items.INTERACTIONCRACKEDBRICK, CustomTags.HAMMERS),
            //cobble -> mossy
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLE, TFGTags.Items.INTERACTIONMOSSYCOBBLE, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLESTAIR, TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLESLAB, TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLEWALL, TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, GTItems.PLANT_BALL.asItem()),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLE, TFGTags.Items.INTERACTIONMOSSYCOBBLE, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLESTAIR, TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLESLAB, TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, TFCTags.Items.COMPOST_GREENS_LOW),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONCOBBLEWALL, TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, TFCTags.Items.COMPOST_GREENS_LOW),
            //mossy -> cobble
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLE, TFGTags.Items.INTERACTIONCOBBLE, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, TFGTags.Items.INTERACTIONCOBBLESTAIR, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, TFGTags.Items.INTERACTIONCOBBLESLAB, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, TFGTags.Items.INTERACTIONCOBBLEWALL, CustomTags.KNIVES),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLE, TFGTags.Items.INTERACTIONCOBBLE, pumice_item),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLESTAIR, TFGTags.Items.INTERACTIONCOBBLESTAIR, pumice_item),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLESLAB, TFGTags.Items.INTERACTIONCOBBLESLAB, pumice_item),
            new BlockInteractionRecipe(TFGTags.Items.INTERACTIONMOSSYCOBBLEWALL, TFGTags.Items.INTERACTIONCOBBLEWALL, pumice_item)

    };

}
