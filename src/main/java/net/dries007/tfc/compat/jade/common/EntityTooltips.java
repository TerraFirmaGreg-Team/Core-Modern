/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jade.common;

import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.dries007.tfc.common.entities.prey.TFCFox;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.material.Fluids;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.entities.misc.TFCFishingHook;
import net.dries007.tfc.common.entities.prey.TFCFrog;
import net.dries007.tfc.common.entities.prey.TFCRabbit;
import net.dries007.tfc.common.entities.prey.WildAnimal;
import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.dries007.tfc.common.entities.ai.prey.TFCOcelot;
import net.dries007.tfc.common.entities.aquatic.AquaticMob;
import net.dries007.tfc.common.entities.aquatic.TFCSquid;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.dries007.tfc.common.entities.livestock.horse.TFCChestedHorse;
import net.dries007.tfc.common.entities.livestock.horse.TFCHorse;
import net.dries007.tfc.common.entities.predator.Predator;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.wanmine.wab.entity.Charger;
import net.wanmine.wab.entity.Snatcher;
import net.wanmine.wab.entity.Soarer;
import org.spongepowered.asm.mixin.Unique;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.entity.animals.tfcbison.TFCBison;
import su.terrafirmagreg.core.common.entity.animals.tfcjerboa.TFCJerboa;
import su.terrafirmagreg.core.common.entity.animals.tfclemming.TFCLemming;
import su.terrafirmagreg.core.common.entity.animals.tfcleopardseal.TFCLeopardSeal;
import su.terrafirmagreg.core.common.entity.animals.tfcmongoose.TFCMongoose;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfInterface;
import su.terrafirmagreg.core.common.entity.axolotl.AxolotlData;
import su.terrafirmagreg.core.common.entity.camels.TFCBactrianCamel;
import su.terrafirmagreg.core.common.entity.camels.TFCDromedaryCamel;
import su.terrafirmagreg.core.common.entity.charger.ChargerData;
import su.terrafirmagreg.core.common.entity.fox.FoxData;
import su.terrafirmagreg.core.common.entity.fox.TFGFox;
import su.terrafirmagreg.core.common.entity.glacianram.TFCGlacianRam;
import su.terrafirmagreg.core.common.entity.moonrabbit.MoonRabbit;
import su.terrafirmagreg.core.common.entity.snatcher.SnatcherData;
import su.terrafirmagreg.core.common.entity.sniffer.TFCSniffer;
import su.terrafirmagreg.core.common.entity.soarer.SoarerData;
import su.terrafirmagreg.core.common.entity.surfer.TFCSurfer;
import su.terrafirmagreg.core.common.entity.wraptor.TFCWraptor;
import team.terrafirmagreg.jellies.common.data.JelliesTags;

import java.util.Locale;

/**
 * Common tooltips that can be displayed for various entities via external sources.
 */
public final class EntityTooltips
{
    public static void register(RegisterCallback<EntityTooltip, Entity> registry)
    {
		registry.register("animal", TFG_ANIMAL, TFCAnimal.class);
		registry.register("horse", ANIMAL, TFCHorse.class);
		registry.register("chested_horse", ANIMAL, TFCChestedHorse.class);
		registry.register("rabbit", ANIMAL, TFCRabbit.class);
		registry.register("wild_animal", ANIMAL, WildAnimal.class);
		registry.register("dromedary_camel", TFG_ANIMAL, TFCDromedaryCamel.class);
		registry.register("bactrian_camel", TFG_ANIMAL, TFCBactrianCamel.class);
		registry.register("frog", FROG, TFCFrog.class);
		registry.register("squid", SQUID, TFCSquid.class);
		registry.register("fish", FISH, WaterAnimal.class);
		registry.register("predator", PREDATOR, Predator.class);
		registry.register("pack_predator", PACK_PREDATOR, PackPredator.class);
		registry.register("ocelot", OCELOT, TFCOcelot.class);
		registry.register("fishing_hook", HOOK, TFCFishingHook.class);
		registry.register("rabbit", TFG_RABBIT, Rabbit.class);
		registry.register("surfer", TFG_SURFER, TFCSurfer.class);
		registry.register("soarer", TFG_SOARER, Soarer.class);
		registry.register("axolotl", TFG_AXOLOTL, Axolotl.class);
		registry.register("charger", TFG_CHARGER, Charger.class);
		registry.register("snatcher", TFG_SNATCHER, Snatcher.class);
		registry.register("wolf", TFG_WOLF, PackPredator.class);
		registry.register("dog", TFG_DOG, Dog.class);
		registry.register("leopard_seal", TFC_1_21, TFCLeopardSeal.class);
		registry.register("bison", TFC_1_21, TFCBison.class);
		registry.register("lemming", TFC_1_21, TFCLemming.class);
		registry.register("jerboa", TFC_1_21, TFCJerboa.class);
		registry.register("mongoose", TFC_1_21, TFCMongoose.class);
		registry.register("fox", TFC_FOX, TFCFox.class);
		registry.register("tamed_fox", TFG_FOX, TFGFox.class);
    }

    public static final EntityTooltip FROG = (level, entity, tooltip) -> {
        if (entity instanceof TFCFrog frog)
        {
            tooltip.accept(Helpers.translateEnum(frog.isMale() ? TFCAnimalProperties.Gender.MALE : TFCAnimalProperties.Gender.FEMALE));
            final float familiarity = Math.max(0.0F, Math.min(1.0F, frog.getFamiliarity()));
            final String familiarityPercent = String.format("%.2f", familiarity * 100);
            tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));

        }
    };

    public static final EntityTooltip ANIMAL = (level, entity, tooltip) -> {
        if (entity instanceof WildAnimal animal)
        {
            if (animal.displayMaleCharacteristics())
            {
                tooltip.accept(Helpers.translateEnum(TFCAnimalProperties.Gender.MALE));
            }
            else if (animal.displayFemaleCharacteristics())
            {
                tooltip.accept(Helpers.translateEnum(TFCAnimalProperties.Gender.FEMALE));
            }
            if (animal.isBaby())
            {
                tooltip.accept(Component.translatable("tfc.jade.juvenile"));
            }
        }
        if (entity instanceof TFCAnimalProperties animal)
        {
            final MutableComponent line1 = Helpers.translateEnum(animal.getGender());

            if (animal.isFertilized())
            {
                line1.append(", ").append(Component.translatable("tfc.tooltip.fertilized"));
            }
            final float familiarity = Math.max(0.0F, Math.min(1.0F, animal.getFamiliarity()));
            final String familiarityPercent = String.format("%.2f", familiarity * 100);

            final TFCAnimalProperties.Age age = animal.getAgeType();
            ChatFormatting familiarityStyle = ChatFormatting.GRAY;
            if (familiarity >= animal.getAdultFamiliarityCap() && age != TFCAnimalProperties.Age.CHILD)
            {
                familiarityStyle = ChatFormatting.RED;
            }
            else if (familiarity >= TFCConfig.SERVER.familiarityDecayLimit.get())
            {
                familiarityStyle = ChatFormatting.WHITE;
            }
            line1.append(", ").append(Component.translatable("tfc.jade.familiarity", familiarityPercent).withStyle(familiarityStyle));
            tooltip.accept(line1);
            tooltip.accept(Component.translatable("tfc.jade.animal_size", animal.getGeneticSize()));
            if (animal.isReadyForAnimalProduct())
            {
                tooltip.accept(animal.getProductReadyName().withStyle(ChatFormatting.GREEN));
            }
            if (animal.isReadyToMate())
            {
                tooltip.accept(Component.translatable("tfc.jade.can_mate"));
            }

            // when the animal is 'used up' but hasn't hit its asynchronous old day yet
            final double usageRatio = animal.getUses() >= animal.getUsesToElderly() ? 0.99 : (float) animal.getUses() / animal.getUsesToElderly();
            switch (age)
            {
                case CHILD -> tooltip.accept(Component.translatable("tfc.jade.adulthood_progress", Calendars.get(level).getTimeDelta(ICalendar.TICKS_IN_DAY * (animal.getDaysToAdulthood() + animal.getBirthDay() - Calendars.get(level).getTotalDays()))));
                case ADULT -> tooltip.accept(Component.translatable("tfc.jade.animal_wear", String.format("%d%%", Math.min(100, Math.round(100f * usageRatio)))));
                case OLD -> tooltip.accept(Component.translatable("tfc.jade.old_animal"));
            }

        }
        if (entity instanceof MammalProperties mammal)
        {
            if (mammal.getPregnantTime() > 0)
            {
                tooltip.accept(Component.translatable("tfc.tooltip.animal.pregnant", entity.getName().getString()));

                final ICalendar calendar = Calendars.get(level);
                tooltip.accept(Component.translatable("tfc.jade.gestation_time_left", calendar.getTimeDelta(ICalendar.TICKS_IN_DAY * (mammal.getGestationDays() + mammal.getPregnantTime() - Calendars.get(level).getTotalDays()))));
            }
        }
        if (entity instanceof HorseProperties horse)
        {
            if (horse.getFamiliarity() >= HorseProperties.TAMED_FAMILIARITY)
            {
                tooltip.accept(Component.translatable("tfc.jade.may_ride_horse"));
            }
            if (entity instanceof TFCHorse tfcHorse)
            {
                tooltip.accept(Component.translatable("tfc.jade.variant_and_markings", Helpers.translateEnum(tfcHorse.getVariant(), "horse_variant"), Helpers.translateEnum(tfcHorse.getMarkings())));
            }
            if (entity instanceof TFCChestedHorse chested && !chested.getChestItem().isEmpty())
            {
                final MutableComponent component = chested.getChestItem().getHoverName().copy();
                chested.getChestItem().getCapability(Capabilities.FLUID_ITEM).map(cap -> cap.getFluidInTank(0)).filter(f -> !f.isEmpty()).ifPresent(fluid -> component.append(", ").append(Tooltips.fluidUnitsOf(fluid)));
                tooltip.accept(component);
            }
        }
    };

    public static final EntityTooltip PACK_PREDATOR = (level, entity, tooltip) -> {
        if (entity instanceof PackPredator predator)
        {
            tooltip.accept(Component.translatable("tfc.jade.pack_respect", predator.getRespect()));
            if (predator.isTamable())
            {
                final String familiarityPercent = String.format("%.2f", predator.getFamiliarity() * 100);
                tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));
            }
        }
    };

    public static final EntityTooltip OCELOT = (level, entity, tooltip) -> {
        if (entity instanceof TFCOcelot ocelot)
        {
            final String familiarityPercent = String.format("%.2f", ocelot.getFamiliarity() * 100);
            tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));
        }
    };

    public static final EntityTooltip SQUID = (level, entity, tooltip) -> {
        if (entity instanceof TFCSquid squid)
        {
            tooltip.accept(Component.translatable("tfc.jade.squid_size", squid.getSize()));
        }
    };

    public static final EntityTooltip FISH = (level, entity, tooltip) -> {
        if (entity instanceof AquaticMob aquatic)
        {
            if (aquatic.canSpawnIn(TFCFluids.SALT_WATER.getSource()))
            {
                tooltip.accept(Component.translatable("tfc.jade.saltwater"));
            }
            if (aquatic.canSpawnIn(Fluids.WATER))
            {
                tooltip.accept(Component.translatable("tfc.jade.freshwater"));
            }
            if (Helpers.isEntity(entity, TFCTags.Entities.NEEDS_LARGE_FISHING_BAIT))
            {
                tooltip.accept(Component.translatable("tfc.jade.large_bait"));
            }
        }
    };

    public static final EntityTooltip PREDATOR = (level, entity, tooltip) -> {
        if (entity instanceof Predator predator)
        {
            tooltip.accept(predator.isDiurnal() ? Component.translatable("tfc.jade.diurnal") : Component.translatable("tfc.jade.nocturnal"));
        }
    };

    public static final EntityTooltip RABBIT = (level, entity, tooltip) -> {
        if (entity instanceof Rabbit rabbit)
        {
            tooltip.accept(Helpers.translateEnum(rabbit.getVariant(), "rabbit_variant"));
        }
    };

    public static final EntityTooltip HOOK = (level, entity, tooltip) -> {
        if (entity instanceof TFCFishingHook hook)
        {
            if (hook.getHookedIn() != null)
            {
                tooltip.accept(Component.translatable("tfc.jade.hooked", hook.getHookedIn().getName()));
            }
            if (!hook.getBait().isEmpty())
            {
                tooltip.accept(Component.translatable("tfc.jade.bait", hook.getBait().getHoverName()));
            }
        }
    };

	private static final EntityTooltip TFG_WOLF = (level, entity, tooltip) -> {
		if (entity instanceof TFCWolfInterface wolf && entity instanceof PackPredator predator) {
			if (predator.isTamable()) {
				tooltip.accept(Helpers.translateEnum(wolf.tfg$getVariant(), "TFCWolfVariant"));
			}
		}
	};

	private static final EntityTooltip TFG_DOG = (level, entity, tooltip) -> {
		if (entity instanceof TFCWolfInterface dog) {
			tooltip.accept(Helpers.translateEnum(dog.tfg$getVariant(), "TFCWolfVariant"));
		}
	};

	private static final EntityTooltip TFC_FOX = (level, entity, tooltip) -> {
		if (entity instanceof TFCFox fox) {
			String familiarityPercent = String.format("%.2f", FoxData.getFamiliarity(fox) * 100.0F);
			tooltip.accept(Component.translatable("tfc.jade.familiarity", new Object[] { familiarityPercent }));
			tooltip.accept(Component.translatable(
				(TFGCore.MOD_ID + ".tooltip.tamed_fox.variant." + fox.getVariant().name())
					.toLowerCase(Locale.ROOT)));
		}
	};

	private static final EntityTooltip TFG_FOX = (level, entity, tooltip) -> {
		if (entity instanceof TFGFox fox) {
			tooltip.accept(Component.translatable(
				(TFGCore.MOD_ID + ".tooltip.tamed_fox.variant." + fox.getVariant().name())
					.toLowerCase(Locale.ROOT)));
		}
	};

	private static final EntityTooltip TFG_RABBIT = (level, entity, tooltip) -> {
		if (entity instanceof MoonRabbit moonRabbit) {
			tooltip.accept(Component.translatable(
				(TFGCore.MOD_ID + ".tooltip.moon_rabbit_variant." + moonRabbit.getMoonVariant().name())
					.toLowerCase(Locale.ROOT)));
		} else if (entity instanceof Rabbit rabbit) {
			tooltip.accept(Helpers.translateEnum(rabbit.getVariant(), "rabbit_variant"));
		}
	};

	private static final EntityTooltip TFG_SURFER = (level, entity, tooltip) -> {
		if (entity instanceof TFCSurfer surfer) {
			tooltip.accept(Component.translatable((TFGCore.MOD_ID + ".tooltip.surfer_variant." + surfer.getVariant().getPath()).toLowerCase(Locale.ROOT)));

			tooltip.accept(Helpers.translateEnum(surfer.isMale() ? TFCAnimalProperties.Gender.MALE : TFCAnimalProperties.Gender.FEMALE));

			float familiarity = Math.max(0.0F, Math.min(1.0F, surfer.getFamiliarity()));
			String familiarityPercent = String.format("%.2f", familiarity * 100.0F);
			tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));

			tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.surfer"));
		}
	};

	private static final EntityTooltip TFG_SOARER = (level, entity, tooltip) -> {
		if (entity instanceof Soarer soarer) {
			tooltip.accept(Helpers.translateEnum(SoarerData.isMale(soarer) ? TFCAnimalProperties.Gender.MALE : TFCAnimalProperties.Gender.FEMALE));

			float familiarity = Math.max(0.0F, Math.min(1.0F, SoarerData.getFamiliarity(soarer)));
			String familiarityPercent = String.format("%.2f", familiarity * 100.0F);
			tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));
		}
	};

	private static final EntityTooltip TFG_CHARGER = (level, entity, tooltip) -> {
		if (entity instanceof Charger charger) {
			tooltip.accept(Helpers.translateEnum(ChargerData.isMale(charger) ? TFCAnimalProperties.Gender.MALE : TFCAnimalProperties.Gender.FEMALE));

			float familiarity = Math.max(0.0F, Math.min(1.0F, ChargerData.getFamiliarity(charger)));
			String familiarityPercent = String.format("%.2f", familiarity * 100.0F);
			tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));
		}
	};

	private static final EntityTooltip TFG_SNATCHER = (level, entity, tooltip) -> {
		if (entity instanceof Snatcher snatcher) {
			tooltip.accept(Helpers.translateEnum(SnatcherData.isMale(snatcher) ? TFCAnimalProperties.Gender.MALE : TFCAnimalProperties.Gender.FEMALE));

			float familiarity = Math.max(0.0F, Math.min(1.0F, SnatcherData.getFamiliarity(snatcher)));
			String familiarityPercent = String.format("%.2f", familiarity * 100.0F);
			tooltip.accept(Component.translatable("tfc.jade.familiarity", familiarityPercent));
		}
	};

	private static final EntityTooltip TFG_AXOLOTL = (level, entity, tooltip) -> {
		if (entity instanceof Axolotl axolotl) {
			tooltip.accept(Helpers.translateEnum(AxolotlData.isMale(axolotl) ? TFCAnimalProperties.Gender.MALE : TFCAnimalProperties.Gender.FEMALE));
		}
	};

	private static final EntityTooltip TFC_1_21 = (level, entity, tooltip) -> {
		tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.tfc"));
	};

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
			final MutableComponent line1 = Component.empty();
			boolean genderless = entity.getType().is(JelliesTags.Entities.GENDERLESS);
			if (!genderless) {
				line1.append(Helpers.translateEnum(animal.getGender()));
			}

			if (animal.isFertilized()) {
				if (!genderless) {
					line1.append(", ");
				}
				line1.append(Component.translatable("tfc.tooltip.fertilized"));
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
			if (!genderless || animal.isFertilized()) {
				line1.append(", ");
			}
			line1.append(
				Component.translatable("tfc.jade.familiarity", familiarityPercent).withStyle(familiarityStyle));
			tooltip.accept(line1);
			tooltip.accept(Component.translatable("tfc.jade.animal_size", animal.getGeneticSize()));
			if (animal.isReadyForAnimalProduct()) {
				tooltip.accept(animal.getProductReadyName().withStyle(ChatFormatting.GREEN));
			}
			if (animal instanceof TFCSniffer sniffer) {
				if (sniffer.isReadyForWoolProduct())
					tooltip.accept(sniffer.getWoolReadyName().withStyle(ChatFormatting.GREEN));
			}
			if (animal instanceof TFCWraptor wraptor) {
				if (wraptor.isReadyForWoolProduct())
					tooltip.accept(wraptor.getWoolReadyName().withStyle(ChatFormatting.GREEN));
			}
			if (animal.isReadyToMate()) {
				tooltip.accept(Component.translatable("tfc.jade.can_mate"));
			}

			// when the animal is 'used up' but hasn't hit its asynchronous old day yet
			final double usageRatio = animal.getUses() >= animal.getUsesToElderly() ? 0.99
										  : (float) animal.getUses() / animal.getUsesToElderly();
			switch (age) {
				case CHILD ->
					tooltip.accept(Component.translatable("tfc.jade.adulthood_progress",
						Calendars.get(level).getTimeDelta(ICalendar.TICKS_IN_DAY * (animal.getDaysToAdulthood()
																						+ animal.getBirthDay() - Calendars.get(level).getTotalDays()))));
				case ADULT ->
					tooltip.accept(Component.translatable("tfc.jade.animal_wear",
						String.format("%d%%", Math.min(100, Math.round(100f * usageRatio)))));
				case OLD -> tooltip.accept(Component.translatable("tfc.jade.old_animal"));
			}

		}
		if (entity instanceof MammalProperties mammal) {
			if (mammal.getPregnantTime() > 0) {
				tooltip.accept(Component.translatable("tfc.tooltip.animal.pregnant", entity.getName().getString()));

				final ICalendar calendar = Calendars.get(level);
				tooltip.accept(Component.translatable("tfc.jade.gestation_time_left",
					calendar.getTimeDelta(ICalendar.TICKS_IN_DAY * (mammal.getGestationDays()
																		+ mammal.getPregnantTime() - Calendars.get(level).getTotalDays()))));
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
		if (entity instanceof TFCDromedaryCamel) {
			tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.dromedary_camel"));
		}
		if (entity instanceof TFCBactrianCamel) {
			tooltip.accept(Component.translatable(TFGCore.MOD_ID + ".tooltip.attribution.bactrian_camel"));
		}
	};
}
