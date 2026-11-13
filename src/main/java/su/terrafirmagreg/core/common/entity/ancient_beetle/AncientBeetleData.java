package su.terrafirmagreg.core.common.entity.ancient_beetle;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import en.predator97427fr.aliencraft.entity.AncientBeetleEntity;

public class AncientBeetleData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(AncientBeetleEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DATA_FAMILIARITY = SynchedEntityData.defineId(AncientBeetleEntity.class, EntityDataSerializers.FLOAT);

    public static float getFamiliarity(AncientBeetleEntity beetle) {
        return beetle.getEntityData().get(DATA_FAMILIARITY);
    }

    public static boolean isMale(AncientBeetleEntity beetle) {
        return beetle.getEntityData().get(DATA_IS_MALE);
    }
}
