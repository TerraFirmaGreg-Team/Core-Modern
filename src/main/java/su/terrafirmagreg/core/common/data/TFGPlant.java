/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.data;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.*;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import su.terrafirmagreg.core.common.block.*;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;

public enum TFGPlant implements RegistryPlant {
    // Short grasses
    RED_OAT_GRASS(BlockType.SHORT_GRASS, 0.9F, new int[] { 0, 0, 1, 1, 2, 2, 3, 3, 3, 1, 0, 0 }),

    // Intertidal Plant-Likes
    ANEMONE_GREEN(BlockType.OCEAN_CREEPING, 1.0F),
    ANEMONE_PURPLE(BlockType.OCEAN_CREEPING, 1.0F),
    ANEMONE_LARGE_ORANGE(BlockType.OCEAN_ROTATABLE, 1.0F),
    ANEMONE_LARGE_PURPLE(BlockType.OCEAN_ROTATABLE, 1.0F),
    BARNACLES(BlockType.OCEAN_ROCK_CREEPING, 1.0F),
    MUSSELS(BlockType.OCEAN_ROCK_CREEPING, 1.0F),
    STARFISH(BlockType.OCEAN_ROTATABLE, 1.0F),

    // Other plants
    AZALEA(BlockType.TALL_SHRUB, 0.4f, new int[] { 3, 3, 1, 0, 0, 1, 1, 1, 2, 2, 3, 3 }),
    BEAR_GRASS(BlockType.TALL_GRASS, 0.6F, new int[] { 3, 3, 3, 3, 1, 0, 0, 1, 1, 1, 2, 2 }),
    BIRD_NEST_FERN(BlockType.PERCHED_EPIPHYTE, 0.9f),
    BUTTERCUP(BlockType.FLOWERBED, 1.0F),
    CORNFLOWER(BlockType.STANDARD, 1F, new int[] { 2, 2, 2, 0, 1, 1, 1, 2, 2, 2, 2, 2 }),
    DRY_GRASS(BlockType.DRY, 0.9F),
    EDELWEISS(BlockType.STANDARD, 1.0F, new int[] { 3, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2 }),
    ELEGANT_SUNBURST_LICHEN(BlockType.CREEPING_STONE, 1.0F),
    FAN_PALM(BlockType.TALL_GRASS, 0.6f),
    KINNIKINNICK(BlockType.SHORT_SHRUB, 0.9F, new int[] { 3, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2 }),
    MOUNTAIN_HULLWORT(BlockType.TALL_SHRUB, 0.4f),
    MOSS_CAMPION(BlockType.DRY, 0.9F, new int[] { 3, 3, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3 }),
    PALASH(BlockType.TALL_SHRUB, 0.4f, new int[] { 3, 3, 3, 0, 0, 1, 1, 1, 2, 2, 3, 3 }),
    PENWORTEL(BlockType.SHRUB, 0.4f, new int[] { 3, 3, 3, 0, 0, 1, 1, 1, 2, 2, 3, 3 }),
    PRICKLY_PEAR(BlockType.PASSABLE_CACTUS, 0.3F, new int[] { 3, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2 }),
    PRICKLY_PEAR_PURPLE(BlockType.PASSABLE_CACTUS, 0.3F, new int[] { 3, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2 }),
    QANTU(BlockType.SHRUB, 0.4f, new int[] { 3, 3, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3 }),
    RAMIREZELLA(BlockType.EPIPHYTE, 1.0f),
    RAMUNDA(BlockType.STANDARD, 1.0F, new int[] { 3, 3, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3 }),
    SHAWIASH(BlockType.SHRUB, 0.9F, new int[] { 3, 3, 3, 0, 0, 1, 1, 1, 2, 2, 3, 3 }),
    SILKEN_PINCUSHION_CACTUS(BlockType.CACTUSBED, 0f, new int[] { 3, 3, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3 }),
    SILVER_BROMELIAD(BlockType.PERCHED_EPIPHYTE, 0.9f),
    TANK_BROMELIAD(BlockType.PERCHED_EPIPHYTE, 0.9f),
    YELLOW_SAXIFRAGE(BlockType.FLOWERBED, 1.0F, new int[] { 3, 3, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3 }),

    // Unique
    FLAME_VINE_PLANT(BlockType.WEEPING, 1.0F),
    FLAME_VINE(BlockType.WEEPING_TOP, 1.0F),
    CYCAD_PLANT(BlockType.TWISTING_SOLID, 0F),
    CYCAD(BlockType.TWISTING_SOLID_TOP, 0F);

    private final float speedFactor;
    @Nullable
    private final IntegerProperty property;
    private final int @Nullable [] stagesByMonth;
    private final TFGPlant.BlockType type;

    TFGPlant(BlockType type, float speedFactor) {
        this(type, speedFactor, null);
    }

    TFGPlant(BlockType type, float speedFactor, int @Nullable [] stagesByMonth) {
        this.type = type;
        this.speedFactor = speedFactor;
        this.stagesByMonth = stagesByMonth;

        int maxStage = 0;
        if (stagesByMonth != null) {
            maxStage = Arrays.stream(stagesByMonth).max().orElse(0);
        }

        this.property = maxStage > 0 ? TFCBlockStateProperties.getStageProperty(maxStage) : null;
    }

    public Block create() {
        return type.factory.apply(this, type);
    }

    @Override
    public int stageFor(Month month) {
        assert stagesByMonth != null;
        return stagesByMonth.length < month.ordinal() ? 0 : stagesByMonth[month.ordinal()];
    }

    @Override
    @Nullable
    public IntegerProperty getStageProperty() {
        return property;
    }

    private Supplier<? extends Block> transform() {
        return TFGBlocks_Earth.PLANTS.get(switch (this) {
            case CYCAD -> CYCAD_PLANT;
            case CYCAD_PLANT -> CYCAD;
            case FLAME_VINE -> FLAME_VINE_PLANT;
            case FLAME_VINE_PLANT -> FLAME_VINE;
            default -> throw new IllegalStateException("Uhh why did you try to transform something that's not a tall plant?");
        });
    }

    // Why isn't yours public, TFC?
    enum BlockType {
        STANDARD((plant, type) -> PlantBlock.create(plant, fire(nonSolid(plant)).offsetType(BlockBehaviour.OffsetType.XZ))),
        CREEPING_STONE((plant, type) -> CreepingPlantBlock.createStone(plant, fire(nonSolid(plant).hasPostProcess(TFCBlocks::always)))),
        EPIPHYTE((plant, type) -> EpiphytePlantBlock.create(plant, fire(nonSolid(plant).hasPostProcess(TFCBlocks::always)))),
        SHORT_GRASS((plant, type) -> ShortGrassBlock.create(plant, fire(nonSolid(plant)).offsetType(BlockBehaviour.OffsetType.XZ))),
        DRY((plant, type) -> PlantBlock.createDry(plant, fire(nonSolid(plant).offsetType(BlockBehaviour.OffsetType.XZ)))),
        TALL_GRASS((plant, type) -> TFCTallGrassBlock.create(plant, fire(nonSolid(plant)).offsetType(BlockBehaviour.OffsetType.XZ))),
        SHORT_SHRUB((plant, type) -> TFGPlantBlock.createShortShrub(plant, fire(nonSolid(plant)))),
        SHRUB((plant, type) -> TFGPlantBlock.createShrub(plant, fire(nonSolid(plant)))),
        FLOWERBED((plant, type) -> PlantBlock.createFlat(plant, fire(nonSolid(plant)))),
        CACTUSBED((plant, type) -> CactusBedBlock.createBarrel(plant, fire(solid().strength(0.25F).sound(SoundType.WOOL).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape()))),
        PASSABLE_CACTUS((plant, type) -> PassableCactusBlock.create(plant, fire(nonSolid(plant).strength(0.25F).sound(SoundType.WOOL)).pathType(BlockPathTypes.DAMAGE_OTHER))),
        PERCHED_EPIPHYTE((plant, type) -> TFGPlantBlock.createPerchedEpiphyte(plant, fire(nonSolid(plant)).offsetType(BlockBehaviour.OffsetType.XZ))),
        TALL_SHRUB((plant, type) -> TallShrubBlock.create(plant, fire(nonSolid(plant)))),
        FLOATING_FRESH((plant, type) -> FloatingWaterPlantBlock.create(plant, () -> Fluids.WATER, nonSolid(plant).offsetType(BlockBehaviour.OffsetType.XZ))),
        WEEPING((plant, type) -> new BodyPlantBlock(fire(nonSolidTallPlant(plant)), plant.transform(), BodyPlantBlock.BODY_SHAPE, Direction.DOWN)),
        WEEPING_TOP((plant, type) -> new TopPlantBlock(fire(nonSolidTallPlant(plant)), plant.transform(), Direction.DOWN, BodyPlantBlock.WEEPING_SHAPE)),
        TWISTING_SOLID((plant, type) -> new BodyPlantBlock(fire(solidTallPlant()), plant.transform(), BodyPlantBlock.BODY_SHAPE, Direction.UP)),
        TWISTING_SOLID_TOP((plant, type) -> new TopPlantBlock(fire(solidTallPlant()), plant.transform(), Direction.UP, BodyPlantBlock.TWISTING_SHAPE)),
        OCEAN_ROCK_CREEPING((plant, type) -> CreepingWaterPlantBlock.createRock(plant, TFCBlockStateProperties.SALT_WATER, ExtendedProperties.of(nonSolid(plant).sound(SoundType.BASALT)))),
        OCEAN_CREEPING((plant, type) -> CreepingWaterPlantBlock.create(plant, TFCBlockStateProperties.SALT_WATER, ExtendedProperties.of(nonSolid(plant).sound(SoundType.BASALT)))),
        OCEAN_ROTATABLE((plant, type) -> RotatableWaterPlantBlock.create(plant, TFCBlockStateProperties.SALT_WATER, ExtendedProperties.of(nonSolid(plant).sound(SoundType.SLIME_BLOCK))));

        /**
         * Default properties to avoid rewriting them out every time
         */
        private static BlockBehaviour.Properties solid() {
            return Block.Properties.of().instabreak().noOcclusion().sound(SoundType.GRASS).randomTicks().pushReaction(PushReaction.DESTROY);
        }

        private static BlockBehaviour.Properties nonSolid(TFGPlant plant) {
            return solid().replaceable().instabreak().speedFactor(plant.speedFactor).noCollission();
        }

        private static BlockBehaviour.Properties solidTallPlant() {
            return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().noOcclusion().randomTicks().sound(SoundType.WEEPING_VINES).pushReaction(PushReaction.DESTROY);
        }

        private static BlockBehaviour.Properties nonSolidTallPlant(TFGPlant plant) {
            return solidTallPlant().instabreak().noCollission().speedFactor(plant.speedFactor).pushReaction(PushReaction.DESTROY);
        }

        private static ExtendedProperties fire(BlockBehaviour.Properties properties) {
            return ExtendedProperties.of(properties).flammable(60, 30);
        }

        private final BiFunction<TFGPlant, TFGPlant.BlockType, ? extends Block> factory;

        BlockType(BiFunction<TFGPlant, TFGPlant.BlockType, ? extends Block> factory) {
            this.factory = factory;
        }
    }
}
