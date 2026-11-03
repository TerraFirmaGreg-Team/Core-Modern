package su.terrafirmagreg.core.common.entity.walker;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.wanmine.wab.entity.Walker;

public class WalkerData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Walker.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Walker.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(Walker walker) {
        return walker.getEntityData().get(DATA_FAMILIARITY);
    }

    public static boolean isMale(Walker walker) {
        return walker.getEntityData().get(DATA_IS_MALE);
    }
}
