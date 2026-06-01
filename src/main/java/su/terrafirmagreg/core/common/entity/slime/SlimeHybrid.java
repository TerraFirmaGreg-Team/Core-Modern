package su.terrafirmagreg.core.common.entity.slime;

import org.jetbrains.annotations.Nullable;

import dev.ftb.mods.ftbquests.MethodsReturnNonnullByDefault;

import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
public enum SlimeHybrid {
    RESIN(SlimeVariant.PLANT, SlimeVariant.GLOWBERRY, SlimeVariant.RESIN),
    LATEX(SlimeVariant.RESIN, SlimeVariant.SPRING, SlimeVariant.LATEX);

    static {
        for (SlimeHybrid hybrid : values()) {
            SlimePair.hybridMap.put(new SlimePair(hybrid.firstMate, hybrid.secondMate), hybrid.hybridOffspring);
            SlimePair.hybridMap.put(new SlimePair(hybrid.secondMate, hybrid.firstMate), hybrid.hybridOffspring);
        }
    }

    private final SlimeVariant firstMate;
    private final SlimeVariant secondMate;
    private final SlimeVariant hybridOffspring;

    SlimeHybrid(SlimeVariant firstMate, SlimeVariant secondMate, SlimeVariant hybridOffspring) {
        this.firstMate = firstMate;
        this.secondMate = secondMate;
        this.hybridOffspring = hybridOffspring;
    }

    @Nullable
    public static SlimeVariant getHybrid(SlimeVariant first, SlimeVariant second) {
        return SlimePair.hybridMap.get(new SlimePair(first, second));
    }

    private record SlimePair(SlimeVariant first, SlimeVariant second) {
        private static final Map<SlimePair, SlimeVariant> hybridMap = new HashMap<>();
    }
}
