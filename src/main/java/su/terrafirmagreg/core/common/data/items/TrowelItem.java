package su.terrafirmagreg.core.common.data.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SandBlockType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class TrowelItem extends Item {
    public TrowelItem(Properties properties) {
        super(properties.durability(1026));
    }

    //Maps for RNR block replacing.
    public static Map<ResourceLocation, ResourceLocation> createBlockMapping() {
        Map<ResourceLocation, ResourceLocation> map = new HashMap<>();

        //Gets enums from TFC and turns into a sring for the maps.
        List<String> sandstone_colors = Arrays.stream(SandBlockType.values())
                .map(SandBlockType -> SandBlockType.name().toLowerCase(Locale.ROOT))
                .toList();

        List<String> rocks = Arrays.stream(Rock.values())
                .map(rock -> rock.name().toLowerCase(Locale.ROOT))
                .toList();

        //Sandstone
        for (String sandstone_color : sandstone_colors) {
            map.put(
                    new ResourceLocation("rnr", "flagstone/" + sandstone_color + "_sandstone"),
                    new ResourceLocation("rnr", sandstone_color + "_sandstone_flagstones")
            );
        }
        //Flagstones
        for (String flagstone_rock : rocks) {
            map.put(
                    new ResourceLocation("rnr", "flagstone/" + flagstone_rock),
                    new ResourceLocation("rnr", "rock/flagstones/" + flagstone_rock)
            );
        }
        //Gravel
        for (String gravel_rock : rocks) {
            map.put(
                    new ResourceLocation("rnr", "gravel_fill/" + gravel_rock),
                    new ResourceLocation("rnr", "rock/gravel_road/" + gravel_rock)
            );
        }
        //Cobble
        for (String cobble_rock : rocks) {
            map.put(
                    new ResourceLocation("tfc", "rock/loose/" + cobble_rock),
                    new ResourceLocation("rnr", "rock/cobbled_road/" + cobble_rock)
            );
        }
        for (String mossy_cobble_rock : rocks) {
            map.put(
                    new ResourceLocation("tfc", "rock/mossy_loose/" + mossy_cobble_rock),
                    new ResourceLocation("rnr", "rock/cobbled_road/" + mossy_cobble_rock)
            );
        }
        //Sett Bricks
        for (String brick_rock : rocks) {
            map.put(
                    new ResourceLocation("tfc", "brick/" + brick_rock),
                    new ResourceLocation("rnr", "rock/sett_road/" + brick_rock)
            );
        }
        //Hoggin
        map.put(
                new ResourceLocation("rnr", "hoggin_mix"),
                new ResourceLocation("rnr", "hoggin")
        );
        //Brick
        map.put(
                new ResourceLocation("minecraft", "brick"),
                new ResourceLocation("rnr", "brick_road")
        );

        return map;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();

        BlockPos targetPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(targetPos);
        ResourceLocation clickedBlockId = clickedState.getBlock().builtInRegistryHolder().key().location();

        Map<ResourceLocation, ResourceLocation> blockMapping = createBlockMapping();

        //Exception for rnr roads.
        if (clickedBlockId.toString().equals("rnr:base_course")) {
            List<ItemStack> validStacks = new ArrayList<>();
            //Randomly chooses a spot in the hotbar.
            for (int i = 0; i < 9; i++) {
                ItemStack hotbarStack = player.getInventory().getItem(i);
                if (!hotbarStack.isEmpty()) {
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(hotbarStack.getItem());
                    if (itemId != null && blockMapping.containsKey(itemId)) {
                        validStacks.add(hotbarStack);
                    }
                }
            }

            if (validStacks.isEmpty()) return InteractionResult.PASS;

            ItemStack randomStack = validStacks.get(new Random().nextInt(validStacks.size()));
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(randomStack.getItem());
            ResourceLocation resultBlockId = blockMapping.get(itemId);
            Block resultBlock = ForgeRegistries.BLOCKS.getValue(resultBlockId);

            if (resultBlock != null) {
                //Updates state.
                BlockState newState = resultBlock.defaultBlockState();
                level.setBlock(targetPos, newState, 3);
                level.updateNeighborsAt(targetPos, resultBlock);

                //Plays sound when placing.
                level.playSound(null, targetPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 0.4f);

                //Decreases item count unless in creative.
                if (!player.isCreative()) {
                    randomStack.shrink(1);
                }

                //Damages the tool, or breaks it if at 0.
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));

                return InteractionResult.SUCCESS;
            }

            return InteractionResult.FAIL;
        }

        //Normal block behavior.
        List<ItemStack> blockItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack hotbarStack = player.getInventory().getItem(i);
            if (hotbarStack.getItem() instanceof BlockItem) {
                blockItems.add(hotbarStack);
            }
        }

        if (blockItems.isEmpty()) return InteractionResult.PASS;

        ItemStack randomStack = blockItems.get(new Random().nextInt(blockItems.size()));
        BlockItem blockItem = (BlockItem) randomStack.getItem();
        //Gets context like waterlogged, rotated, etc.
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        //Places block--respecting placement logic.
        InteractionResult result = blockItem.place(placeContext);

        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        level.playSound(null, placePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 0.4f);

        if (result.consumesAction()) {
            if (!player.isCreative()) {
                randomStack.shrink(1);
            }
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
        }

        return result;
    }
}