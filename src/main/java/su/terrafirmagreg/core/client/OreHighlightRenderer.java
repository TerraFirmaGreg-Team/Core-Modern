package su.terrafirmagreg.core.client;

import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGParticles;
import su.terrafirmagreg.core.config.TFGConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
public class OreHighlightRenderer {

    private static final List<Highlight> highlights = new ArrayList<>();
    private static final long HIGHLIGHT_DURATION_MS = 6_000; // 6 seconds

    public static void addHighlights(List<BlockPos> positions) {
        long expireTime = System.currentTimeMillis() + HIGHLIGHT_DURATION_MS;
        Minecraft mc = Minecraft.getInstance();

        int chance = TFGConfig.PreciseOreProspectorParticleChance; // 1 in N from config

        synchronized (highlights) {
            for (BlockPos pos : positions) {
                highlights.add(new Highlight(pos, expireTime));

                // Spawn particle based on config, if 0 it will diable the particle
                if (mc.level != null && chance != 0 && ThreadLocalRandom.current().nextInt(chance) == 0) {
                    mc.level.addParticle(
                            TFGParticles.ORE_PROSPECTOR.get(),
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            0, 0, 0
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        long now = System.currentTimeMillis();

        synchronized (highlights) {
            Iterator<Highlight> iterator = highlights.iterator();
            while (iterator.hasNext()) {
                Highlight highlight = iterator.next();
                if (highlight.expireTime < now) {
                    iterator.remove();
                }
            }
        }
    }

    private record Highlight(BlockPos pos, long expireTime) {}
}
