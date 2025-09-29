package su.terrafirmagreg.core.common.data.tfgt;

import net.minecraft.world.level.block.Blocks;

import fi.dea.mc.deafission.common.data.FissionComponents;
import fi.dea.mc.deafission.core.components.EfficiencyComponent;
import fi.dea.mc.deafission.core.components.HeatComponent;
import fi.dea.mc.deafission.core.components.ThrottleComponent;

public class TFGFissionComponents {
    public static void addComponents() {

        FissionComponents.heat.put(Blocks.BEDROCK, new HeatComponent(Blocks.BEDROCK, 15_000));
        FissionComponents.heat.put(Blocks.BLUE_ICE, new HeatComponent(Blocks.BLUE_ICE, 0.5));
        /*
        Block glacialFur = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("ad_astra", "glacial_fur"));
        {
            FissionComponents.heat.put(glacialFur, new HeatComponent(glacialFur, 2));
        }*/

        FissionComponents.efficiency.put(Blocks.DIAMOND_BLOCK, new EfficiencyComponent(Blocks.DIAMOND_BLOCK, 10_000));

        FissionComponents.throttle.put(Blocks.ACACIA_WOOD, new ThrottleComponent(Blocks.ACACIA_WOOD, 10_000));
    }

}
