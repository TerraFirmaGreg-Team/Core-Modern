package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.dries007.tfc.common.entities.livestock.DairyAnimal;
import net.dries007.tfc.common.entities.livestock.ProducingMammal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.common.entity.TFGWoolEggProducingAnimal;
import su.terrafirmagreg.core.common.tfgt.recipe.condition.AnimalPresentCondition;

public class PastoralEngineMachine extends WorkableElectricMultiblockMachine {

    @SaveField
    @Getter
    @Setter
    private int harvestCounter = 0;

    private static final int HARVESTS_PER_USE = 2; // Number of time it harvests before it ages the animal

    public PastoralEngineMachine(BlockEntityCreationInfo info) {
        super(info);
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
                (Entity) null,
                getFormedBoundingBox(),
                entity -> {
                    if (!(entity instanceof TFCAnimalProperties animal) ||
                            animal.getAgeType() == TFCAnimalProperties.Age.OLD ||
                            finalCondition != null && !finalCondition.matchesEntity(entity)) {
                        return false;
                    }
                    if (animal instanceof TFGWoolEggProducingAnimal woolAnimal && woolAnimal.hasWool()) {
                        return true;
                    }
                    return animal.isReadyForAnimalProduct();
                });

        for (Entity entity : ready) {
            if (!(entity instanceof TFCAnimalProperties animal))
                continue;

            AnimalProductEvent event = buildEvent(serverLevel, animal);
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                // Animals products put on cooldown
                if (animal instanceof TFGWoolEggProducingAnimal woolAnimal) {
                    woolAnimal.setWoolCooldown();
                } else {
                    animal.setProductsCooldown();
                }

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
        if (animal instanceof TFGWoolEggProducingAnimal woollyeggy) {
            return new AnimalProductEvent(
                    level, woollyeggy.blockPosition(), null, woollyeggy,
                    woollyeggy.getWoolItem(),
                    ItemStack.EMPTY, 1);
        }

        // Fallback for the other ProducingAnimal
        return new AnimalProductEvent(
                level, animal.getEntity().blockPosition(), null, animal,
                ItemStack.EMPTY, ItemStack.EMPTY, 1);
    }

    public AABB getFormedBoundingBox() {
        if (!isFormed()) {
            return new AABB(getBlockPos()).inflate(2.5);
        }

        BlockPos pos = getBlockPos();
        Direction front = getFrontFacing();
        Direction right = front.getClockWise(); // north to east

        BlockPos min = pos
                .relative(front, 1)
                .relative(right.getOpposite(), 2)
                .relative(Direction.DOWN, 2);

        BlockPos max = pos
                .relative(front.getOpposite(), 6)
                .relative(right, 6)
                .relative(Direction.UP, 1);

        return new AABB(
                Math.min(min.getX(), max.getX()),
                Math.min(min.getY(), max.getY()),
                Math.min(min.getZ(), max.getZ()),
                Math.max(min.getX(), max.getX()) + 1,
                Math.max(min.getY(), max.getY()) + 1,
                Math.max(min.getZ(), max.getZ()) + 1);
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        var widgets = super.getWidgetsForDisplay(syncManager);
        if (!isFormed())
            return widgets;

        IntSyncValue harvestCounterValue = new IntSyncValue(this::getHarvestCounter, this::setHarvestCounter);
        syncManager.syncValue("harvestCounter", harvestCounterValue);

        int totalAnimals;
        int readyAnimals;
        int oldAnimals;

        if (!isRemote()) {
            List<Entity> allAnimals = getLevel().getEntities(
                    (Entity) null, getFormedBoundingBox(),
                    entity -> entity instanceof TFCAnimalProperties);

            int old = 0;
            int ready = 0;

            for (Entity e : allAnimals) {
                if (e instanceof TFCAnimalProperties animal) {
                    if (animal.getAgeType() == TFCAnimalProperties.Age.OLD) {
                        old++;
                    } else if (animal instanceof TFGWoolEggProducingAnimal wooly) {
                        if (wooly.hasWool()) {
                            ready++;
                        }
                    } else if (animal.isReadyForAnimalProduct()) {
                        ready++;
                    }
                }
            }

            totalAnimals = allAnimals.size();
            oldAnimals = old;
            readyAnimals = ready;
        } else {
            totalAnimals = 0;
            readyAnimals = 0;
            oldAnimals = 0;
        }

        IntSyncValue totalAnimalsValue = new IntSyncValue(() -> totalAnimals);
        IntSyncValue readyAnimalsValue = new IntSyncValue(() -> readyAnimals);
        IntSyncValue oldAnimalsValue = new IntSyncValue(() -> oldAnimals);

        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.pastoral_engine.animals_total", totalAnimalsValue.getIntValue())
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.pastoral_engine.animals_ready", readyAnimalsValue.getIntValue())
                .withStyle(readyAnimalsValue.getIntValue() > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.pastoral_engine.animals_old", oldAnimalsValue.getIntValue())
                .withStyle(oldAnimalsValue.getIntValue() > 0 ? ChatFormatting.RED : ChatFormatting.GRAY)).asWidget());

        Object2LongMap<ResourceLocation> idToCooldownMap = new Object2LongArrayMap<>();

        if (!isRemote()) {
            List<Entity> allAnimals = getLevel().getEntities(
                    (Entity) null, getFormedBoundingBox(),
                    entity -> entity instanceof TFCAnimalProperties);

            var animalsOnCooldown = allAnimals.stream().filter(PastoralEngineMachine::isEntityValid)
                    .sorted(Comparator.comparingLong(PastoralEngineMachine::getCooldown)).toList();

            for (var entity : animalsOnCooldown) {
                idToCooldownMap.put(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())), getCooldown(entity));
            }

        }

        GenericMapSyncHandler<ResourceLocation, Long> mapHandler = new GenericMapSyncHandler<>(() -> idToCooldownMap, null,
                FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readLong, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeLong,
                null, null, null);

        syncManager.syncValue("entityCooldownMap", mapHandler);

        DynamicLinkedSyncHandler<GenericMapSyncHandler<ResourceLocation, Long>> widgetSyncHandler = new DynamicLinkedSyncHandler<>(mapHandler)
                .widgetProvider((psm, map) -> {
                    if (map.getValue().isEmpty()) {
                        return Text.lang("tfg.machine.pastoral_engine.next_harvest_title")
                                .withStyle(ChatFormatting.YELLOW).asWidget();
                    }

                    ListWidget<IWidget, ?> listWidget = new ListWidget<>().coverChildren();

                    for (var entry : map.getValue().entrySet()) {
                        long totalHours = entry.getValue() / ICalendar.TICKS_IN_HOUR;
                        long days = totalHours / ICalendar.HOURS_IN_DAY;
                        long hours = totalHours % ICalendar.HOURS_IN_DAY;

                        String path = entry.getKey().getPath();
                        String formattedName = Arrays.stream(path.split("_"))
                                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                                .collect(Collectors.joining(" "));

                        if (days > 0) {
                            listWidget.child(Text.lang(
                                    "tfg.machine.pastoral_engine.next_harvest_days",
                                    formattedName, days, hours)
                                    .withStyle(ChatFormatting.YELLOW).asWidget());
                        } else {
                            listWidget.child(Text.lang(
                                    "tfg.machine.pastoral_engine.next_harvest_hours",
                                    formattedName, hours)
                                    .withStyle(ChatFormatting.YELLOW).asWidget());
                        }
                    }
                    return listWidget;
                });

        widgets.add(new DynamicWidget<>().syncHandler(widgetSyncHandler));

        // Always visible (client + server)
        widgets.add(Text.dynamic(() -> Component.translatable("tfg.machine.pastoral_engine.next_use",
                harvestCounterValue.getIntValue(), HARVESTS_PER_USE).withStyle(ChatFormatting.AQUA)).asWidget());
        return widgets;
    }

    private static boolean isEntityValid(Entity e) {
        return !(e instanceof TFCAnimalProperties animal) ||
                (animal.getAgeType() == TFCAnimalProperties.Age.ADULT) ||
                (animal instanceof TFGWoolEggProducingAnimal woolAnimal) && (!woolAnimal.hasWool()) ||
                (animal instanceof ProducingMammal producer) &&
                        !(animal.isReadyForAnimalProduct() || producer.getProducedTick() > 0);
    }

    private static long getCooldown(Entity e) {
        if (e instanceof TFGWoolEggProducingAnimal woolAnimal) {
            return woolAnimal.getWoolCooldown();
        }
        assert e instanceof TFCAnimalProperties;
        return ((TFCAnimalProperties) e).getProductsCooldown();
    }
}
