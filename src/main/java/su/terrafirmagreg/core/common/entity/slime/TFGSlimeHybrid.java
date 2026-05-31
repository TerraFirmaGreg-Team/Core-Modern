package su.terrafirmagreg.core.common.entity.slime;

import org.jetbrains.annotations.Nullable;

import dev.ftb.mods.ftbquests.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public enum TFGSlimeHybrid {
    RESIN(TFGSlimeVariant.PLANT, TFGSlimeVariant.GLOWBERRY, TFGSlimeVariant.RESIN),
    LATEX(TFGSlimeVariant.RESIN, TFGSlimeVariant.SPRING, TFGSlimeVariant.LATEX);

    public static final TFGSlimeHybrid[] VALUES = values();

    private final TFGSlimeVariant firstMate;
    private final TFGSlimeVariant secondMate;
    private final TFGSlimeVariant hybrid;

    TFGSlimeHybrid(TFGSlimeVariant firstMate, TFGSlimeVariant secondMate, TFGSlimeVariant hybrid) {
        this.firstMate = firstMate;
        this.secondMate = secondMate;
        this.hybrid = hybrid;
    }

    @Nullable
    public static TFGSlimeVariant getHybrid(TFGSlimeVariant fMate, TFGSlimeVariant sMate) {
        for (TFGSlimeHybrid result : VALUES) {
            if ((result.firstMate == fMate && result.secondMate == sMate) ||
                    (result.firstMate == sMate && result.secondMate == fMate)) {
                return result.hybrid;
            }
        }

        return null;
    }
}
