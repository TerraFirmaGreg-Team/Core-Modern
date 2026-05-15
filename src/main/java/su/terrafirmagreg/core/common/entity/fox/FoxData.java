package su.terrafirmagreg.core.common.entity.fox;

import net.dries007.tfc.common.entities.prey.TFCFox;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public abstract class FoxData {
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(TFCFox.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(TFCFox fox) {
        return fox.getEntityData().get(DATA_FAMILIARITY);
    }
}
