package su.terrafirmagreg.core.common.data.entities.snatcher;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.wanmine.wab.entity.Snatcher;

public class SnatcherData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Snatcher.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Snatcher.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(Snatcher snatcher) {
        return snatcher.getEntityData().get(DATA_FAMILIARITY);
    }

    public static boolean isMale(Snatcher snatcher) {
        return snatcher.getEntityData().get(DATA_IS_MALE);
    }
}
