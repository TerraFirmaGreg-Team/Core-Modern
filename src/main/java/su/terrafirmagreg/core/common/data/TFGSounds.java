package su.terrafirmagreg.core.common.data;

import java.util.Optional;
import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.dries007.tfc.client.TFCSounds;
import net.minecraft.sounds.SoundEvents;

import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings({ "unchecked" })
public final class TFGSounds {
    public static final TFCSounds.EntitySound SEAL = new TFCSounds.EntitySound(() -> SoundEvents.FOX_AMBIENT, () -> SoundEvents.FOX_DEATH, () -> SoundEvents.FOX_HURT, () -> SoundEvents.CHICKEN_STEP,
            Optional.of((Supplier) () -> SoundEvents.FOX_BITE), Optional.of((Supplier) () -> SoundEvents.FOX_SLEEP));
    public static final TFCSounds.EntitySound BISON = new TFCSounds.EntitySound(() -> SoundEvents.FOX_AMBIENT, () -> SoundEvents.FOX_DEATH, () -> SoundEvents.FOX_HURT, () -> SoundEvents.CHICKEN_STEP,
            Optional.of((Supplier) () -> SoundEvents.FOX_BITE), Optional.of((Supplier) () -> SoundEvents.FOX_SLEEP));
    public static final TFCSounds.EntitySound FOX = new TFCSounds.EntitySound(() -> SoundEvents.FOX_AMBIENT, () -> SoundEvents.FOX_DEATH, () -> SoundEvents.FOX_HURT, () -> SoundEvents.CHICKEN_STEP,
            Optional.of((Supplier) () -> SoundEvents.FOX_BITE), Optional.of((Supplier) () -> SoundEvents.FOX_SLEEP));

    public static final SoundEntry SEAL_AMBIENT = TFGCore.REGISTRATE.sound(TFGCore.id("seal_ambient")).addVariant(TFGCore.id("seal_ambient_1")).build();

    public static final SoundEntry GEOLOGIC_VULCANIZER = TFGCore.REGISTRATE.sound(TFGCore.id("geologic_vulcanizer")).build();

    public static void init() {
    }
}
