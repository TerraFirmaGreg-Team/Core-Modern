package su.terrafirmagreg.core.common.data.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

import java.util.List;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class OreProspectorEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();
        ItemStack held = player.getItemInHand(event.getHand());

        if (level.isClientSide()) return;

        // Check if the held item matches any ore prospector tag
        boolean matchesTag = OreProspectorCopper.getItemTag() != null && held.is(OreProspectorCopper.getItemTag())
                || OreProspectorBronze.getItemTag() != null && held.is(OreProspectorBronze.getItemTag())
                || OreProspectorWroughtIron.getItemTag() != null && held.is(OreProspectorWroughtIron.getItemTag())
                || OreProspectorSteel.getItemTag() != null && held.is(OreProspectorSteel.getItemTag())
                || OreProspectorBlackSteel.getItemTag() != null && held.is(OreProspectorBlackSteel.getItemTag())
                || OreProspectorBlueSteel.getItemTag() != null && held.is(OreProspectorBlueSteel.getItemTag())
                || OreProspectorRedSteel.getItemTag() != null && held.is(OreProspectorRedSteel.getItemTag());

        if (matchesTag) {
            OreProspectorCopper.handleRightClick(event);
            OreProspectorBronze.handleRightClick(event);
            OreProspectorWroughtIron.handleRightClick(event);
            OreProspectorSteel.handleRightClick(event);
            OreProspectorBlackSteel.handleRightClick(event);
            OreProspectorBlueSteel.handleRightClick(event);
            OreProspectorRedSteel.handleRightClick(event);

            event.setCanceled(true); // Cancel default behavior
        }
    }

    private static final WeakOreProspectorEventHelper OreProspectorCopper = new WeakOreProspectorEventHelper(
            50, 5, 5, TFGTags.Items.OreProspectorsCopper
    );
    private static final WeakOreProspectorEventHelper OreProspectorBronze = new WeakOreProspectorEventHelper(
            60, 8, 8, TFGTags.Items.OreProspectorsBronze
    );
    private static final NormalOreProspectorEventHelper OreProspectorWroughtIron = new NormalOreProspectorEventHelper(
            70, 10, 10, TFGTags.Items.OreProspectorsWroughtIron
    );
    private static final NormalOreProspectorEventHelper OreProspectorSteel = new NormalOreProspectorEventHelper(
            80, 12, 12, TFGTags.Items.OreProspectorsSteel
    );
    private static final NormalOreProspectorEventHelper OreProspectorBlackSteel = new NormalOreProspectorEventHelper(
            90, 15, 15, TFGTags.Items.OreProspectorsBlackSteel
    );
    private static final AdvancedOreProspectorEventHelper OreProspectorBlueSteel = new AdvancedOreProspectorEventHelper(
            140, 15, 15, TFGTags.Items.OreProspectorsBlueSteel
    );
    private static final AdvancedOreProspectorEventHelper OreProspectorRedSteel = new AdvancedOreProspectorEventHelper(
            100, 25, 25, TFGTags.Items.OreProspectorsRedSteel
    );

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        OreProspectorCopper.handleRightClick(event);
        OreProspectorBronze.handleRightClick(event);
        OreProspectorWroughtIron.handleRightClick(event);
        OreProspectorSteel.handleRightClick(event);
        OreProspectorBlackSteel.handleRightClick(event);
        OreProspectorBlueSteel.handleRightClick(event);
        OreProspectorRedSteel.handleRightClick(event);
    }

    public static final List<WeakOreProspectorEventHelper> WeakOreProspectorListHelper = List.of(
            OreProspectorCopper,
            OreProspectorBronze
    );

    public static final List<NormalOreProspectorEventHelper> NormalOreProspectorListHelper = List.of(
            OreProspectorWroughtIron,
            OreProspectorSteel,
            OreProspectorBlackSteel
    );

    public static final List<AdvancedOreProspectorEventHelper> AdvancedOreProspectorListHelper = List.of(
            OreProspectorBlueSteel,
            OreProspectorRedSteel
    );

}
