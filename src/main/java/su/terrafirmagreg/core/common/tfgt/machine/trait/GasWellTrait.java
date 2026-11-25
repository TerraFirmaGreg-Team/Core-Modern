package su.terrafirmagreg.core.common.tfgt.machine.trait;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam.GasWellMachine;

public class GasWellTrait extends MachineTrait {

    public static final int FLUID_CONSUMPTION_PER_TICK = 10;
    public static int EXPLOSIVE_CONSUMPTION_INTERVAL = 240;

    @Getter
    @SaveField
    private int timer = 0;
    @SaveField
    private boolean hasConsumedExplosive = false;
    @Getter
    private boolean outputBlocked = false;
    @Nullable
    private BedrockFluidVeinSavedData cachedSavedData = null;
    @Nullable
    private TickableSubscription tickSubscription = null;

    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(GasWellMachine.class);
    }

    public GasWellMachine getGasWell() {
        return (GasWellMachine) getMachine();
    }

    public boolean isActive() {
        return hasConsumedExplosive;
    }

    public void start() {
        tickSubscription = subscribeServerTick(tickSubscription, this::tick);
    }

    public void stop() {
        unsubscribe(tickSubscription);
        tickSubscription = null;
    }

    public void reset() {
        timer = 0;
        hasConsumedExplosive = false;
        outputBlocked = false;
    }

    @Override
    public void onMachineUnload() {
        super.onMachineUnload();
        tickSubscription = null;
        cachedSavedData = null;
    }

    public BedrockFluidVeinSavedData getSavedData(ServerLevel level) {
        if (cachedSavedData == null) {
            cachedSavedData = BedrockFluidVeinSavedData.getOrCreate(level);
        }
        return cachedSavedData;
    }

    @Nullable
    public FluidVeinWorldEntry getVeinEntry() {
        if (!(getLevel() instanceof ServerLevel serverLevel))
            return null;
        int chunkX = SectionPos.blockToSectionCoord(getBlockPos().getX());
        int chunkZ = SectionPos.blockToSectionCoord(getBlockPos().getZ());
        var entry = getSavedData(serverLevel).getFluidVeinWorldEntry(chunkX, chunkZ);
        return entry != null && entry.getDefinition() != null ? entry : null;
    }

    @Nullable
    public Fluid getVeinFluid() {
        var entry = getVeinEntry();
        return entry == null ? null : entry.getDefinition().getStoredFluid().get();
    }

    public int getFluidToProduce() {
        var entry = getVeinEntry();
        return entry == null ? 0 : getFluidToProduce(entry);
    }

    public int getVeinPercent() {
        var entry = getVeinEntry();
        return entry == null ? 0 : entry.getOperationsRemaining() * 100 / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
    }

    private void tick() {
        var machine = getGasWell();
        if (!(machine.getLevel() instanceof ServerLevel serverLevel))
            return;
        if (!machine.isFormed() || machine.getDefaultPatternState().hasErrors())
            return;

        int chunkX = SectionPos.blockToSectionCoord(machine.getBlockPos().getX());
        int chunkZ = SectionPos.blockToSectionCoord(machine.getBlockPos().getZ());

        var savedData = getSavedData(serverLevel);
        var entry = savedData.getFluidVeinWorldEntry(chunkX, chunkZ);

        if (entry == null || entry.getDefinition() == null)
            return;

        var veinFluid = entry.getDefinition().getStoredFluid().get();
        if (veinFluid == null)
            return;

        // Only work for natural_gas
        var naturalGas = GTMaterials.NaturalGas.getFluid();
        if (naturalGas == null || !veinFluid.isSame(naturalGas))
            return;

        if (!hasConsumedExplosive) {
            if (!consumeExplosive())
                return;
            timer = 0;
            machine.setActive(true);
        }

        // Consumme water or steam
        if (!consumeFluid()) {
            // If no fluid just put on break : stop timer
            // Starts again with fluid
            return;
        }

        timer++;
        int intervalTicks = EXPLOSIVE_CONSUMPTION_INTERVAL * 20;
        if (timer >= intervalTicks) {
            timer = 0;
            if (!consumeExplosive()) {
                hasConsumedExplosive = false;
                machine.setActive(false);
                return;
            }
        }

        // Produce gas once per second
        if (machine.getOffsetTimer() % 20 == 0) {
            int produced = getFluidToProduce(entry);
            if (produced <= 0)
                return;

            var toOutput = new FluidStack(veinFluid, produced);
            outputBlocked = !canOutputFluid(toOutput);
            if (outputBlocked)
                return; // If the output is full don't deplete the vein but keep consumming explosives and input fluid

            outputFluid(toOutput);
            savedData.depleteVein(chunkX, chunkZ, 0, false);
        }
    }

    private boolean consumeExplosive() {
        var itemHandler = getGasWell().getInputItemHandler();
        if (itemHandler == null)
            return false;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            var stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && isExplosive(stack)) {
                var extracted = itemHandler.extractItemInternal(i, 1, false);
                if (!extracted.isEmpty()) {
                    hasConsumedExplosive = true;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean consumeFluid() {
        var inputTank = getGasWell().getInputFluidTank();
        if (inputTank == null)
            return false;

        int waterAmount = FLUID_CONSUMPTION_PER_TICK;
        int steamAmount = FLUID_CONSUMPTION_PER_TICK * 2;

        var waterStack = GTMaterials.Water.getFluid(waterAmount);
        var steamStack = GTMaterials.Steam.getFluid(steamAmount);

        var drained = inputTank.drainInternal(waterStack, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty() && drained.getAmount() >= waterAmount) {
            inputTank.drainInternal(waterStack, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }

        drained = inputTank.drainInternal(steamStack, IFluidHandler.FluidAction.SIMULATE);
        if (!drained.isEmpty() && drained.getAmount() >= steamAmount) {
            inputTank.drainInternal(steamStack, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }

        return false;
    }

    private boolean canOutputFluid(FluidStack fluid) {
        var outputTank = getGasWell().getOutputFluidTank();
        if (outputTank == null)
            return false;
        return outputTank.fillInternal(fluid, IFluidHandler.FluidAction.SIMULATE) >= fluid.getAmount();
    }

    private void outputFluid(FluidStack fluid) {
        var outputTank = getGasWell().getOutputFluidTank();
        if (outputTank == null)
            return;
        outputTank.fillInternal(fluid, IFluidHandler.FluidAction.EXECUTE);
    }

    private boolean isExplosive(ItemStack stack) {
        return stack.is(TFGTags.Items.Explosives);
    }

    private int getFluidToProduce(FluidVeinWorldEntry entry) {
        int depletedYield = entry.getDefinition().getDepletedYield();
        int regularYield = entry.getFluidYield();
        int remainingOperations = entry.getOperationsRemaining();
        return Math.max(depletedYield,
                regularYield * remainingOperations / BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
    }
}
