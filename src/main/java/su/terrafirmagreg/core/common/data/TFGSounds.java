package su.terrafirmagreg.core.common.data;

import java.util.Optional;
import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.dries007.tfc.client.TFCSounds;
import net.minecraft.sounds.SoundEvents;

import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings({ "unchecked" })
public final class TFGSounds {
    // TFC Entity Sounds
    public static final SoundEntry SEAL_AMBIENT = TFGCore.REGISTRATE.sound(TFGCore.id("seal_ambient")).addVariant(TFGCore.id("seal_ambient_1")).build();
    public static final SoundEntry SEAL_DEATH = TFGCore.REGISTRATE.sound(TFGCore.id("seal_death")).addVariant(TFGCore.id("seal_death_1")).build();
    public static final SoundEntry SEAL_HURT = TFGCore.REGISTRATE.sound(TFGCore.id("seal_hurt")).addVariant(TFGCore.id("seal_hurt_1")).build();
    public static final SoundEntry SEAL_WALK = TFGCore.REGISTRATE.sound(TFGCore.id("seal_walk")).addVariant(TFGCore.id("seal_walk_1")).addVariant(TFGCore.id("seal_walk_2"))
            .addVariant(TFGCore.id("seal_walk_3")).build();
    public static final SoundEntry SEAL_ATTACK = TFGCore.REGISTRATE.sound(TFGCore.id("seal_attack")).addVariant(TFGCore.id("seal_attack_1")).addVariant(TFGCore.id("seal_attack_2")).build();

    public static final SoundEntry BISON_AMBIENT = TFGCore.REGISTRATE.sound(TFGCore.id("bison_ambient")).playExisting(TFCSounds.DEER.ambient(), 1.0f, 0.9f).build();
    public static final SoundEntry BISON_DEATH = TFGCore.REGISTRATE.sound(TFGCore.id("bison_death")).playExisting(TFCSounds.DEER.death(), 1.0f, 1.0f).build();
    public static final SoundEntry BISON_HURT = TFGCore.REGISTRATE.sound(TFGCore.id("bison_hurt")).playExisting(TFCSounds.DEER.hurt(), 1.0f, 1.0f).build();
    public static final SoundEntry BISON_WALK = TFGCore.REGISTRATE.sound(TFGCore.id("bison_walk")).playExisting(SoundEvents.WOLF_STEP).build();
    public static final SoundEntry BISON_ATTACK = TFGCore.REGISTRATE.sound(TFGCore.id("bison_attack")).playExisting(TFCSounds.MOOSE.attack().get(), 1.0f, 1.0f).build();

    public static final TFCSounds.EntitySound SEAL = new TFCSounds.EntitySound(SEAL_AMBIENT::getMainEvent, SEAL_DEATH::getMainEvent, SEAL_HURT::getMainEvent, SEAL_WALK::getMainEvent,
            Optional.of(SEAL_ATTACK::getMainEvent), Optional.empty());
    public static final TFCSounds.EntitySound BISON = new TFCSounds.EntitySound(BISON_AMBIENT::getMainEvent, BISON_DEATH::getMainEvent, BISON_HURT::getMainEvent, BISON_WALK::getMainEvent,
            Optional.of(BISON_ATTACK::getMainEvent), Optional.empty());
    public static final TFCSounds.EntitySound FOX = new TFCSounds.EntitySound(() -> SoundEvents.FOX_AMBIENT, () -> SoundEvents.FOX_DEATH, () -> SoundEvents.FOX_HURT, () -> SoundEvents.CHICKEN_STEP,
            Optional.of((Supplier) () -> SoundEvents.FOX_BITE), Optional.of((Supplier) () -> SoundEvents.FOX_SLEEP));

    // GT Machine Sounds
    public static final SoundEntry GEOLOGIC_VULCANIZER = TFGCore.REGISTRATE.sound(TFGCore.id("geologic_vulcanizer")).build();

    public static void init() {
    }
}
