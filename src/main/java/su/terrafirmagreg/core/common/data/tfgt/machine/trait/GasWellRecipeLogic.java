package su.terrafirmagreg.core.common.data.tfgt.machine.trait;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.FluidVeinWorldEntry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.steam.GasWellMachine;

@Getter
public class GasWellRecipeLogic {

    public boolean isActive() {
        return hasConsumedExplosive;
    }

    public static final int FLUID_CONSUMPTION_PER_TICK = 10;
    public static int EXPLOSIVE_CONSUMPTION_INTERVAL = 120;

    private final GasWellMachine machine;

    private int timer = 0;

    private boolean hasConsumedExplosive = false;

    public GasWellRecipeLogic(GasWellMachine machine) {
        this.machine = machine;
    }

    public void reset() {
        timer = 0;
        hasConsumedExplosive = false;
    }

    public void tick() {
        if (!(machine.getLevel() instanceof ServerLevel serverLevel))
            return;
        if (!machine.isFormed() || machine.getMultiblockState().hasError())
            return;

        int chunkX = SectionPos.blockToSectionCoord(machine.getPos().getX());
        int chunkZ = SectionPos.blockToSectionCoord(machine.getPos().getZ());

        var savedData = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
        var entry = savedData.getFluidVeinWorldEntry(chunkX, chunkZ);

        if (entry == null || entry.getDefinition() == null)
            return;

        // Check the vein
        var veinFluid = entry.getDefinition().getStoredFluid().get();
        if (veinFluid == null)
            return;

        // Only work for natural_gas
        var naturalGas = GTMaterials.NaturalGas.getFluid();
        if (naturalGas == null || !veinFluid.isSame(naturalGas))
            return;

        // Won't start if explosive wasn't consummed
        if (!hasConsumedExplosive) {
            if (!consumeExplosive())
                return;
        }

        // Always consumme water or steam
        if (!consumeFluid())
            return;

        // Use explosive
        timer++;
        if (timer >= EXPLOSIVE_CONSUMPTION_INTERVAL * 20) { // interval en secondes * 20 ticks
            if (!consumeExplosive()) {
                hasConsumedExplosive = false;
                timer = 0;
                return;
            }
            timer = 0;
        }

        // Production once per second
        if (machine.getOffsetTimer() % 20 == 0) {
            int produced = getFluidToProduce(entry);
            if (produced <= 0)
                return;
            outputFluid(new FluidStack(veinFluid, produced));
            // Amount of depletion and override the stat veins
            savedData.depleteVein(chunkX, chunkZ, 5, true);
        }
    }

    private boolean consumeFluid() {
        var inputTank = machine.getInputFluidTank();
        if (inputTank == null)
            return false;

        int waterAmount = FLUID_CONSUMPTION_PER_TICK;
        int steamAmount = FLUID_CONSUMPTION_PER_TICK * 160;

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

    private void outputFluid(FluidStack fluid) {
        var outputTank = machine.getOutputFluidTank();
        if (outputTank == null)
            return;
        outputTank.fillInternal(fluid, IFluidHandler.FluidAction.EXECUTE);
    }

    private boolean consumeExplosive() {
        var itemHandler = machine.getInputItemHandler();
        if (itemHandler == null)
            return false;

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            var stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty() && isExplosive(stack)) {
                itemHandler.extractItemInternal(i, 1, false);
                hasConsumedExplosive = true;
                return true;
            }
        }
        return false;
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
