package su.terrafirmagreg.core.common.data;

import java.util.Optional;
import java.util.function.Supplier;

import net.dries007.tfc.client.TFCSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;

public final class TFGSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TFGCore.MOD_ID);

    public static final TFCSounds.EntitySound SEAL = createTfcEntitySounds("seal", true, false);
    public static final TFCSounds.EntitySound BISON = createTfcEntitySounds("bison", true, false);

    private static RegistryObject<SoundEvent> createSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, name)));
    }

    private static Optional<Supplier<SoundEvent>> createOptionalSound(String name, boolean isPresent) {
        return isPresent ? Optional.of(createSound(name)) : Optional.empty();
    }

    //spotless:off
    private static TFCSounds.EntitySound createTfcEntitySounds(String name, boolean attack, boolean sleep)
    {
        return new TFCSounds.EntitySound(
                createSound("entity.%s.ambient".formatted(name)),
                createSound("entity.%s.death".formatted(name)),
                createSound("entity.%s.hurt".formatted(name)),
                createSound("entity.%s.step".formatted(name)),
                createOptionalSound("entity.%s.attack".formatted(name), attack),
                createOptionalSound("entity.%s.sleep".formatted(name), sleep)
        );
    }
    //spotless:on
}
