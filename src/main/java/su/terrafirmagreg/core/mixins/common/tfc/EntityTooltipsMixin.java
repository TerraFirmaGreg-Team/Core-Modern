package su.terrafirmagreg.core.mixins.common.tfc;

import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.dries007.tfc.common.entities.ai.prey.TFCOcelot;
import net.dries007.tfc.common.entities.aquatic.TFCSquid;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
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
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.WaterAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.entities.moonrabbit.MoonRabbit;
import su.terrafirmagreg.core.common.data.entities.glacianram.TFCGlacianRam;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;
import su.terrafirmagreg.core.common.data.entities.wraptor.TFCWraptor;

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
		registry.register("animal", TFG_ANIMAL, TFCAnimal.class);
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

	@Unique
	private static final EntityTooltip TFG_ANIMAL = (level, entity, tooltip) -> {
		if (entity instanceof WildAnimal animal) {
			if (animal.displayMaleCharacteristics()) {
				tooltip.accept(Helpers.translateEnum(TFCAnimalProperties.Gender.MALE));
			} else if (animal.displayFemaleCharacteristics()) {
				tooltip.accept(Helpers.translateEnum(TFCAnimalProperties.Gender.FEMALE));
			}
			if (animal.isBaby()) {
				tooltip.accept(Component.translatable("tfc.jade.juvenile"));
			}
		}
		if (entity instanceof TFCAnimalProperties animal) {
			final MutableComponent line1 = Helpers.translateEnum(animal.getGender());

			if (animal.isFertilized()) {
				line1.append(", ").append(Component.translatable("tfc.tooltip.fertilized"));
			}
			final float familiarity = Math.max(0.0F, Math.min(1.0F, animal.getFamiliarity()));
			final String familiarityPercent = String.format("%.2f", familiarity * 100);

			final TFCAnimalProperties.Age age = animal.getAgeType();
			ChatFormatting familiarityStyle = ChatFormatting.GRAY;
			if (familiarity >= animal.getAdultFamiliarityCap() && age != TFCAnimalProperties.Age.CHILD) {
				familiarityStyle = ChatFormatting.RED;
			} else if (familiarity >= TFCConfig.SERVER.familiarityDecayLimit.get()) {
				familiarityStyle = ChatFormatting.WHITE;
			}
			line1.append(", ").append(Component.translatable("tfc.jade.familiarity", familiarityPercent).withStyle(familiarityStyle));
			tooltip.accept(line1);
			tooltip.accept(Component.translatable("tfc.jade.animal_size", animal.getGeneticSize()));
			if (animal.isReadyForAnimalProduct()) {
				tooltip.accept(animal.getProductReadyName().withStyle(ChatFormatting.GREEN));
			}
			if (animal instanceof TFCSniffer sniffer) {
				if (sniffer.isReadyForWoolProduct()) tooltip.accept(sniffer.getWoolReadyName().withStyle(ChatFormatting.GREEN));
			}
			if (animal instanceof TFCWraptor wraptor) {
				if (wraptor.isReadyForWoolProduct()) tooltip.accept(wraptor.getWoolReadyName().withStyle(ChatFormatting.GREEN));
			}
			if (animal.isReadyToMate()) {
				tooltip.accept(Component.translatable("tfc.jade.can_mate"));
			}

			// when the animal is 'used up' but hasn't hit its asynchronous old day yet
			final double usageRatio = animal.getUses() >= animal.getUsesToElderly() ? 0.99 : (float) animal.getUses() / animal.getUsesToElderly();
			switch (age) {
				case CHILD ->
						tooltip.accept(Component.translatable("tfc.jade.adulthood_progress", Calendars.get(level).getTimeDelta(ICalendar.TICKS_IN_DAY * (animal.getDaysToAdulthood() + animal.getBirthDay() - Calendars.get(level).getTotalDays()))));
				case ADULT ->
						tooltip.accept(Component.translatable("tfc.jade.animal_wear", String.format("%d%%", Math.min(100, Math.round(100f * usageRatio)))));
				case OLD -> tooltip.accept(Component.translatable("tfc.jade.old_animal"));
			}

		}
		if (entity instanceof MammalProperties mammal) {
			if (mammal.getPregnantTime() > 0) {
				tooltip.accept(Component.translatable("tfc.tooltip.animal.pregnant", entity.getName().getString()));

				final ICalendar calendar = Calendars.get(level);
				tooltip.accept(Component.translatable("tfc.jade.gestation_time_left", calendar.getTimeDelta(ICalendar.TICKS_IN_DAY * (mammal.getGestationDays() + mammal.getPregnantTime() - Calendars.get(level).getTotalDays()))));
			}
		}
		if (entity instanceof TFCGlacianRam) {
			tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.glacian_ram"));
		}
		if (entity instanceof TFCSniffer) {
			tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.sniffer"));
		}
		if (entity instanceof TFCWraptor) {
			tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.wraptor"));
		}
	};

}
