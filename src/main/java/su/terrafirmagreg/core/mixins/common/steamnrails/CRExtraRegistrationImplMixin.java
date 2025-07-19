package su.terrafirmagreg.core.mixins.common.steamnrails;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRExtraRegistration;
import com.railwayteam.railways.registry.forge.CRExtraRegistrationImpl;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Temporary fix for #1288 until steam n rails properly fixes itself
 */
@Mixin(value = CRExtraRegistrationImpl.class)
public abstract class CRExtraRegistrationImplMixin {
	@Unique
	private static final CreateRegistrate REGISTRATE;

	static {
		CreateRegistrate localRegistrate = null;

		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(Create.class, lookup);

			VarHandle handle = privateLookup.findStaticVarHandle(Create.class, "REGISTRATE", CreateRegistrate.class);
			localRegistrate = (CreateRegistrate) handle.get();
		} catch (Exception e) {
			Railways.LOGGER.error("Failed to get Create's Registrate Instance, This should not happen!!", e);
		}

		REGISTRATE = localRegistrate;
	}

	@Inject(method = "platformSpecificRegistration", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$platformSpecificRegistration(CallbackInfo ci) {
		if (REGISTRATE != null) {
			REGISTRATE.addRegisterCallback("copycat", Registries.BLOCK_ENTITY_TYPE, CRExtraRegistration::addVentAsCopycat);
			REGISTRATE.addRegisterCallback("track_signal", Registries.BLOCK, CRExtraRegistration::addSignalSource);
		}

		ci.cancel();
	}
}
