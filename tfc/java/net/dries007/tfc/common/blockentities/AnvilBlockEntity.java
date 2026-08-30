/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blockentities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.devices.Tiered;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.capabilities.forge.ForgingBonus;
import net.dries007.tfc.common.capabilities.forge.ForgingCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.container.AnvilContainer;
import net.dries007.tfc.common.container.AnvilPlanContainer;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.advancements.TFCAdvancements;

public class AnvilBlockEntity extends InventoryBlockEntity<AnvilBlockEntity.AnvilInventory> implements ISlotCallback
{
    public static final int SLOT_INPUT_MAIN = 0;
    public static final int SLOT_INPUT_SECOND = 1;
    public static final int SLOT_HAMMER = 2;
    public static final int SLOT_CATALYST = 3;

    public static final int[] SLOTS_BY_HAND_EXTRACT = new int[] {SLOT_INPUT_MAIN, SLOT_INPUT_SECOND};
    public static final int[] SLOTS_BY_HAND_INSERT = new int[] {SLOT_CATALYST, SLOT_INPUT_MAIN, SLOT_INPUT_SECOND};

    private static final Component NAME = Component.translatable("tfc.block_entity.anvil");

    public AnvilBlockEntity(BlockPos pos, BlockState state)
    {
        this(TFCBlockEntities.ANVIL.get(), pos, state, AnvilInventory::new, NAME);
    }

    public AnvilBlockEntity(BlockEntityType<? extends AnvilBlockEntity> type, BlockPos pos, BlockState state, InventoryFactory<AnvilInventory> inventoryFactory, Component defaultName)
    {
        super(type, pos, state, inventoryFactory, defaultName);
    }

    @Nullable
    public Forging getMainInputForging()
    {
        return ForgingCapability.get(inventory.getStackInSlot(AnvilBlockEntity.SLOT_INPUT_MAIN));
    }

    /**
     * @return the provider for opening the anvil plan screen
     */
    public MenuProvider planProvider()
    {
        return new SimpleMenuProvider(this::createPlanContainer, getDisplayName());
    }

    /**
     * @return the provider for opening the main anvil working screen
     */
    public MenuProvider anvilProvider()
    {
        return this;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        return AnvilContainer.create(this, player.getInventory(), containerId);
    }

    @Nullable
    public AbstractContainerMenu createPlanContainer(int containerId, Inventory inventory, Player player)
    {
        return AnvilPlanContainer.create(this, player.getInventory(), containerId);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return switch (slot)
            {
                case SLOT_INPUT_MAIN, SLOT_INPUT_SECOND -> true;
                case SLOT_HAMMER -> Helpers.isItem(stack, TFCTags.Items.HAMMERS);
                case SLOT_CATALYST -> Helpers.isItem(stack, TFCTags.Items.FLUX);
                default -> false;
            };
    }

    @Override
    public int getSlotStackLimit(int slot)
    {
        return slot == SLOT_CATALYST ? 64 : 1;
    }

    @Override
    public void onSlotTake(Player player, int slot, ItemStack stack)
    {
        if (slot == SLOT_INPUT_MAIN)
        {
            ForgingCapability.clearRecipeIfNotWorked(stack);
        }

        // If items are taken from the main or secondary slots, move excess output items into the freed up item slot
        if (slot == SLOT_INPUT_MAIN || slot == SLOT_INPUT_SECOND)
        {
            final List<ItemStack> excess = inventory.excess;
            if (!excess.isEmpty() && inventory.getStackInSlot(slot).isEmpty())
            {
                inventory.setStackInSlot(slot, excess.remove(0));
            }
        }
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        assert level != null;

        final ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        if (!stack.isEmpty())
        {
            final Forging forge = ForgingCapability.get(stack);
            if (forge != null)
            {
                AnvilRecipe recipe = forge.getRecipe(level);
                if (recipe == null)
                {
                    // Select a default recipe if we only find a single recipe for this item
                    final Collection<AnvilRecipe> all = AnvilRecipe.getAll(level, stack, getTier());
                    if (all.size() == 1)
                    {
                        // Update the recipe held by the forging item
                        recipe = all.iterator().next();
                        if (!level.isClientSide)
                        {
                            // If multiple items are in the main slot, all but 1 are moved to the secondary slot, or put into the excess output list if the secondary item slot is full
                            if (stack.getCount() != 1)
                            {
                                final ItemStack overflow = stack.split(stack.getCount() - 1);
                                if (inventory.getStackInSlot(SLOT_INPUT_SECOND).isEmpty())
                                {
                                    inventory.setStackInSlot((SLOT_INPUT_SECOND), overflow);
                                }
                                else
                                {
                                    inventory.excess.add(overflow);
                                }
                            }
                            forge.setRecipe(recipe, inventory);
                        }
                    }
                }
            }
        }
        markForSync();
    }

    @Override
    public void ejectInventory()
    {
        final ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        if (!stack.isEmpty())
        {
            ForgingCapability.clearRecipeIfNotWorked(stack);
        }
        super.ejectInventory();

        // Account for all the excess output items that have not yet been moved into actual item slots
        assert level != null;
        inventory.excess.stream().filter(item -> !item.isEmpty()).forEach(item -> Helpers.spawnItem(level, worldPosition, item));
    }

    public void chooseRecipe(@Nullable AnvilRecipe recipe)
    {
        assert level != null;

        final ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        if (!stack.isEmpty())
        {
            final Forging forge = ForgingCapability.get(stack);
            if (forge != null)
            {
                // If multiple items are in the main slot, all but 1 are moved to the secondary slot, or put into the excess output list if the secondary item slot is full
                if (stack.getCount() != 1)
                {
                    final ItemStack overflow = stack.split(stack.getCount() - 1);
                    if (inventory.getStackInSlot(SLOT_INPUT_SECOND).isEmpty())
                    {
                        inventory.setStackInSlot((SLOT_INPUT_SECOND), overflow);
                    }
                    else
                    {
                        inventory.excess.add(overflow);
                    }
                }

                forge.setRecipe(recipe, inventory);
            }
        }
    }

    /**
     * Sends feedback to the chat, as the action bar is obscured by the anvil gui
     */
    public InteractionResult work(ServerPlayer player, ForgeStep step)
    {
        assert level != null;

        final ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        final Forging forge = ForgingCapability.get(stack);
        if (forge != null)
        {
            // Check that we have a hammer, either in the anvil or in the player inventory
            ItemStack hammer = inventory.getStackInSlot(SLOT_HAMMER);
            @Nullable InteractionHand hammerSlot = null;
            if (hammer.isEmpty())
            {
                hammer = player.getMainHandItem();
                hammerSlot = InteractionHand.MAIN_HAND;
            }
            if (hammer.isEmpty())
            {
                hammer = player.getOffhandItem();
                hammerSlot = InteractionHand.OFF_HAND;
            }
            if (hammer.isEmpty() || !Helpers.isItem(hammer, TFCTags.Items.HAMMERS))
            {
                player.displayClientMessage(Component.translatable("tfc.tooltip.hammer_required_to_work"), false);
                return InteractionResult.FAIL;
            }

            // Prevent the player from immediately destroying the item by overworking
            if (!forge.getSteps().any() && forge.getWork() == 0 && step.step() < 0)
            {
                return InteractionResult.FAIL;
            }

            final AnvilRecipe recipe = forge.getRecipe(level);
            if (recipe != null)
            {
                if (!recipe.matches(inventory, level))
                {
                    player.displayClientMessage(Component.translatable("tfc.tooltip.anvil_is_too_low_tier_to_work"), false);
                    return InteractionResult.FAIL;
                }

                final @Nullable IHeat heat = HeatCapability.get(stack);
                if (heat != null && !heat.canWork())
                {
                    player.displayClientMessage(Component.translatable("tfc.tooltip.not_hot_enough_to_work"), false);
                    return InteractionResult.FAIL;
                }

                // Proceed with working
                forge.addStep(step);

                // Damage the hammer
                final InteractionHand breakingHand = hammerSlot;
                hammer.hurtAndBreak(1, player, e -> {
                    if (breakingHand != null)
                    {
                        e.broadcastBreakEvent(breakingHand);
                    }
                });

                if (forge.getWork() < 0 || forge.getWork() > ForgeStep.LIMIT)
                {
                    // Destroy the input
                    inventory.setStackInSlot(SLOT_INPUT_MAIN, ItemStack.EMPTY);
                    level.playSound(null, worldPosition, SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, 0.4f, 1.0f);
                    return InteractionResult.FAIL;
                }
                createForgingEffects();

                // Re-check anvil recipe completion
                if (recipe.checkComplete(inventory))
                {
                    // Recipe completed, so consume inputs and add outputs
                    final ItemStack outputStack = recipe.assemble(inventory, level.registryAccess());
                    final @Nullable IHeat outputHeat = HeatCapability.get(outputStack);

                    // Always preserve heat of the input
                    if (outputHeat != null)
                    {
                        outputHeat.setTemperatureIfWarmer(heat);
                    }

                    // And apply the forging bonus, if the recipe says to do so
                    if (recipe.shouldApplyForgingBonus())
                    {
                        final float ratio = (float) forge.getSteps().total() / ForgeRule.calculateOptimalStepsToTarget(recipe.computeTarget(inventory), recipe.getRules());
                        final ForgingBonus bonus = ForgingBonus.byRatio(ratio);
                        ForgingBonus.set(outputStack, bonus);

                        if (bonus == ForgingBonus.PERFECTLY_FORGED)
                        {
                            TFCAdvancements.PERFECTLY_FORGED.trigger(player);
                        }
                    }
                    inventory.setStackInSlot(SLOT_INPUT_MAIN, outputStack);
                }

                markForSync();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public boolean workRemotely(ForgeStep step, int movement, boolean forceCompletion)
    {
        assert level != null;

        if (level.isClientSide)
        {
            return false;
        }

        final ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        final Forging forge = ForgingCapability.get(stack);
        if (forge != null)
        {
            // Prevent the player from immediately destroying the item by overworking
            if (!forge.getSteps().any() && forge.getWork() == 0 && movement < 0)
            {
                return false;
            }

            final AnvilRecipe recipe = forge.getRecipe(level);
            if (recipe != null)
            {
                if (!recipe.matches(inventory, level))
                {
                    return false;
                }

                final @Nullable IHeat heat = HeatCapability.get(stack);
                if (heat != null && !heat.canWork())
                {
                    return false;
                }

                // Proceed with working
                if (forceCompletion)
                {
                    final int target = recipe.computeTarget(inventory);
                    final int cursor = forge.getWork();
                    if ((movement > 0 && cursor > target) || (movement < 0 && cursor < target))
                    {
                        movement = -movement;
                    }
                    if ((movement > 0 && cursor + movement > target) || (movement < 0 && cursor + movement < target))
                    {
                        movement = target - cursor;
                    }
                }
                forge.addStep(step, movement);

                if (forge.getWork() < 0 || forge.getWork() > ForgeStep.LIMIT)
                {
                    // Destroy the input
                    inventory.setStackInSlot(SLOT_INPUT_MAIN, ItemStack.EMPTY);
                    level.playSound(null, worldPosition, SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, 0.4f, 1.0f);
                    return true;
                }

                createForgingEffects();

                // Re-check anvil recipe completion
                if (recipe.checkComplete(inventory))
                {
                    // Recipe completed, so consume inputs and add outputs
                    final ItemStack outputStack = recipe.assemble(inventory, level.registryAccess());
                    final @Nullable IHeat outputHeat = HeatCapability.get(outputStack);

                    // Always preserve heat of the input
                    if (outputHeat != null)
                    {
                        outputHeat.setTemperatureIfWarmer(heat);
                    }

                    inventory.setStackInSlot(SLOT_INPUT_MAIN, outputStack);
                }

                markForSync();
            }
            return true;
        }
        return false;
    }

    private void createForgingEffects()
    {
        assert level != null;
        level.playSound(null, worldPosition, TFCSounds.ANVIL_HIT.get(), SoundSource.PLAYERS, 0.4f, 1.0f);
        if (level instanceof ServerLevel server)
        {
            final double x = worldPosition.getX() + Mth.nextDouble(level.random, 0.2, 0.8);
            final double z = worldPosition.getZ() + Mth.nextDouble(level.random, 0.2, 0.8);
            final double y = worldPosition.getY() + Mth.nextDouble(level.random, 0.8, 1.0);
            server.sendParticles(TFCParticles.SPARK.get(), x, y, z, 5, 0, 0, 0, 0.2f);
        }
    }

    /**
     * Sends feedback to the action bar, as the anvil gui will be closed
     */
    public InteractionResult weld(Player player)
    {
        final ItemStack left = inventory.getLeft(), right = inventory.getRight();
        if (left.isEmpty() && right.isEmpty())
        {
            return InteractionResult.PASS;
        }

        assert level != null;

        final WeldingRecipe recipe = level.getRecipeManager().getRecipeFor(TFCRecipeTypes.WELDING.get(), inventory, level).orElse(null);
        if (recipe != null)
        {
            if (!recipe.isCorrectTier(getTier()))
            {
                player.displayClientMessage(Component.translatable("tfc.tooltip.anvil_is_too_low_tier_to_weld"), true);
                return InteractionResult.FAIL;
            }

            final @Nullable IHeat leftHeat = HeatCapability.get(left);
            final @Nullable IHeat rightHeat = HeatCapability.get(right);

            if ((leftHeat != null && !leftHeat.canWeld()) || (rightHeat != null && !rightHeat.canWeld()))
            {
                player.displayClientMessage(Component.translatable("tfc.tooltip.not_hot_enough_to_weld"), true);
                return InteractionResult.FAIL;
            }

            if (inventory.getStackInSlot(SLOT_CATALYST).isEmpty())
            {
                player.displayClientMessage(Component.translatable("tfc.tooltip.no_flux_to_weld"), true);
                return InteractionResult.FAIL;
            }

            final ItemStack result = recipe.assemble(inventory, level.registryAccess());
            final @Nullable IHeat resultHeat = HeatCapability.get(result);

            inventory.getStackInSlot(SLOT_INPUT_MAIN).shrink(1);
            inventory.getStackInSlot(SLOT_INPUT_SECOND).shrink(1);
            inventory.getStackInSlot(SLOT_CATALYST).shrink(1);

            // If the main item slot is free, we move the result into it, otherwise we simply add it to the excess item list
            // We don't just put the result into the main item slot due to recipes which can output multiple items, as these could lead to unintended item deletion
            if (inventory.getStackInSlot(SLOT_INPUT_MAIN).isEmpty())
            {
                inventory.setStackInSlot(SLOT_INPUT_MAIN, result);
            }
            else
            {
                inventory.excess.add(result);
            }

            // If there are excess output items, we move them into the newly freed up item slot
            if (!inventory.excess.isEmpty())
            {
                inventory.setStackInSlot(SLOT_INPUT_SECOND, inventory.excess.remove(0));
            }

            // Always copy heat from inputs since we have two
            if (resultHeat != null)
            {
                resultHeat.setTemperatureIfWarmer(leftHeat);
                resultHeat.setTemperatureIfWarmer(rightHeat);
            }

            markForSync();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public int getTier()
    {
        return getBlockState().getBlock() instanceof Tiered tiered ? tiered.getTier() : 0;
    }

    /**
     * Sets the inventory for a block entity that is not placed in the world
     */
    public void setInventoryFromOutsideWorld(ItemStack main, ItemStack hammer, ItemStack flux)
    {
        final NonNullList<ItemStack> internalStacks = inventory.getInternalStacks();
        internalStacks.set(SLOT_INPUT_MAIN, main);
        internalStacks.set(SLOT_HAMMER, hammer);
        internalStacks.set(SLOT_CATALYST, flux);
    }

    public static class AnvilInventory extends InventoryItemHandler implements AnvilRecipe.Inventory, WeldingRecipe.Inventory
    {
        private final AnvilBlockEntity anvil;
        private final List<ItemStack> excess;

        public AnvilInventory(InventoryBlockEntity<AnvilInventory> anvil)
        {
            super(anvil, 4);
            this.anvil = (AnvilBlockEntity) anvil;
            this.excess = new ArrayList<>();
        }

        @Override
        public ItemStack getItem()
        {
            return getStackInSlot(SLOT_INPUT_MAIN);
        }

        @Override
        public ItemStack getLeft()
        {
            return getStackInSlot(SLOT_INPUT_MAIN);
        }

        @Override
        public ItemStack getRight()
        {
            return getStackInSlot(SLOT_INPUT_SECOND);
        }

        @Override
        public int getTier()
        {
            return anvil.getTier();
        }

        @Override
        public long getSeed()
        {
            Helpers.warnWhenCalledFromClientThread();
            return anvil.getLevel() instanceof ServerLevel level ? level.getSeed() : 0;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            final ItemStack stack = super.extractItem(slot, amount, simulate);
            ForgingCapability.clearRecipeIfNotWorked(stack);

            // If there are excess output items, we move them into the newly freed up item slot
            if (!excess.isEmpty() && getStackInSlot(slot).isEmpty())
            {
                anvil.inventory.setStackInSlot(slot, excess.remove(0));
            }

            return stack;
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = super.serializeNBT();

            // Adding all excess output items to the NBT
            if (!excess.isEmpty())
            {
                final ListTag excessNbt = new ListTag();
                for (ItemStack stack : excess)
                {
                    excessNbt.add(stack.save(new CompoundTag()));
                }
                nbt.put("excess", excessNbt);
            }

            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            super.deserializeNBT(nbt);

            // Reading all excess output items from the NBT
            excess.clear();
            if (nbt.contains("excess"))
            {
                final ListTag excessNbt = nbt.getList("excess", Tag.TAG_COMPOUND);
                for (int i = 0; i < excessNbt.size(); i++)
                {
                    excess.add(ItemStack.of(excessNbt.getCompound(i)));
                }
            }
        }

    }
}
