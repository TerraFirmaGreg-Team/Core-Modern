package su.terrafirmagreg.core.common.item.behavior;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch;

import net.dries007.tfc.common.entities.livestock.pet.TamableMammal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.util.AEColor;

import su.terrafirmagreg.core.config.TFGConfig;

public class ChameleonSprayCanBehaviour implements IInteractionItem, IAddInformation {

    private static final ImmutableMap<DyeColor, Block> GLASS_MAP;
    private static final ImmutableMap<DyeColor, Block> GLASS_PANE_MAP;
    private static final ImmutableMap<DyeColor, Block> TERRACOTTA_MAP;
    private static final ImmutableMap<DyeColor, Block> WOOL_MAP;
    private static final ImmutableMap<DyeColor, Block> CARPET_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_POWDER_MAP;
    private static final ImmutableMap<DyeColor, Block> BED_MAP;
    private static final ImmutableMap<DyeColor, Block> BANNER_MAP;

    @SuppressWarnings("deprecation")
    private static Block getBlock(DyeColor color, String postfix) {
        ResourceLocation id = new ResourceLocation("minecraft", color.getSerializedName() + "_" + postfix);
        return BuiltInRegistries.BLOCK.get(id);
    }

    static {
        ImmutableMap.Builder<DyeColor, Block> glassBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> glassPaneBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> terracottaBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> woolBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> carpetBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concreteBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concretePowderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> bedBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> bannerBuilder = ImmutableMap.builder();

        for (DyeColor color : DyeColor.values()) {
            glassBuilder.put(color, getBlock(color, "stained_glass"));
            glassPaneBuilder.put(color, getBlock(color, "stained_glass_pane"));
            terracottaBuilder.put(color, getBlock(color, "terracotta"));
            woolBuilder.put(color, getBlock(color, "wool"));
            carpetBuilder.put(color, getBlock(color, "carpet"));
            concreteBuilder.put(color, getBlock(color, "concrete"));
            concretePowderBuilder.put(color, getBlock(color, "concrete_powder"));

            bedBuilder.put(color, getBlock(color, "bed"));
            bannerBuilder.put(color, getBlock(color, "banner"));
        }
        GLASS_MAP = glassBuilder.build();
        GLASS_PANE_MAP = glassPaneBuilder.build();
        TERRACOTTA_MAP = terracottaBuilder.build();
        WOOL_MAP = woolBuilder.build();
        CARPET_MAP = carpetBuilder.build();
        CONCRETE_MAP = concreteBuilder.build();
        CONCRETE_POWDER_MAP = concretePowderBuilder.build();

        BED_MAP = bedBuilder.build();
        BANNER_MAP = bannerBuilder.build();
    }

    private static final TriPredicate<IPaintable, IPaintable, Direction> paintablePredicate = (parent, child, dir) -> {
        if (parent == null)
            return true;
        if (!parent.getClass().equals(child.getClass())) {
            return false;
        }
        return parent.getPaintingColor() == child.getPaintingColor();
    };

    @SuppressWarnings("rawtypes")
    private static final TriPredicate<IPipeNode, IPipeNode, Direction> gtPipePredicate = (parent, child, direction) -> {
        if (parent == null)
            return true;
        if (!paintablePredicate.test(parent, child, direction)) {
            return false;
        }
        return parent.isConnected(direction) && child.isConnected(direction.getOpposite());
    };

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null)
            return InteractionResult.PASS;

        if (getAvailableOperations(stack) <= 0) {
            player.displayClientMessage(Component.translatable("behaviour.paintspray.chameleon.message.out_of_paint"), true);
            return InteractionResult.FAIL;
        }

        DyeColor selectedColor = getColor(stack);
        int maxBlocksToRecolor = player.isShiftKeyDown() ? ConfigHolder.INSTANCE.tools.sprayCanChainLength : 1;
        maxBlocksToRecolor = Math.min(maxBlocksToRecolor, getAvailableOperations(stack));

        var first = level.getBlockEntity(context.getClickedPos());
        boolean changesMade = false;

        if (first != null && handleSpecialBlockEntities(first, selectedColor, maxBlocksToRecolor, context)) {
            changesMade = true;
        } else if (first == null || !(first instanceof SignBlockEntity || first instanceof IColorableBlockEntity || first instanceof IPipeNode || first instanceof IPaintable))

            if (changesMade) {
                GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(cap -> {
            int current = cap.getFluidInTank(0).getAmount();
            int max = cap.getTankCapacity(0);
            tooltip.add(Component.translatable("behaviour.paintspray.chameleon.tooltip.fluid", current, max));
        });

    }

    public static void setColor(ItemStack stack, @Nullable DyeColor color) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.remove("chromatic_code");

        if (color == null) {
            tag.putInt("color", -1);
        } else {
            tag.putInt("color", color.ordinal());
        }
    }

    @Nullable
    public static DyeColor getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("color") || tag.getInt("color") == -1) {
            return null;
        }
        int ordinal = tag.getInt("color");
        DyeColor[] colors = DyeColor.values();
        if (ordinal >= 0 && ordinal < colors.length) {
            return colors[ordinal];
        }
        return null;
    }

    private boolean handleBlocks(BlockPos start, DyeColor color, int limit, UseOnContext context) {
        final var level = context.getLevel();
        var collected = BreadthFirstBlockSearch
                .conditionalBlockPosSearch(start,
                        (parent, child) -> parent == null ||
                                level.getBlockState(child).is(level.getBlockState(parent).getBlock()),
                        limit, limit * 6);

        int successfullyPainted = 0;
        for (var pos : collected) {
            if (tryPaintBlock(level, pos, color)) {
                successfullyPainted++;
            }
        }

        if (successfullyPainted > 0) {

            boolean isBulk = limit > 1;
            consumePaint(context.getItemInHand(), successfullyPainted, isBulk);
            return true;
        }
        return false;
    }

    private boolean handleSignRecolor(SignBlockEntity sign, @Nullable DyeColor color, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null)
            return false;

        boolean isFront = sign.isFacingFrontText(player);
        var signText = sign.getText(isFront);
        if (sign.isWaxed())
            return false;

        ItemStack stack = context.getItemInHand();
        String chromCode = getChromaticCode(stack);
        boolean changed = false;

        if (chromCode != null) {
            for (int i = 0; i < 4; i++) {
                final int line = i;
                String currentText = signText.getMessage(line, false).getString();
                String cleaned = currentText.replaceAll("§.", "");
                String newText = "§" + chromCode + cleaned;

                if (!currentText.equals(newText)) {
                    sign.updateText(t -> t.setMessage(line, Component.literal(newText)), isFront);
                    changed = true;
                }
            }
        } else {
            if (color == null) {

                sign.updateText(text -> {
                    boolean runClear = false;
                    for (int i = 0; i < 4; i++) {
                        String current = text.getMessage(i, false).getString();
                        String cleaned = current.replaceAll("§.", "");
                        if (!current.equals(cleaned)) {
                            text = text.setMessage(i, Component.literal(cleaned));
                        }
                    }
                    return text.setColor(DyeColor.BLACK).setHasGlowingText(false);
                }, isFront);
                changed = true;
            } else {
                DyeColor targetColor = color;
                if (signText.getColor() != targetColor) {
                    sign.updateText(text -> text.setColor(targetColor), isFront);
                    changed = true;
                }
            }
        }

        if (changed) {
            consumePaint(stack, 1, false);
            return true;
        }

        return false;
    }

    private boolean handleSpecialBlockEntities(BlockEntity first, DyeColor color, int limit, UseOnContext context) {
        var player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null)
            return false;

        if (first instanceof SignBlockEntity sign) {
            return handleSignRecolor(sign, color, context);
        }

        boolean isBulk = limit > 1;

        if (GTCEu.Mods.isAE2Loaded() && first instanceof IColorableBlockEntity) {
            var collected = BreadthFirstBlockSearch.conditionalSearch(
                    IColorableBlockEntity.class,
                    (IColorableBlockEntity) first,
                    first.getLevel(),
                    be -> ((BlockEntity) be).getBlockPos(),
                    (parent, child, dir) -> parent == null || parent.getColor() == child.getColor(),
                    limit,
                    limit * 6);

            AEColor ae2Color = color == null ? AEColor.TRANSPARENT : AEColor.values()[color.ordinal()];

            int successfullyPainted = 0;
            for (IColorableBlockEntity colorable : collected) {
                if (colorable.getColor() != ae2Color) {
                    colorable.recolourBlock(null, ae2Color, player);
                    successfullyPainted++;
                }
            }
            if (successfullyPainted > 0) {
                consumePaint(stack, successfullyPainted, isBulk);
                return true;
            }
            return false;
        } else if (first instanceof IPipeNode pipe) {
            var collected = BreadthFirstBlockSearch.conditionalSearch(IPipeNode.class, pipe,
                    first.getLevel(), IPipeNode::getPipePos,
                    gtPipePredicate, limit, limit * 6);

            int successfullyPainted = 0;
            long targetNativeColor = color == null ? IPaintable.UNPAINTED_COLOR : color.getMapColor().col;
            for (var node : collected) {
                if (node.getPaintingColor() != targetNativeColor) {
                    paintPaintable(node, color);
                    successfullyPainted++;
                }
            }

            if (successfullyPainted > 0) {
                consumePaint(stack, successfullyPainted, isBulk);
                return true;
            }
            return false;
        } else if (first instanceof IPaintable paintable) {
            var collected = BreadthFirstBlockSearch.conditionalSearch(IPaintable.class, paintable,
                    first.getLevel(), p -> ((BlockEntity) p).getBlockPos(),
                    paintablePredicate, limit, limit * 6);

            int successfullyPainted = 0;
            long targetNativeColor = color == null ? IPaintable.UNPAINTED_COLOR : color.getMapColor().col;
            for (var node : collected) {
                if (node.getPaintingColor() != targetNativeColor) {
                    paintPaintable(node, color);
                    successfullyPainted++;
                }
            }

            if (successfullyPainted > 0) {
                consumePaint(stack, successfullyPainted, isBulk);
                return true;
            }
            return false;
        }

        return false;
    }

    private static void paintPaintable(IPaintable paintable, DyeColor color) {
        if (color == null) {
            paintable.setPaintingColor(IPaintable.UNPAINTED_COLOR);
        } else {
            paintable.setPaintingColor(color.getMapColor().col);
        }
    }

    public static void setChromaticCode(ItemStack stack, char code) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("chromatic_code", String.valueOf(code));
        tag.putInt("color", -2);
    }

    @Nullable
    public static String getChromaticCode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("chromatic_code") && tag.getInt("color") == -2) {
            return tag.getString("chromatic_code");
        }
        return null;
    }

    private boolean tryPaintBlock(Level level, BlockPos pos, DyeColor color) {
        var blockState = level.getBlockState(pos);
        var block = blockState.getBlock();

        String chromCode = getChromaticCode(level.getBlockEntity(pos) == null ? ItemStack.EMPTY : new ItemStack(block.asItem()));

        if (block instanceof LampBlock oldLamp) {
            DyeColor targetColor = (color == null) ? DyeColor.WHITE : color;
            if (oldLamp.color == targetColor)
                return false;

            Block targetBlock = oldLamp.bordered
                    ? GTBlocks.LAMPS.get(targetColor).get()
                    : GTBlocks.BORDERLESS_LAMPS.get(targetColor).get();

            if (targetBlock != null) {
                BlockState newLampState = targetBlock.defaultBlockState()
                        .setValue(LampBlock.INVERTED, blockState.getValue(LampBlock.INVERTED))
                        .setValue(LampBlock.BLOOM, blockState.getValue(LampBlock.BLOOM))
                        .setValue(LampBlock.LIGHT, blockState.getValue(LampBlock.LIGHT))
                        .setValue(LampBlock.POWERED, blockState.getValue(LampBlock.POWERED));

                level.setBlockAndUpdate(pos, newLampState);
                return true;
            }
            return false;
        }

        DyeColor targetColor = (color == null) ? DyeColor.WHITE : color;
        Block targetDecoBlock = null;
        boolean isDecorationBlock = false;

        for (DyeColor entryColor : DyeColor.values()) {
            var sheetEntry = GTBlocks.METAL_SHEETS.get(entryColor);
            var largeSheetEntry = GTBlocks.LARGE_METAL_SHEETS.get(entryColor);
            var studsEntry = GTBlocks.STUDS.get(entryColor);

            if (sheetEntry != null && sheetEntry.get() == block) {
                isDecorationBlock = true;
                if (entryColor != targetColor) {
                    targetDecoBlock = GTBlocks.METAL_SHEETS.get(targetColor).get();
                }
                break;
            }
            if (largeSheetEntry != null && largeSheetEntry.get() == block) {
                isDecorationBlock = true;
                if (entryColor != targetColor) {
                    targetDecoBlock = GTBlocks.LARGE_METAL_SHEETS.get(targetColor).get();
                }
                break;
            }
            if (studsEntry != null && studsEntry.get() == block) {
                isDecorationBlock = true;
                if (entryColor != targetColor) {
                    targetDecoBlock = GTBlocks.STUDS.get(targetColor).get();
                }
                break;
            }
        }

        if (isDecorationBlock) {
            if (targetDecoBlock != null) {
                BlockState newDecorationState = targetDecoBlock.defaultBlockState();

                for (Property property : blockState.getProperties()) {
                    if (newDecorationState.hasProperty(property)) {
                        newDecorationState = newDecorationState.setValue(property, blockState.getValue(property));
                    }
                }

                level.setBlockAndUpdate(pos, newDecorationState);
                return true;
            }
            return false;
        }

        int targetIntColor = (color == null)
                ? (int) IPaintable.UNPAINTED_COLOR
                : (int) color.getMapColor().col;

        if (block instanceof IPaintable paintableBlock) {
            if (paintableBlock.getPaintingColor() != targetIntColor) {
                if (!level.isClientSide()) {
                    paintableBlock.setPaintingColor(targetIntColor);
                }
                return true;
            }
            return false;
        }

        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IPaintable paintableBE) {
            if (paintableBE.getPaintingColor() != targetIntColor) {
                if (!level.isClientSide()) {
                    paintableBE.setPaintingColor(targetIntColor);
                }
                return true;
            }
            return false;
        }

        if (block.defaultBlockState().is(BlockTags.BEDS) || block.defaultBlockState().is(BlockTags.BANNERS)) {
            return tryPaintSpecialBlock(level, pos, block, color);
        }

        if (color == null) {
            return tryStripBlockColor(level, pos, block);
        }

        return recolorBlockState(level, pos, color) || tryPaintSpecialBlock(level, pos, block, color);
    }

    private boolean tryPaintSpecialBlock(Level world, BlockPos pos, Block block, DyeColor color) {

        DyeColor activeColor = (color == null) ? DyeColor.WHITE : color;

        if (block.defaultBlockState().is(Tags.Blocks.GLASS))
            return recolorBlockNoState(GLASS_MAP, activeColor, world, pos, Blocks.GLASS);
        if (block.defaultBlockState().is(Tags.Blocks.GLASS_PANES))
            return recolorBlockNoState(GLASS_PANE_MAP, activeColor, world, pos, Blocks.GLASS_PANE);
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA))
            return recolorBlockNoState(TERRACOTTA_MAP, activeColor, world, pos, Blocks.TERRACOTTA);
        if (block.defaultBlockState().is(BlockTags.WOOL))
            return recolorBlockNoState(WOOL_MAP, activeColor, world, pos, null);
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS))
            return recolorBlockNoState(CARPET_MAP, activeColor, world, pos, null);
        if (block.defaultBlockState().is(CustomTags.CONCRETE_BLOCK))
            return recolorBlockNoState(CONCRETE_MAP, activeColor, world, pos, null);
        if (block.defaultBlockState().is(CustomTags.CONCRETE_POWDER_BLOCK))
            return recolorBlockNoState(CONCRETE_POWDER_MAP, activeColor, world, pos, null);

        if (block.defaultBlockState().is(BlockTags.BEDS)) {
            BlockState currentBedState = world.getBlockState(pos);
            if (currentBedState.getBlock() instanceof BedBlock) {
                Direction facing = currentBedState.getValue(BedBlock.FACING);
                BedPart part = currentBedState.getValue(BedBlock.PART);

                BlockPos otherHalfPos = (part == BedPart.FOOT)
                        ? pos.relative(facing)
                        : pos.relative(facing.getOpposite());
                BlockState otherHalfState = world.getBlockState(otherHalfPos);

                Block targetBedBlock = BED_MAP.get(activeColor);
                if (targetBedBlock == null || targetBedBlock == currentBedState.getBlock())
                    return false;

                CompoundTag mainTag = null;
                BlockEntity mainBE = world.getBlockEntity(pos);
                if (mainBE != null)
                    mainTag = mainBE.saveWithFullMetadata();

                CompoundTag otherTag = null;
                BlockEntity otherBE = world.getBlockEntity(otherHalfPos);
                if (otherBE != null)
                    otherTag = otherBE.saveWithFullMetadata();

                BlockState newMainState = targetBedBlock.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART,
                        part);
                BlockState newOtherState = targetBedBlock.defaultBlockState().setValue(BedBlock.FACING, facing).setValue(BedBlock.PART,
                        part == BedPart.FOOT ? BedPart.HEAD
                                : BedPart.FOOT);

                world.setBlock(pos, newMainState, 18);
                if (otherHalfState.is(BlockTags.BEDS)) {
                    world.setBlock(otherHalfPos, newOtherState, 18);
                }

                if (mainTag != null) {
                    BlockEntity newMainBE = world.getBlockEntity(pos);
                    if (newMainBE != null)
                        newMainBE.load(mainTag);
                }
                if (otherTag != null) {
                    BlockEntity newOtherBE = world.getBlockEntity(otherHalfPos);
                    if (newOtherBE != null)
                        newOtherBE.load(otherTag);
                }

                world.sendBlockUpdated(pos, currentBedState, newMainState, 3);
                world.sendBlockUpdated(otherHalfPos, otherHalfState, newOtherState, 3);
                return true;
            }
        }

        if (block.defaultBlockState().is(BlockTags.BANNERS)) {
            BlockState bannerState = world.getBlockState(pos);
            boolean isWallBanner = bannerState.getBlock() instanceof WallBannerBlock;

            Block targetBannerBlock;
            if (isWallBanner) {
                ResourceLocation targetId = new ResourceLocation("minecraft", activeColor.getSerializedName() + "_wall_banner");
                targetBannerBlock = BuiltInRegistries.BLOCK.get(targetId);
            } else {
                targetBannerBlock = BANNER_MAP.get(activeColor);
            }

            if (targetBannerBlock == null || targetBannerBlock == bannerState.getBlock())
                return false;

            CompoundTag bannerTag = null;
            BlockEntity bannerBE = world.getBlockEntity(pos);
            if (bannerBE != null)
                bannerTag = bannerBE.saveWithFullMetadata();

            BlockState newBannerState = targetBannerBlock.defaultBlockState();
            for (Property property : bannerState.getProperties()) {
                if (newBannerState.hasProperty(property)) {
                    newBannerState = newBannerState.setValue(property, bannerState.getValue(property));
                }
            }

            world.setBlock(pos, newBannerState, 3);

            if (bannerTag != null) {
                BlockEntity newBannerBE = world.getBlockEntity(pos);
                if (newBannerBE != null)
                    newBannerBE.load(bannerTag);
            }
            return true;
        }

        return false;
    }

    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, @Nullable DyeColor color, Level level, BlockPos pos, Block defaultBlock) {
        Block newBlock = map.getOrDefault(color, defaultBlock);
        if (newBlock == Blocks.AIR)
            newBlock = defaultBlock;

        BlockState old = level.getBlockState(pos);
        if (newBlock != null && newBlock != old.getBlock()) {
            BlockState state = newBlock.defaultBlockState();
            for (Property property : old.getProperties()) {
                if (!state.hasProperty(property))
                    continue;
                state.setValue(property, old.getValue(property));
            }
            level.setBlockAndUpdate(pos, state);
            return true;
        }
        return false;
    }

    private static boolean tryStripBlockColor(Level world, BlockPos pos, Block block) {
        if (block instanceof StainedGlassBlock) {
            world.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
            return true;
        }
        if (block instanceof StainedGlassPaneBlock) {
            world.setBlockAndUpdate(pos, Blocks.GLASS_PANE.defaultBlockState());
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA) && block != Blocks.TERRACOTTA) {
            world.setBlockAndUpdate(pos, Blocks.TERRACOTTA.defaultBlockState());
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.WOOL) && block != Blocks.WHITE_WOOL) {
            world.setBlockAndUpdate(pos, Blocks.WHITE_WOOL.defaultBlockState());
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS) && block != Blocks.WHITE_CARPET) {
            world.setBlockAndUpdate(pos, Blocks.WHITE_CARPET.defaultBlockState());
            return true;
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_BLOCK) && block != Blocks.WHITE_CONCRETE) {
            world.setBlockAndUpdate(pos, Blocks.WHITE_CONCRETE.defaultBlockState());
            return true;
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_POWDER_BLOCK) && block != Blocks.WHITE_CONCRETE_POWDER) {
            world.setBlockAndUpdate(pos, Blocks.WHITE_CONCRETE_POWDER.defaultBlockState());
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.BEDS) && block != Blocks.WHITE_BED) {
            world.setBlockAndUpdate(pos, Blocks.WHITE_BED.defaultBlockState());
            return true;
        }

        if (block.defaultBlockState().is(BlockTags.BANNERS) && block != Blocks.WHITE_BANNER) {
            world.setBlockAndUpdate(pos, Blocks.WHITE_BANNER.defaultBlockState());
            return true;
        }

        BlockState state = world.getBlockState(pos);
        for (Property prop : state.getProperties()) {
            if (prop.getValueClass() == DyeColor.class) {
                BlockState defaultState = block.defaultBlockState();
                DyeColor defaultColor = DyeColor.WHITE;
                try {
                    defaultColor = (DyeColor) defaultState.getValue(prop);
                } catch (IllegalArgumentException ignored) {
                }
                return recolorBlockState(world, pos, defaultColor);
            }
        }
        return false;
    }

    private static boolean recolorBlockState(Level level, BlockPos pos, DyeColor color) {
        BlockState state = level.getBlockState(pos);
        for (Property property : state.getProperties()) {
            if (property.getValueClass() == DyeColor.class) {
                if (state.getValue(property) == color)
                    return false;
                level.setBlockAndUpdate(pos, state.setValue(property, color));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onEntitySwing(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return false;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        return false;
    }

    private static boolean consumePaint(ItemStack stack, int operationsCount, boolean isBulk) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(cap -> {
                    int costPerOp = TFGConfig.SERVER.CHAMELEON_SPRAY_CAN_COST_PER_OPERATION.get();
                    int totalRequired = costPerOp * operationsCount;

                    if (isBulk && operationsCount > 1) {
                        double multiplier = TFGConfig.SERVER.CHAMELEON_SPRAY_CAN_BULK_MULTIPLIER.get();
                        totalRequired = Math.max(1, (int) (totalRequired * multiplier));
                    }

                    var simulated = cap.drain(totalRequired, IFluidHandler.FluidAction.SIMULATE);
                    if (simulated.getAmount() >= totalRequired) {
                        cap.drain(totalRequired, IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }

    private static int getAvailableOperations(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(cap -> {
                    int currentFluid = cap.getFluidInTank(0).getAmount();
                    int costPerOp = TFGConfig.SERVER.CHAMELEON_SPRAY_CAN_COST_PER_OPERATION.get();

                    return costPerOp == 0 ? Integer.MAX_VALUE : currentFluid / costPerOp;
                }).orElse(0);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (getChromaticCode(stack) != null)
            return InteractionResult.PASS;
        if (getAvailableOperations(stack) <= 0) {
            player.displayClientMessage(Component.translatable("behaviour.paintspray.chameleon.message.out_of_paint"), true);
            return InteractionResult.FAIL;
        }

        Level level = target.level();
        DyeColor color = getColor(stack);
        DyeColor targetColor = (color == null) ? DyeColor.WHITE : color;
        DyeColor collarTargetColor = (color == null) ? DyeColor.RED : color;
        boolean changed = false;

        if (target instanceof Sheep sheep) {
            if (sheep.isAlive() && !sheep.isBaby() && sheep.getColor() != targetColor) {
                if (!level.isClientSide)
                    sheep.setColor(targetColor);
                changed = true;
            }
        } else if (target instanceof TamableMammal pet && pet.getOwnerUUID() != null) {
            if (pet.getCollarColor() != collarTargetColor) {
                if (!level.isClientSide)
                    pet.setCollarColor(collarTargetColor);
                changed = true;
            }
        }

        if (changed) {
            consumePaint(stack, 1, false);
            GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}