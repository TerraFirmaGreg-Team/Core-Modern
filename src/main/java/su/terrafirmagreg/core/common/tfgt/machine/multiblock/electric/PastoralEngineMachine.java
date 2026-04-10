package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.dries007.tfc.common.entities.livestock.DairyAnimal;
import net.dries007.tfc.common.entities.livestock.ProducingMammal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.tfgt.recipe.condition.AnimalPresentCondition;

public class PastoralEngineMachine extends WorkableElectricMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PastoralEngineMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int harvestCounter = 0;

    private static final int HARVESTS_PER_USE = 3; // Number of time it harvests before it ages the animal

    public PastoralEngineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        onRecipeFinished();
    }

    private void onRecipeFinished() {
        if (!(getLevel() instanceof ServerLevel serverLevel))
            return;

        harvestCounter++;
        boolean applyUse = harvestCounter >= HARVESTS_PER_USE;
        if (applyUse)
            harvestCounter = 0;

        // Grab condition AnimalPresentCondition from last finished recipe
        AnimalPresentCondition condition = null;
        if (getRecipeLogic().getLastRecipe() != null) {
            for (var c : getRecipeLogic().getLastRecipe().conditions) {
                if (c instanceof AnimalPresentCondition apc) {
                    condition = apc;
                    break;
                }
            }
        }

        final AnimalPresentCondition finalCondition = condition;

        List<Entity> ready = serverLevel.getEntities(
                (Entity) null, getFormedBoundingBox(),
                entity -> entity instanceof TFCAnimalProperties animal
                        && animal.isReadyForAnimalProduct()
                        && animal.getAgeType() != TFCAnimalProperties.Age.OLD // Not old so can still produce
                        // Filter from condition
                        && (finalCondition == null || finalCondition.matchesEntity(entity)));

        for (Entity entity : ready) {
            if (!(entity instanceof TFCAnimalProperties animal))
                continue;

            AnimalProductEvent event = buildEvent(serverLevel, animal);
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                animal.setProductsCooldown(); // Animals products put on cooldown
                /*
                TFGCore.LOGGER.info("[Pastoral] Cooldown appliqué sur {} — cooldown restant: {}",
                        animal.getEntity().getType().getDescriptionId(),
                        animal.getProductsCooldown());
                 */
                if (applyUse) {
                    animal.addUses(event.getUses()); // Age the animal
                }
            } else {
                /*
                TFGCore.LOGGER.info("[Pastoral] Event annulé pour {}",
                        animal.getEntity().getType().getDescriptionId());
                 */
            }
        }
    }

    private AnimalProductEvent buildEvent(ServerLevel level,
            TFCAnimalProperties animal) {
        if (animal instanceof DairyAnimal dairy) {
            return new AnimalProductEvent(
                    level, dairy.blockPosition(), null, dairy,
                    new FluidStack(dairy.getMilkFluid(), FluidHelpers.BUCKET_VOLUME),
                    ItemStack.EMPTY, 1);
        }
        if (animal instanceof WoolyAnimal wooly) {
            return new AnimalProductEvent(
                    level, wooly.blockPosition(), null, wooly,
                    wooly.getWoolItem(),
                    ItemStack.EMPTY, 1);
        }
        // Fallback for the other ProducingAnimal
        return new AnimalProductEvent(
                level, animal.getEntity().blockPosition(), null, animal,
                ItemStack.EMPTY, ItemStack.EMPTY, 1);
    }

    public AABB getFormedBoundingBox() {
        if (!isFormed() || getParts().isEmpty()) {
            return new AABB(getPos()).inflate(2.5);
        }

        BlockPos self = getPos();
        int minX = self.getX(), minY = self.getY(), minZ = self.getZ();
        int maxX = self.getX(), maxY = self.getY(), maxZ = self.getZ();

        for (var part : getParts()) {
            BlockPos p = part.self().getPos();
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }

        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed())
            return;

        // Infos animaux — server only
        if (getLevel() instanceof ServerLevel serverLevel) {
            AABB box = getFormedBoundingBox();

            List<Entity> allAnimals = serverLevel.getEntities(
                    (Entity) null, box,
                    entity -> entity instanceof TFCAnimalProperties);

            int total = allAnimals.size();
            int ready = 0;
            int old = 0;

            for (Entity e : allAnimals) {
                if (e instanceof TFCAnimalProperties animal) {
                    if (animal.getAgeType() == TFCAnimalProperties.Age.OLD) {
                        old++;
                    } else if (animal.isReadyForAnimalProduct()) {
                        ready++;
                    }
                }
            }

            textList.add(Component.translatable("tfg.machine.pastoral_engine.animals_total", total)
                    .withStyle(ChatFormatting.WHITE));
            textList.add(Component.translatable("tfg.machine.pastoral_engine.animals_ready", ready)
                    .withStyle(ready > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            textList.add(Component.translatable("tfg.machine.pastoral_engine.animals_old", old)
                    .withStyle(old > 0 ? ChatFormatting.RED : ChatFormatting.GRAY));

            // Cooldown par type
            boolean hasAnimalOnCooldown = allAnimals.stream()
                    .anyMatch(e -> e instanceof TFCAnimalProperties animal
                            && animal.getAgeType() != TFCAnimalProperties.Age.OLD
                            && !animal.isReadyForAnimalProduct());

            if (hasAnimalOnCooldown) {
                textList.add(Component.translatable("tfg.machine.pastoral_engine.next_harvest_title")
                        .withStyle(ChatFormatting.YELLOW));

                allAnimals.stream()
                        .filter(e -> e instanceof TFCAnimalProperties animal
                                // Filter so only animals that can produce milk have their cooldown checked
                                && e instanceof ProducingMammal producer
                                && animal.getAgeType() == TFCAnimalProperties.Age.ADULT
                                && animal.getGender() == TFCAnimalProperties.Gender.FEMALE
                                && !animal.isReadyForAnimalProduct()
                                && producer.getProducedTick() > 0) // Check that the animal already produced milk at least once
                        .collect(java.util.stream.Collectors.groupingBy(
                                e -> ForgeRegistries.ENTITY_TYPES.getKey(e.getType()),
                                java.util.stream.Collectors.minBy(
                                        java.util.Comparator.comparingLong(
                                                e -> ((TFCAnimalProperties) e).getProductsCooldown()))))
                        .forEach((entityTypeId, optAnimal) -> optAnimal.ifPresent(e -> {
                            long minCooldown = ((TFCAnimalProperties) e).getProductsCooldown();
                            long totalHours = minCooldown / ICalendar.TICKS_IN_HOUR;
                            long days = totalHours / ICalendar.HOURS_IN_DAY;
                            long hours = totalHours % ICalendar.HOURS_IN_DAY;

                            String path = entityTypeId == null ? "unknown" : entityTypeId.getPath();
                            String formattedName = Arrays.stream(path.split("_"))
                                    .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                                    .collect(java.util.stream.Collectors.joining(" "));

                            if (days > 0) {
                                textList.add(Component.translatable(
                                        "tfg.machine.pastoral_engine.next_harvest_days",
                                        formattedName, days, hours)
                                        .withStyle(ChatFormatting.YELLOW));
                            } else {
                                textList.add(Component.translatable(
                                        "tfg.machine.pastoral_engine.next_harvest_hours",
                                        formattedName, hours)
                                        .withStyle(ChatFormatting.YELLOW));
                            }
                        }));
            }
        }
        // Toujours visible (client + server)
        textList.add(Component.translatable("tfg.machine.pastoral_engine.next_use",
                harvestCounter, HARVESTS_PER_USE)
                .withStyle(ChatFormatting.AQUA));
    }
}
