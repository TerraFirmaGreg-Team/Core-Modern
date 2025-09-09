package su.terrafirmagreg.core.common.data.events;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.config.TFGConfig;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class OreProspectorEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();
        ItemStack held = player.getItemInHand(event.getHand());

        if (level.isClientSide())
            return;

        boolean matchesTag = getWeakOreProspectorListHelper().stream().anyMatch(h -> held.is(h.getItemTag()))
                || getNormalOreProspectorListHelper().stream().anyMatch(h -> held.is(h.getItemTag()))
                || getAdvancedOreProspectorListHelper().stream().anyMatch(h -> held.is(h.getItemTag()));

        if (matchesTag) {
            BlockPos pos = event.getPos();
            BlockEntity blockEntity = level.getBlockEntity(pos);

            // Allow containers to be opened (chests, barrels, etc.)
            boolean isContainer = blockEntity instanceof MenuProvider;
            boolean isMachine = blockEntity instanceof IMachineBlockEntity;

            // Allow interactable entities (item frames, armor stands, tool racks, etc.)
            boolean hasEntityTarget = event.getEntity() != null
                    && player.pick(5.0D, 0.0F, false) instanceof EntityHitResult;

            // Determines which events should be canceled
            boolean shouldCancel = (!hasEntityTarget && !isMachine && !isContainer) || player.isCrouching();

            if (shouldCancel) {
                getWeakOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
                getNormalOreProspectorListHelper().forEach(h -> h.handleRightClick(event));
                getAdvancedOreProspectorListHelper().forEach(h -> h.handleRightClick(event));

                event.setCanceled(true);
            }
        }
    }

    @Contract(" -> new")
    public static @NotNull @Unmodifiable List<WeakOreProspectorEventHelper> getWeakOreProspectorListHelper() {
        return List.of(
                new WeakOreProspectorEventHelper(
                        TFGConfig.SERVER.copperPropickConfig.searchLength().get(),
                        TFGConfig.SERVER.copperPropickConfig.searchWidth().get(),
                        TFGConfig.SERVER.copperPropickConfig.searchWidth().get(),
                        TFGTags.Items.OreProspectorsCopper),
                new WeakOreProspectorEventHelper(
                        TFGConfig.SERVER.bronzePropickConfig.searchLength().get(),
                        TFGConfig.SERVER.bronzePropickConfig.searchWidth().get(),
                        TFGConfig.SERVER.bronzePropickConfig.searchWidth().get(),
                        TFGTags.Items.OreProspectorsBronze));
    }

    @Contract(" -> new")
    public static @NotNull @Unmodifiable List<NormalOreProspectorEventHelper> getNormalOreProspectorListHelper() {
        return List.of(
                new NormalOreProspectorEventHelper(
                        TFGConfig.SERVER.wroughtIronPropickConfig.searchLength().get(),
                        TFGConfig.SERVER.wroughtIronPropickConfig.searchWidth().get(),
                        TFGConfig.SERVER.wroughtIronPropickConfig.searchWidth().get(),
                        TFGTags.Items.OreProspectorsWroughtIron),
                new NormalOreProspectorEventHelper(
                        TFGConfig.SERVER.steelPropickConfig.searchLength().get(),
                        TFGConfig.SERVER.steelPropickConfig.searchWidth().get(),
                        TFGConfig.SERVER.steelPropickConfig.searchWidth().get(),
                        TFGTags.Items.OreProspectorsSteel),
                new NormalOreProspectorEventHelper(
                        TFGConfig.SERVER.blackSteelPropickConfig.searchLength().get(),
                        TFGConfig.SERVER.blackSteelPropickConfig.searchWidth().get(),
                        TFGConfig.SERVER.blackSteelPropickConfig.searchWidth().get(),
                        TFGTags.Items.OreProspectorsBlackSteel));
    }

    @Contract(" -> new")
    public static @NotNull @Unmodifiable List<AdvancedOreProspectorEventHelper> getAdvancedOreProspectorListHelper() {
        return List.of(
                new AdvancedOreProspectorEventHelper(
                        TFGConfig.SERVER.blueSteelPropickConfig.inner().searchLength().get(),
                        TFGConfig.SERVER.blueSteelPropickConfig.inner().searchWidth().get(),
                        TFGConfig.SERVER.blueSteelPropickConfig.inner().searchWidth().get(),
                        TFGTags.Items.OreProspectorsBlueSteel,
                        TFGConfig.SERVER.blueSteelPropickConfig.preciselyRenderVein().get()),
                new AdvancedOreProspectorEventHelper(
                        TFGConfig.SERVER.redSteelPropickConfig.inner().searchLength().get(),
                        TFGConfig.SERVER.redSteelPropickConfig.inner().searchWidth().get(),
                        TFGConfig.SERVER.redSteelPropickConfig.inner().searchWidth().get(),
                        TFGTags.Items.OreProspectorsRedSteel,
                        TFGConfig.SERVER.redSteelPropickConfig.preciselyRenderVein().get()));
    }
}
