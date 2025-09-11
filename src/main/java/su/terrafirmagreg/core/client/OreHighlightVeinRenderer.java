package su.terrafirmagreg.core.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGParticles;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
public class OreHighlightVeinRenderer {

    private static final List<Highlight> highlights = new ArrayList<>();
    private static final long HIGHLIGHT_DURATION_MS = 6_000;

    public static void addVeinHighlights(List<BlockPos> positions) {
        long expireTime = System.currentTimeMillis() + HIGHLIGHT_DURATION_MS;
        Minecraft mc = Minecraft.getInstance();

        synchronized (highlights) {
            for (BlockPos pos : positions) {
                highlights.add(new Highlight(pos, expireTime));

                if (mc.level != null) {
                    mc.level.addParticle(
                            TFGParticles.ORE_PROSPECTOR_VEIN.get(),
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            0, 0, 0);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        long now = System.currentTimeMillis();
        synchronized (highlights) {
            highlights.removeIf(h -> h.expireTime < now);
        }
    }

    private record Highlight(BlockPos pos, long expireTime) {
    }
}
