package su.terrafirmagreg.core.common.data.entities.soarer;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.wanmine.wab.entity.Soarer;

public abstract class SoarerData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Soarer.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Soarer.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(Soarer soarer) {
        return soarer.getEntityData().get(DATA_FAMILIARITY);
    }

    public static boolean isMale(Soarer soarer) {
        return soarer.getEntityData().get(DATA_IS_MALE);
    }
}
