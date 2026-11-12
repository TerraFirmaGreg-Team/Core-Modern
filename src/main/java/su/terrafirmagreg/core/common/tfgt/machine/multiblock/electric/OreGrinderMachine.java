package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

/**
 * Ported from GTm but keeping the itemLimit
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreGrinderMachine extends WorkableElectricMultiblockMachine {

    private static final Reference2IntMap<RecipeCapability<?>> LIMIT_1 = itemLimit(1);
    private static final Reference2IntMap<RecipeCapability<?>> LIMIT_2 = itemLimit(2);
    private static final Reference2IntMap<RecipeCapability<?>> LIMIT_3 = itemLimit(3);
    private static final Reference2IntMap<RecipeCapability<?>> LIMIT_4 = itemLimit(4);

    private static Reference2IntMap<RecipeCapability<?>> itemLimit(int items) {
        var map = new Reference2IntOpenHashMap<RecipeCapability<?>>(1);
        map.put(ItemRecipeCapability.CAP, items);
        return Reference2IntMaps.unmodifiable(map);
    }

    private AABB grindBound = new AABB(BlockPos.ZERO);
    private final List<IItemHandler> handlers = new ArrayList<>();
    @Nullable
    private TickableSubscription hurtSub;

    public OreGrinderMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public Reference2IntMap<RecipeCapability<?>> getOutputLimits() {
        if (!isFormed())
            return super.getOutputLimits();
        return switch (GTUtil.getTierByVoltage(getMaxVoltage())) {
            case 0 -> LIMIT_1;
            case 1 -> LIMIT_2;
            case 2 -> LIMIT_3;
            default -> LIMIT_4;
        };
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        updateBounds();

        handlers.clear();
        for (var handler : getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (handler instanceof IItemHandler itemHandler) {
                handlers.add(itemHandler);
            }
        }

        unsubscribe(hurtSub);
        hurtSub = subscribeServerTick(this::spinWheels);
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        clearGrinder();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        clearGrinder();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        clearGrinder();
    }

    private void clearGrinder() {
        unsubscribe(hurtSub);
        hurtSub = null;
        handlers.clear();
    }

    private void updateBounds() {
        var fl = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, 1, -1);
        var br = RelativeDirection.offsetPos(getBlockPos(), getFrontFacing(), getUpwardsFacing(), isFlipped(), 1, -1, -3);
        grindBound = AABB.of(BoundingBox.fromCorners(fl, br));
    }

    private void spinWheels() {
        if (isRemote() || getLevel() == null)
            return;
        if (getOffsetTimer() % 10 != 0)
            return;

        List<ItemEntity> itemEntities = new ArrayList<>();
        for (var entity : getLevel().getEntities(null, grindBound)) {
            if (entity instanceof ItemEntity itemEntity) {
                itemEntities.add(itemEntity);
            } else if (getRecipeLogic().isWorking()) {
                entity.hurt(entity.damageSources().cramming(), 2.0f);
            }
        }

        if (handlers.isEmpty())
            return;

        for (ItemEntity item : itemEntities) {
            if (item.isRemoved())
                continue;
            for (var handler : handlers) {
                item.setItem(ItemHandlerHelper.insertItem(handler, item.getItem(), false));
                if (item.getItem().isEmpty()) {
                    item.discard();
                    break;
                }
            }
        }
    }
}
