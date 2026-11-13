package su.terrafirmagreg.core.common.environment;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;

/**
 * Interface for blocks that have behavior changes when inside greenhouses.
 * <p>Currently this applies to:
 * <u>
 *  <li>{@link com.eerussianguy.firmalife.common.blockentities.FLBeehiveBlockEntity}</li>
 *  <li>{@link com.therighthon.afc.common.blockentities.TapBlockEntity}</li>
 *  </u></p>
 */
public interface IGreenhouseReceiver extends ClimateReceiver {
    boolean tfg$isInsideValidGreenhouse();
}
