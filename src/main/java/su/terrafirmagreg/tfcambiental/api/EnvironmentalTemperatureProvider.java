package su.terrafirmagreg.tfcambiental.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.capability.TemperatureCapability;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

@FunctionalInterface
public interface EnvironmentalTemperatureProvider {
    static final List<EnvironmentalTemperatureProvider> PROVIDERS = List.of(
            EnvironmentalTemperatureProvider::handleGeneralTemperature,
            EnvironmentalTemperatureProvider::handleWater,
            EnvironmentalTemperatureProvider::handleSprinting,
            EnvironmentalTemperatureProvider::handleTimeOfDay,
            EnvironmentalTemperatureProvider::handleShade,
            EnvironmentalTemperatureProvider::handleCozy,
            EnvironmentalTemperatureProvider::handleRain,
            EnvironmentalTemperatureProvider::handleWind,
            EnvironmentalTemperatureProvider::handleUnderground,
            EnvironmentalTemperatureProvider::handleWetness,
            EnvironmentalTemperatureProvider::handleThirst,
            EnvironmentalTemperatureProvider::handleFood,
            EnvironmentalTemperatureProvider::handleFire);

    static final List<EnvironmentalTemperatureProvider> NETHER_PROVIDERS = List.of(
            EnvironmentalTemperatureProvider::handleGeneralTemperature,
            EnvironmentalTemperatureProvider::handleWater,
            EnvironmentalTemperatureProvider::handleSprinting,
            EnvironmentalTemperatureProvider::handleWetness,
            EnvironmentalTemperatureProvider::handleThirst,
            EnvironmentalTemperatureProvider::handleFood,
            EnvironmentalTemperatureProvider::handleFire);

    Optional<TempModifier> getModifier(Player player);

    static boolean calculateEnclosure(Player player, int radius) {
        PathNavigationRegion region = new PathNavigationRegion(
                player.level(),
                player.getOnPos().above().offset(-radius, -radius, -radius),
                player.getOnPos().above().offset(radius, 400, radius));
        Bee guineaBee = new Bee(EntityType.BEE, player.level());
        guineaBee.setPos(player.getPosition(0));
        guineaBee.setBaby(true);
        guineaBee.setPathfindingMalus(BlockPathTypes.TRAPDOOR, -1.0F);
        FlyNodeEvaluator evaluator = new FlyNodeEvaluator();
        PathFinder finder = new PathFinder(evaluator, 500);
        Path path = finder.findPath(
                region,
                guineaBee,
                Set.of(player.getOnPos().above().atY(258)),
                500,
                0,
                12);
        return path == null || path.getNodeCount() < 255 - player.getOnPos().above().getY();
    }

    static float getEnvironmentTemperatureWithTimeOfDay(Player player) {
        return getEnvironmentTemperature(player) + handleTimeOfDay(player).map(TempModifier::getChange).orElse(0f);
    }

    static float getEnvironmentTemperature(Player player) {
        return Climate.getTemperature(player.level(), player.getOnPos());
    }

    static float getEnvironmentHumidity(Player player) {
        return Climate.getRainfall(player.level(), player.getOnPos()) / 3000;
    }

    static int getSkylight(Player player) {
        BlockPos pos = new BlockPos(player.getOnPos()).above(1);
        return player.level().getBrightness(LightLayer.SKY, pos);
    }

    static int getBlockLight(Player player) {
        BlockPos pos = new BlockPos(player.getOnPos()).above(1);
        return player.level().getBrightness(LightLayer.BLOCK, pos);
    }

    static Optional<TempModifier> handleFire(Player player) {
        if (player.isOnFire()) {
            return TempModifier.defined(4f, 4f, -1f);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleGeneralTemperature(Player player) {
        return Optional.of(new TempModifier(getEnvironmentTemperature(player), getEnvironmentHumidity(player)));
    }

    static Optional<TempModifier> handleTimeOfDay(Player player) {
        int dayTicks = (int) (player.level().dayTime() % 24000);
        if (dayTicks < 6000) {
            return TempModifier.defined(2f, 0);
        } else if (dayTicks < 12000) {
            return TempModifier.defined(4f, 0, -0.02f);
        } else if (dayTicks < 18000) {
            return TempModifier.defined(1f, 0);
        } else {
            return TempModifier.defined(1f, 0);
        }
    }

    static Optional<TempModifier> handleWater(Player player) {
        if (player.isInWater()) {
            BlockPos pos = player.getOnPos().above();
            BlockState state = player.level().getBlockState(pos);
            if (state.getFluidState().is(TFCAmbiental.SPRING_WATER)) {
                return TempModifier.defined(5f, 6f, 10f);
            } else if (state.getBlock() == Blocks.LAVA) {
                return TempModifier.defined(10f, 5f, -10f);
            }
            return TempModifier.defined(-5f, 6f, 10f);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleRain(Player player) {
        if (player.level().isRaining()) {
            var isInRain = player.level().isRainingAt(player.blockPosition());
            if (getSkylight(player) < 15) {
                return TempModifier.defined(-2f, 0.1f, isInRain ? 0.5f : 0);
            }
            return TempModifier.defined(-4f, 0.3f, isInRain ? 4f : 0.5f);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleWind(Player player) {
        return player.getCapability(TemperatureCapability.CAPABILITY).map(temperatureCapability -> {
            var wind = Climate.getWindVector(player.level(), player.blockPosition());
            float temperature = temperatureCapability.getTemperature();
            float targetTemperature = temperatureCapability.getTargetTemperature();
            float potency = temperature < targetTemperature ? 0.1f * temperatureCapability.getWetness() * wind.length()
                    : 0f;
            float change = temperature > EnvironmentalTemperatureProvider.getEnvironmentTemperature(player) - 3f
                    ? -0.01f
                    : 0f;
            return TempModifier.defined(change * wind.length(), potency);
        }).orElse(TempModifier.none());
    }

    static Optional<TempModifier> handleSprinting(Player player) {
        if (player.isSprinting()) {
            return TempModifier.defined(2f, 0.3f, -0.05f);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleUnderground(Player player) {
        if (getSkylight(player) < 2) {
            return TempModifier.defined(-6f, 0.2f);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleShade(Player player) {
        int light = Math.max(12, getSkylight(player));
        if (light < 15) {
            float temp = getEnvironmentTemperatureWithTimeOfDay(player);
            float avg = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
            if (temp > avg) {
                return TempModifier.defined(-Math.abs(avg - temp) * 0.6f, 0f);
            }
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleCozy(Player player) {
        if (player.isUnderWater()) {
            return TempModifier.none();
        }
        if (TFCAmbientalConfig.COMMON.indoorCheckTickModifier.get() > 0) {
            float temp = getEnvironmentTemperatureWithTimeOfDay(player);
            float avg = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();

            if (temp < avg - 1) {
                final boolean[] isInside = { false };
                player.getCapability(TemperatureCapability.CAPABILITY).ifPresent(temperatureCapability -> {
                    if (player.tickCount % TFCAmbientalConfig.COMMON.indoorCheckTickModifier.get() == 0) {
                        temperatureCapability
                                .setInside(EnvironmentalTemperatureProvider.calculateEnclosure(player, 30));
                    }
                    isInside[0] = temperatureCapability.isInside();
                });

                if (isInside[0]) {
                    return TempModifier.defined(Math.abs(avg - 1 - temp) * 0.6f, 0f);
                }
            }
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleThirst(Player player) {
        if (player.getFoodData() instanceof TFCFoodData stats) {
            if (getEnvironmentTemperatureWithTimeOfDay(
                    player) > TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue() + 3) {
                if (stats.getThirst() > 80f) {
                    return TempModifier.defined(-2.5f, 0f);
                } else if (stats.getThirst() < 5f) {
                    return TempModifier.defined(2.5f, 0f);
                }
            }
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleFood(Player player) {
        if (player.getFoodData().getFoodLevel() > 14 && getEnvironmentTemperatureWithTimeOfDay(
                player) < TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue() - 3) {
            return TempModifier.defined(2.5f, 0f);
        }
        return TempModifier.none();
    }

    static Optional<TempModifier> handleWetness(Player player) {
        return player.getCapability(TemperatureCapability.CAPABILITY).map(temperatureCapability -> {
            // TODO Wool clothing halves the effect of wetness
            var mod = -0.01f;
            var potency = 0.2f;
            if (temperatureCapability.getWetness() > 1.5f && !player.isInWater()) {
                var envTemperature = getEnvironmentTemperature(player);
                potency = envTemperature < temperatureCapability.getTemperature() ? 5.5f : potency;
            }
            return TempModifier.defined(mod * temperatureCapability.getWetness(), potency,
                    !player.isInWater() ? -0.03f : 0);
        }).orElse(TempModifier.none());
    }

    static void evaluateAll(Player player, TempModifierStorage storage) {
        evaluateAll(player, storage, false);
    }

    static void evaluateAll(Player player, TempModifierStorage storage, boolean nether) {
        if (nether) {
            for (EnvironmentalTemperatureProvider provider : NETHER_PROVIDERS) {
                storage.add(provider.getModifier(player));
            }
        } else {
            for (EnvironmentalTemperatureProvider provider : PROVIDERS) {
                storage.add(provider.getModifier(player));
            }
        }
    }
}
