package su.terrafirmagreg.core.common.data.tfgt;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dea.mc.deafission.common.data.FissionComponents;
import fi.dea.mc.deafission.core.components.EfficiencyComponent;
import fi.dea.mc.deafission.core.components.HeatComponent;
import fi.dea.mc.deafission.core.components.ThrottleComponent;

public class TFGFissionComponents {

    private static final Supplier<Block> glacian_frame = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "glacian_wool_frame"));
    private static final Supplier<Block> aes_frame = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "aes_insulation_frame"));

    public static void addComponents() {

        FissionComponents.heat.put(Blocks.BEDROCK, new HeatComponent(Blocks.BEDROCK, 15_000));
        FissionComponents.heat.put(Blocks.BLUE_ICE, new HeatComponent(Blocks.BLUE_ICE, 0.5));

        FissionComponents.heat.put(glacian_frame.get(), new HeatComponent(glacian_frame.get(), 2));

        FissionComponents.heat.put(aes_frame.get(), new HeatComponent(aes_frame.get(), 1));

        FissionComponents.efficiency.put(Blocks.DIAMOND_BLOCK, new EfficiencyComponent(Blocks.DIAMOND_BLOCK, 10_000));

        FissionComponents.throttle.put(Blocks.ACACIA_WOOD, new ThrottleComponent(Blocks.ACACIA_WOOD, 10_000));

    }

}
