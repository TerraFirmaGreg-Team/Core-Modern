package exception.terrafirmagreg.event;

import exception.terrafirmagreg.TerraFirmaGreg;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TerraFirmaGreg.MOD_ID)
public class EventHandler {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            BlockState state = event.getLevel().getBlockState(event.getPos());
            // TODO crops with right click harvest like pams maple trees cancel the right click event and stops you from placing blocks
            // PR needed to add an allowed blocks tag I suppose because this shit ain't working
            if (state.is(TerraFirmaGreg.TAPPABLE) && event.getItemStack().is(TerraFirmaGreg.TAP_ITEM.get()) && event.isCanceled()) {
                event.setCanceled(false);
            }
        }
    }
}
