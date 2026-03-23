package su.terrafirmagreg.core.world.new_ow_wg.region;

public interface IRegionPoint {

    void tfg$setDistanceToWestCoast(byte dist);

    byte tfg$getDistanceToWestCoast();

    void tfg$setIsSurfaceRockKarst(boolean isKarst);

    boolean tfg$getIsSurfaceRockKarst();

    void tfg$setHotSpotAge(byte age);

    byte tfg$getHotSpotAge();

    int tfg$getX();

    int tfg$getZ();

    int tfg$getIndex();

    void tfg$init(int x, int z, int index);
}
