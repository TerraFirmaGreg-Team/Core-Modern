package su.terrafirmagreg.core.common.entity.platerodrilus;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import dev.dh.arthropocolypse.entity.Platerodrilus;

public class PlaterodrilusData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Platerodrilus.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Platerodrilus.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(Platerodrilus platero) {
        return platero.getEntityData().get(DATA_FAMILIARITY);
    }

    public static boolean isMale(Platerodrilus platero) {
        return platero.getEntityData().get(DATA_IS_MALE);
    }
}
