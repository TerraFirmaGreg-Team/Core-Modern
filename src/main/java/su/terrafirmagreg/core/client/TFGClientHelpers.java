package su.terrafirmagreg.core.client;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class TFGClientHelpers {

    public static Vec2 nextVec2InRadius(RandomSource random, float radius) {
        float x, y;
        do {
            x = random.nextFloat() * 2 - 1; // [-1, 1]
            y = random.nextFloat() * 2 - 1; // [-1, 1]
        } while (Mth.lengthSquared(x, y) > 1);
        return new Vec2(x * radius, y * radius);
    }
}
