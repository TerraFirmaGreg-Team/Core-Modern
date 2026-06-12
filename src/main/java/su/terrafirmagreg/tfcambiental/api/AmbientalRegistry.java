package su.terrafirmagreg.tfcambiental.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;

public class AmbientalRegistry<Type> implements Iterable<Type> {
    public static final AmbientalRegistry<ItemTemperatureProvider> ITEMS = new AmbientalRegistry<>();
    public static final AmbientalRegistry<BlockTemperatureProvider> BLOCKS = new AmbientalRegistry<>();
    public static final AmbientalRegistry<BlockEntityTemperatureProvider> BLOCK_ENTITIES = new AmbientalRegistry<>();
    public static final AmbientalRegistry<EnvironmentalTemperatureProvider> ENVIRONMENT = new AmbientalRegistry<>();
    public static final AmbientalRegistry<EquipmentTemperatureProvider> EQUIPMENT = new AmbientalRegistry<>();
    public static final AmbientalRegistry<EntityTemperatureProvider> ENTITIES = new AmbientalRegistry<>();

    static {
        EQUIPMENT.register(EquipmentTemperatureProvider::handleSunlightCap);
        EQUIPMENT.register(EquipmentTemperatureProvider::handleClothes);
        EQUIPMENT.register(EquipmentTemperatureProvider::getEquipmentTempModifier);

        ITEMS.register(ItemTemperatureProvider::handleTemperatureCapability);
        ITEMS.register(ItemTemperatureProvider::handleHotIngots);

        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleCharcoalForge);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleFirePit);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handlePot);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleGrill);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleBloomery);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleLitBlock);
        BLOCK_ENTITIES.register(BlockEntityTemperatureProvider::handleIHeatBlock);

        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("hot_block", 3f, 0.2f, -15f)).filter((mod) -> state.is(TFCAmbiental.HOT_STUFF)));
        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("cold_stuff", -0.5f, 0.2f))
                .filter((mod) -> state.is(TFCAmbiental.COLD_STUFF) && player.level().getBrightness(LightLayer.SKY, pos) == 15));
        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("cold_feet", -0.5f, 0.5f)).filter((mod) -> {
            return state.is(Blocks.SNOW) && EquipmentTemperatureProvider.getEquipmentByType(player, ArmorItem.Type.BOOTS).isEmpty();
        }));
        BLOCKS.register((player, pos, state) -> Optional.of(new TempModifier("warm_block", 1f, 0f, -5f)).filter((mod) -> state.is(TFCAmbiental.WARM_STUFF)));
        BLOCKS.register(BlockEntityTemperatureProvider::getBlockTempModifier);

        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleGeneralTemperature);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleTimeOfDay);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleShade);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleCozy);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleThirst);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleFood);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleDiet);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleFire);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleWater);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleRain);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleWind);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleSprinting);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleUnderground);
        ENVIRONMENT.register(EnvironmentalTemperatureProvider::handleWetness);

        ENTITIES.register(EntityTemperatureProvider::handleHotEntities);
        ENTITIES.register(EntityTemperatureProvider::handleColdEntities);
    }

    private final ArrayList<Type> list = new ArrayList<>();

    private AmbientalRegistry() {
    }

    public void register(Type type) {
        list.add(type);
    }

    @Override
    public Iterator<Type> iterator() {
        return list.iterator();
    }
}
