package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.item.RoadMarkingStencilItem;

public final class AsphaltRoadSprayLogic {

    public record SprayContext(InteractionHand hand, ItemStack stack) {
    }

    private AsphaltRoadSprayLogic() {
    }

    @Nullable
    public static SprayContext resolveSprayContext(Player player, InteractionHand usedHand) {
        ItemStack usedStack = player.getItemInHand(usedHand);
        if (isSprayCan(usedStack)) {
            if (!canUseSprayFromHand(player, usedHand)) {
                return null;
            }
            return new SprayContext(usedHand, usedStack);
        }
        InteractionHand oppositeHand = usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack oppositeStack = player.getItemInHand(oppositeHand);
        if (isSprayCan(oppositeStack) && canUseSprayFromHand(player, oppositeHand)) {
            return new SprayContext(oppositeHand, oppositeStack);
        }
        return null;
    }

    public static boolean canUseSprayFromHand(Player player, InteractionHand sprayHand) {
        if (sprayHand == InteractionHand.MAIN_HAND) {
            return true;
        }
        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return mainStack.isEmpty() || RoadMarkingStencilItem.patternFrom(mainStack).isPresent();
    }

    public static AsphaltRoadDecal resolveTargetDecal(Player player, InteractionHand sprayHand) {
        ItemStack opposite = player.getItemInHand(sprayHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return RoadMarkingStencilItem.patternFrom(opposite)
                .map(pattern -> switch (pattern) {
                    case LINE -> decalFromHorizontalFacing(player.getDirection());
                    case CROSS -> AsphaltRoadDecal.CROSS;
                    case ARROW -> arrowFromHorizontalFacing(player.getDirection());
                })
                .orElseGet(() -> decalFromHorizontalFacing(player.getDirection()));
    }

    public static AsphaltRoadDecal decalFromHorizontalFacing(Direction facing) {
        boolean vertical = facing == Direction.NORTH || facing == Direction.SOUTH;
        return vertical ? AsphaltRoadDecal.LINE_VERTICAL : AsphaltRoadDecal.LINE_HORIZONTAL;
    }

    public static AsphaltRoadDecal arrowFromHorizontalFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> AsphaltRoadDecal.ARROW_NORTH;
            case EAST -> AsphaltRoadDecal.ARROW_EAST;
            case SOUTH -> AsphaltRoadDecal.ARROW_SOUTH;
            default -> AsphaltRoadDecal.ARROW_WEST;
        };
    }

    public static AsphaltRoadMarkingColor sprayCanColor(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !"gtceu".equals(id.getNamespace())) {
            return AsphaltRoadMarkingColor.NONE;
        }
        String path = id.getPath();
        if (!path.endsWith("_dye_spray_can")) {
            return AsphaltRoadMarkingColor.NONE;
        }
        String colorName = path.substring(0, path.length() - "_dye_spray_can".length());
        return AsphaltRoadMarkingColor.fromSerializedName(colorName);
    }

    public static boolean isSolventSprayCan(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "gtceu".equals(id.getNamespace()) && "solvent_spray_can".equals(id.getPath());
    }

    public static boolean isSprayCan(ItemStack stack) {
        return isSolventSprayCan(stack) || !sprayCanColor(stack).isNone();
    }

    public static boolean supportsRoadMarking(BlockState state) {
        return state.getBlock() instanceof AsphaltRoadBlock
                || state.getBlock() instanceof AsphaltRoadSlabBlock;
    }

    public static AsphaltRoadDecal currentDecal(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.DECAL);
        }
        return state.getValue(AsphaltRoadSlabBlock.DECAL);
    }

    public static AsphaltRoadMarkingColor currentMarkingColor(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.getValue(AsphaltRoadBlock.COLOR);
        }
        return state.getValue(AsphaltRoadSlabBlock.COLOR);
    }

    public static boolean isSameMarking(BlockState state, AsphaltRoadDecal targetDecal, AsphaltRoadMarkingColor targetColor) {
        return currentDecal(state) == targetDecal && currentMarkingColor(state) == targetColor;
    }

    public static BlockState applyMarking(BlockState state, AsphaltRoadDecal decal, AsphaltRoadMarkingColor color) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.DECAL, decal).setValue(AsphaltRoadBlock.COLOR, color);
        }
        return state.setValue(AsphaltRoadSlabBlock.DECAL, decal).setValue(AsphaltRoadSlabBlock.COLOR, color);
    }

    public static BlockState clearMarking(BlockState state) {
        if (state.getBlock() instanceof AsphaltRoadBlock) {
            return state.setValue(AsphaltRoadBlock.DECAL, AsphaltRoadDecal.NONE)
                    .setValue(AsphaltRoadBlock.COLOR, AsphaltRoadMarkingColor.NONE);
        }
        return state.setValue(AsphaltRoadSlabBlock.DECAL, AsphaltRoadDecal.NONE)
                .setValue(AsphaltRoadSlabBlock.COLOR, AsphaltRoadMarkingColor.NONE);
    }
}
