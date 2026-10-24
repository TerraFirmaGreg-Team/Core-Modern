package su.terrafirmagreg.core.world.rock_getters;

import com.notenoughmail.kubejs_tfc.util.implementation.custom.LayeredArea;

import net.dries007.tfc.world.layer.UniformLayer;
import net.dries007.tfc.world.noise.OpenSimplex2D;

public class TFGRockGetters {

    public static BeneathRockGetter Beneath;
    public static MoonRockGetter Moon;
    public static MarsRockGetter Mars;
    public static VenusRockGetter Venus;
    public static MercuryRockGetter Mercury;
    public static EuropaRockGetter Europa;

    static {
        // Rock layer noise
        LayeredArea rockLayer = new LayeredArea(UniformLayer.INSTANCE, 413567326L);
        for (int i = 0; i < 3; i++) {
            rockLayer.zoom(true, 19763144126L).smooth(79784123632L);
        }
        for (int i = 0; i < 6; i++) {
            rockLayer.zoom(true, 451364589723L);
        }
        rockLayer.smooth(71214856214L).zoom(true, 854126548632L).smooth(145256147896L);

        // Depth rock noise
        OpenSimplex2D depthRockNoise = new OpenSimplex2D(432746324L).octaves(2).spread(0.03).scaled(-10, 10);

        // Dimension noises
        Beneath = new BeneathRockGetter(rockLayer, depthRockNoise);
        Moon = new MoonRockGetter(rockLayer, depthRockNoise);
        Mars = new MarsRockGetter(rockLayer, depthRockNoise);
        Venus = new VenusRockGetter(rockLayer, depthRockNoise);
        Mercury = new MercuryRockGetter(rockLayer, depthRockNoise);
        Europa = new EuropaRockGetter(rockLayer, depthRockNoise);
    }
}
