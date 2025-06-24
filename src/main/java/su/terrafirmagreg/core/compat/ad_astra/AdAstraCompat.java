package su.terrafirmagreg.core.compat.ad_astra;

import earth.terrarium.adastra.api.events.AdAstraEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import su.terrafirmagreg.core.common.data.TFGTags;

// Misc Ad Astra compat stuff!
// API is over here https://ad-astra.terrarium.wiki/events.html

public abstract class AdAstraCompat {

	public static void RegisterEvents()
	{
		AdAstraEvents.GravityTickEvent.register(AdAstraCompat::OnGravityTick);
	}

	private static boolean OnGravityTick(Level level, LivingEntity entity, Vec3 travelVector, BlockPos movementAffectingPos)
	{
		if (entity.getType().getTags().anyMatch(t -> t == TFGTags.Entities.IgnoresGravity))
			return false;

		return true;
	}
}
