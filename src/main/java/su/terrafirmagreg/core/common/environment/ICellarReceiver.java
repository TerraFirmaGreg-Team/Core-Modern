package su.terrafirmagreg.core.common.environment;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;

/**
 * Interface for blocks that have behavior changes when inside cellars.
 * <p>Currently this applies to:
 * <u>
 *  <li>{@link net.dries007.tfc.common.blockentities.BarrelBlockEntity}</li>
 *  </u></p>
 */
public interface ICellarReceiver extends ClimateReceiver {
    boolean isInsideValidCellar();
}
