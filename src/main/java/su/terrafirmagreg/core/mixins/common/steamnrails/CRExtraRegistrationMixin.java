package su.terrafirmagreg.core.mixins.common.steamnrails;

import com.railwayteam.railways.registry.CRExtraRegistration;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.distant_signals.SignalDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static com.railwayteam.railways.registry.CRExtraRegistration.registeredSignalSource;

@Mixin(value = CRExtraRegistration.class, remap = false)
public abstract class CRExtraRegistrationMixin {

	/**
	 * @author Pyritie
	 * @reason Temporary fix for #1288 until steam n rails properly fixes itself
	 */
	@Overwrite
	public static void addSignalSource(Block block) {
		if (registeredSignalSource) return;
		SignalDisplaySource source = new SignalDisplaySource();
		Railways.registrate().displaySource("track_signal_source", () -> source).register();
		DisplaySource.BY_BLOCK.add(block, source);
		registeredSignalSource = true;
	}
}
