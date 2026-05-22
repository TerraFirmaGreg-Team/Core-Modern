package su.terrafirmagreg.core.common.data;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import su.terrafirmagreg.core.TFGCore;

public class TFGPartialModels {
    private static final Map<String, PartialModel[]> SEGMENT_MODELS = new HashMap<>();
    private static final Map<String, EnumMap<Direction, PartialModel>> BRACKET_MODELS = new HashMap<>();

    private static volatile Map<Block, PartialModel[]> SEGMENT_MODELS_BY_BLOCK;
    private static volatile Map<Block, EnumMap<Direction, PartialModel>> BRACKET_MODELS_BY_BLOCK;

    public static void init() {
    }

    public static void register() {
    }

    private static final String[] GIRDER_VARIANTS = {
            "girder/beam/tin_alloy",
            "girder/beam/brass",
            "girder/beam/wrought_iron",
            "girder/beam/copper",
            "girder/beam/zinc",
            "girder/truss/tin_alloy",
            "girder/truss/brass",
            "girder/truss/wrought_iron",
            "girder/truss/copper",
            "girder/truss/steel",
            "girder/truss/zinc"
    };

    private static final String[] BEAM_GIRDER_VARIANTS = {
            "girder/beam/tin_alloy",
            "girder/beam/brass",
            "girder/beam/wrought_iron",
            "girder/beam/copper",
            "girder/beam/zinc"
    };

    static {
        for (String variant : GIRDER_VARIANTS) {
            SEGMENT_MODELS.put(variant, new PartialModel[] {
                    block(variant + "/segment_middle"),
                    block(variant + "/segment_top"),
                    block(variant + "/segment_bottom"),
                    block(variant + "/segment_middle_alt")
            });

            EnumMap<Direction, PartialModel> brackets = new EnumMap<>(Direction.class);
            brackets.put(Direction.EAST, block(variant + "/bracket_east"));
            brackets.put(Direction.WEST, block(variant + "/bracket_west"));
            brackets.put(Direction.NORTH, block(variant + "/bracket_north"));
            brackets.put(Direction.SOUTH, block(variant + "/bracket_south"));
            BRACKET_MODELS.put(variant, brackets);
        }
    }

    private static final Map<String, Map<String, PartialModel>> BEAM_GIRDER_CT_POLES = new HashMap<>();
    private static volatile Map<Block, Map<String, PartialModel>> BEAM_GIRDER_CT_POLES_BY_BLOCK;

    static {
        for (String variant : BEAM_GIRDER_VARIANTS) {
            Map<String, PartialModel> poles = new HashMap<>();
            poles.put("top", block(variant + "/block_pole_top"));
            poles.put("middle", block(variant + "/block_pole_middle"));
            poles.put("bottom", block(variant + "/block_pole_bottom"));
            BEAM_GIRDER_CT_POLES.put(variant, poles);
        }
    }

    @Nullable
    public static PartialModel getMetalGirderConnectedPole(Block girderBlock, String key) {
        Map<Block, Map<String, PartialModel>> result = BEAM_GIRDER_CT_POLES_BY_BLOCK;
        if (result == null) {
            result = buildBlockMap(BEAM_GIRDER_CT_POLES);
            BEAM_GIRDER_CT_POLES_BY_BLOCK = result;
        }
        Map<String, PartialModel> poles = result.get(girderBlock);
        if (poles == null)
            return null;
        return poles.get(key);
    }

    private static Map<Block, PartialModel[]> segmentModelsByBlock() {
        Map<Block, PartialModel[]> result = SEGMENT_MODELS_BY_BLOCK;
        if (result == null) {
            result = buildBlockMap(SEGMENT_MODELS);
            SEGMENT_MODELS_BY_BLOCK = result;
        }
        return result;
    }

    private static Map<Block, EnumMap<Direction, PartialModel>> bracketModelsByBlock() {
        Map<Block, EnumMap<Direction, PartialModel>> result = BRACKET_MODELS_BY_BLOCK;
        if (result == null) {
            result = buildBlockMap(BRACKET_MODELS);
            BRACKET_MODELS_BY_BLOCK = result;
        }
        return result;
    }

    private static <V> Map<Block, V> buildBlockMap(Map<String, V> source) {
        Map<Block, V> map = new IdentityHashMap<>();
        for (var entry : source.entrySet()) {
            ResourceLocation id = TFGCore.id(entry.getKey());
            BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> map.put(block, entry.getValue()));
        }
        return map;
    }

    @Nullable
    public static PartialModel getSegmentModel(Block girderBlock, PartialModel original) {
        PartialModel[] models = segmentModelsByBlock().get(girderBlock);
        if (models == null)
            return null;

        if (original == AllPartialModels.GIRDER_SEGMENT_MIDDLE)
            return models[0];
        if (original == AllPartialModels.GIRDER_SEGMENT_TOP)
            return models[1];
        if (original == AllPartialModels.GIRDER_SEGMENT_BOTTOM)
            return models[2];

        return null;
    }

    @Nullable
    public static PartialModel getBracketModel(Block girderBlock, Direction direction) {
        EnumMap<Direction, PartialModel> brackets = bracketModelsByBlock().get(girderBlock);
        if (brackets == null)
            return null;

        return brackets.get(direction);
    }

    @Nullable
    public static PartialModel getAltMiddleModel(Block girderBlock) {
        PartialModel[] models = segmentModelsByBlock().get(girderBlock);
        if (models == null)
            return null;

        return models[3];
    }

    private static PartialModel block(String path) {
        return PartialModel.of(TFGCore.id("block/" + path));
    }
}
