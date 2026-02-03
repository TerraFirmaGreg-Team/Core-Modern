package su.terrafirmagreg.core.common.data.entities.axolotl;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public abstract class AxolotlData {
    public static final EntityDataAccessor<Boolean> DATA_IS_MALE = SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BOOLEAN);

    public static boolean isMale(Axolotl axolotl) {
        return axolotl.getEntityData().get(DATA_IS_MALE);
    }
}
