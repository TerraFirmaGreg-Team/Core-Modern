package exception.terrafirmagreg.objects.entity;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.compat.tfc.TFCCompat;
import exception.terrafirmagreg.objects.block.TapBlock;
import exception.terrafirmagreg.objects.recipe.TapExtractRecipe;
import exception.terrafirmagreg.util.ColorUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;

public class TapBlockEntity extends BlockEntity {
    public TapExtractRecipe currentRecipe;
    public boolean hasSearchedForRecipe = false;
    private int counter = 0;

    public TapBlockEntity(BlockPos pos, BlockState state) {
        super(TerraFirmaGreg.TAP_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TapBlockEntity blockEntity) {
        // Check or lookup recipe
        if (level.getBlockEntity(pos.below()) instanceof SapCollectorBlockEntity sapCollector) {
            if (blockEntity.currentRecipe == null && !blockEntity.hasSearchedForRecipe) {
                var log = level.getBlockState(pos.relative(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite()));
                blockEntity.currentRecipe = TerraFirmaGreg.getRecipe(level, log);
                sapCollector.setCurrentRecipe(blockEntity.currentRecipe);
                if (blockEntity.currentRecipe != null) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.ATTACHED, true));
                    if (!blockEntity.currentRecipe.fluidColor.isEmpty()) {
                        TapBlock.color.put(pos, ColorUtil.getCacheColor(ColorUtil.getCacheColor(blockEntity.currentRecipe.fluidColor)));
                    } else {
                        IClientFluidTypeExtensions renderProperties = IClientFluidTypeExtensions.of(blockEntity.currentRecipe.displayFluid.getFluid());
                        int fluidTintColour = renderProperties.getTintColor(blockEntity.currentRecipe.displayFluid);
                        TapBlock.color.put(pos, ColorUtil.getCacheColor(fluidTintColour));
                    }
                } else {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.ATTACHED, false));
                }
                blockEntity.hasSearchedForRecipe = true;
            } else if (blockEntity.currentRecipe != null) {
                if (TFCCompat.canProcess(blockEntity.currentRecipe)) {
                    float mod = 1f;
                    if (state.getBlock() instanceof TapBlock tapBlock) {
                        mod = tapBlock.getModifier();
                    }

                    int tickRate = TerraFirmaGreg.recipeTickrate(blockEntity.currentRecipe);
                    if (++blockEntity.counter % tickRate == 0) {
                        if (sapCollector.progress > blockEntity.currentRecipe.processingTime) {
                            sapCollector.progress = blockEntity.currentRecipe.processingTime;
                        } else {
                            sapCollector.addProgress((int) (tickRate * mod));
                            level.sendBlockUpdated(pos.below(), sapCollector.getBlockState(), sapCollector.getBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                    if (!state.getValue(BlockStateProperties.ATTACHED)) {
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.ATTACHED, true));
                    }
                } else if (state.getValue(BlockStateProperties.ATTACHED)) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.ATTACHED, false));
                }
            }
        }
    }

    public void reset() {
        currentRecipe = null;
        hasSearchedForRecipe = false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.loadPacketNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.savePacketNBT(tag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithId();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.loadPacketNBT(pkt.getTag());
        if (level instanceof ClientLevel) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    public void loadPacketNBT(CompoundTag tag) {
        if (tag.contains("recipe") && level != null) {
            var recipe = level.getRecipeManager().byKey(new ResourceLocation(tag.getString("recipe")));
            recipe.ifPresent(value -> this.currentRecipe = (TapExtractRecipe) value);
        }
    }

    public void savePacketNBT(CompoundTag tag) {
        if (this.currentRecipe != null) {
            tag.putString("recipe", this.currentRecipe.getId().toString());
        }
    }
}
