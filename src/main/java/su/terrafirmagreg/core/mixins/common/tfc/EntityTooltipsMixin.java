package su.terrafirmagreg.core.mixins.common.tfc;

import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.dries007.tfc.common.entities.ai.prey.TFCOcelot;
import net.dries007.tfc.common.entities.aquatic.TFCSquid;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.horse.TFCChestedHorse;
import net.dries007.tfc.common.entities.livestock.horse.TFCHorse;
import net.dries007.tfc.common.entities.misc.TFCFishingHook;
import net.dries007.tfc.common.entities.predator.Predator;
import net.dries007.tfc.common.entities.prey.TFCFrog;
import net.dries007.tfc.common.entities.prey.TFCRabbit;
import net.dries007.tfc.common.entities.prey.WildAnimal;
import net.dries007.tfc.compat.jade.common.EntityTooltip;
import net.dries007.tfc.compat.jade.common.EntityTooltips;
import net.dries007.tfc.compat.jade.common.RegisterCallback;
import net.dries007.tfc.util.Helpers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.WaterAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.entities.MoonRabbit;

import java.util.Locale;

import static net.dries007.tfc.compat.jade.common.EntityTooltips.*;

@Mixin(value = EntityTooltips.class, remap = false)
public abstract class EntityTooltipsMixin {

	// This is easier than doing our own jade compat, lol

	//@Inject(method = "register", at = @At("TAIL"), remap = false)
	/**
	 * @author a
	 * @reason a
	 */
	@Overwrite
	public static void register(RegisterCallback<EntityTooltip, Entity> registry)
	{
		// Overwrite the TFC ones because I don't know how the hell you're supposed to mixin static lambdas
		registry.register("animal", ANIMAL, TFCAnimal.class);
		registry.register("horse", ANIMAL, TFCHorse.class);
		registry.register("chested_horse", ANIMAL, TFCChestedHorse.class);
		registry.register("rabbit", ANIMAL, TFCRabbit.class);
		registry.register("wild_animal", ANIMAL, WildAnimal.class);
		registry.register("frog", FROG, TFCFrog.class);
		registry.register("squid", SQUID, TFCSquid.class);
		registry.register("fish", FISH, WaterAnimal.class);
		registry.register("predator", PREDATOR, Predator.class);
		registry.register("pack_predator", PACK_PREDATOR, PackPredator.class);
		registry.register("ocelot", OCELOT, TFCOcelot.class);
		registry.register("fishing_hook", HOOK, TFCFishingHook.class);
		registry.register("rabbit", TFG_RABBIT, Rabbit.class);

		//registry.register(ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "moon_rabbit"), MOON_RABBIT, MoonRabbit.class);
	}

	@Unique
	private static final EntityTooltip TFG_RABBIT = (level, entity, tooltip) -> {
		if (entity instanceof MoonRabbit moonRabbit)
		{
			tooltip.accept(Component.translatable((TFGCore.MOD_ID + ".tooltip.moon_rabbit_variant." + moonRabbit.getMoonVariant().name()).toLowerCase(Locale.ROOT)));
		}
		else if (entity instanceof Rabbit rabbit)
		{
			tooltip.accept(Helpers.translateEnum(rabbit.getVariant(), "rabbit_variant"));
		}
	};
}
