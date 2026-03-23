package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.world.region.Region;

import su.terrafirmagreg.core.world.new_ow_wg.region.IRegionPoint;

@Mixin(value = Region.Point.class, remap = false)
public abstract class PointMixin implements IRegionPoint {
    @Unique
    private int tfg$x;
    @Unique
    private int tfg$z;
    @Unique
    private int tfg$index;
    @Unique
    private byte tfg$distanceToWestCoast;
    @Unique
    private boolean tfg$isSurfaceRockKarst;
    @Unique
    private byte tfg$hotSpotAge;

    @Unique
    public void tfg$init(int x, int z, int index) {
        tfg$x = x;
        tfg$z = z;
        tfg$index = index;
        tfg$distanceToWestCoast = 0;
        tfg$isSurfaceRockKarst = false;
        tfg$hotSpotAge = 0;
    }

    @Unique
    public void tfg$setDistanceToWestCoast(byte dist) {
        tfg$distanceToWestCoast = dist;
    }

    @Unique
    public byte tfg$getDistanceToWestCoast() {
        return tfg$distanceToWestCoast;
    }

    @Unique
    public void tfg$setIsSurfaceRockKarst(boolean isKarst) {
        tfg$isSurfaceRockKarst = isKarst;
    }

    @Unique
    public boolean tfg$getIsSurfaceRockKarst() {
        return tfg$isSurfaceRockKarst;
    }

    @Unique
    public void tfg$setHotSpotAge(byte age) {
        tfg$hotSpotAge = age;
    }

    @Unique
    public byte tfg$getHotSpotAge() {
        return tfg$hotSpotAge;
    }

    @Unique
    public int tfg$getX() {
        return tfg$x;
    }

    @Unique
    public int tfg$getZ() {
        return tfg$z;
    }

    @Unique
    public int tfg$getIndex() {
        return tfg$index;
    }

}
