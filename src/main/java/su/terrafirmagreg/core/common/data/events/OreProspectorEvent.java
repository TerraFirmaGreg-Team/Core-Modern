package su.terrafirmagreg.core.common.data.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.config.TFGConfig;

import java.util.List;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class OreProspectorEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();
        ItemStack held = player.getItemInHand(event.getHand());

        if (level.isClientSide()) return;

        boolean matchesTag = getWeakOreProspectorListHelper().stream().anyMatch(h -> held.is(h.getItemTag()))
                || getNormalOreProspectorListHelper().stream().anyMatch(h -> held.is(h.getItemTag()))
                || getAdvancedOreProspectorListHelper().stream().anyMatch(h -> held.is(h.getItemTag()));

        if (matchesTag) {
            getWeakOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
            getNormalOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
            getAdvancedOreProspectorListHelper().forEach(h -> h.handleRightClick(event));

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        getWeakOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
        getNormalOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
        getAdvancedOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
    }

    @Contract(" -> new")
    public static @NotNull @Unmodifiable List<WeakOreProspectorEventHelper> getWeakOreProspectorListHelper() {
        return List.of(
                new WeakOreProspectorEventHelper(
                        TFGConfig.CopperOreProspectorLength,
                        TFGConfig.CopperOreProspectorHalfWidth,
                        TFGConfig.CopperOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsCopper
                ),
                new WeakOreProspectorEventHelper(
                        TFGConfig.BronzeOreProspectorLength,
                        TFGConfig.BronzeOreProspectorHalfWidth,
                        TFGConfig.BronzeOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsBronze
                )
        );
    }

    @Contract(" -> new")
    public static @NotNull @Unmodifiable List<NormalOreProspectorEventHelper> getNormalOreProspectorListHelper() {
        return List.of(
                new NormalOreProspectorEventHelper(
                        TFGConfig.WroughtIronOreProspectorLength,
                        TFGConfig.WroughtIronOreProspectorHalfWidth,
                        TFGConfig.WroughtIronOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsWroughtIron
                ),
                new NormalOreProspectorEventHelper(
                        TFGConfig.SteelOreProspectorLength,
                        TFGConfig.SteelOreProspectorHalfWidth,
                        TFGConfig.SteelOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsSteel
                ),
                new NormalOreProspectorEventHelper(
                        TFGConfig.BlackSteelOreProspectorLength,
                        TFGConfig.BlackSteelOreProspectorHalfWidth,
                        TFGConfig.BlackSteelOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsBlackSteel
                )
        );
    }

    @Contract(" -> new")
    public static @NotNull @Unmodifiable List<AdvancedOreProspectorEventHelper> getAdvancedOreProspectorListHelper() {
        return List.of(
                new AdvancedOreProspectorEventHelper(
                        TFGConfig.BlueSteelOreProspectorLength,
                        TFGConfig.BlueSteelOreProspectorHalfWidth,
                        TFGConfig.BlueSteelOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsBlueSteel,
                        TFGConfig.BlueSteelOreProspectorRender
                ),
                new AdvancedOreProspectorEventHelper(
                        TFGConfig.RedSteelOreProspectorLength,
                        TFGConfig.RedSteelOreProspectorHalfWidth,
                        TFGConfig.RedSteelOreProspectorHalfWidth,
                        TFGTags.Items.OreProspectorsRedSteel,
                        TFGConfig.RedSteelOreProspectorRender
                )
        );
    }
}
