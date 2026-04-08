package su.terrafirmagreg.core.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.structure_processors.MineshaftProcessor;

public class TFGStructureProcessors {

    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, TFGCore.MOD_ID);

    public static final RegistryObject<StructureProcessorType<MineshaftProcessor>> MINESHAFT_PROCESSOR = STRUCTURE_PROCESSORS.register("mineshaft_processor",
            () -> () -> MineshaftProcessor.CODEC);

}
