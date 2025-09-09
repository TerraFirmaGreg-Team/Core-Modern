package su.terrafirmagreg.core.common.data;

import java.util.function.Supplier;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;

public final class TFGEntityDataSerializers {

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, TFGCore.MOD_ID);

    public static final RegistryObject<EntityDataSerializer<TFCSniffer.State>> SNIFFER_STATE = register("sniffer_state",
            () -> EntityDataSerializer.simpleEnum(TFCSniffer.State.class));

    private static <T extends EntityDataSerializer<?>> RegistryObject<T> register(String name,
            Supplier<T> dataSerializer) {
        return ENTITY_DATA_SERIALIZERS.register(name, dataSerializer);
    }
}
