package su.terrafirmagreg.core.common.data.entities.charger;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.wanmine.wab.entity.Charger;

public class ChargerData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Charger.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(Charger.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(Charger charger) {
        return charger.getEntityData().get(DATA_FAMILIARITY);
    }

    public static boolean isMale(Charger charger) {
        return charger.getEntityData().get(DATA_IS_MALE);
    }
}
