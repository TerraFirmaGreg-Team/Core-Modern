package su.terrafirmagreg.core.common.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public class DataGen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new TFGSoundProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new TFGItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new TFGBlockModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new TFGBlockStateProvider(packOutput, existingFileHelper));
    }
}
