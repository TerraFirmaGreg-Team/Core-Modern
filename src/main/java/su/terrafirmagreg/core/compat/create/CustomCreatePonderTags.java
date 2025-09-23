package su.terrafirmagreg.core.compat.create;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class CustomCreatePonderTags {

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                CatnipServices.REGISTRIES::getKeyOrThrow);

        itemHelper.addToTag(AllCreatePonderTags.ARM_TARGETS)
                .add(Items.CHARCOAL)
                .add(TFCBlocks.CRUCIBLE.get())
                //.add(FLBlocks.CURED_OVEN_TOP.get(BRICK).get())
                //.add(FLBlocks.CURED_OVEN_BOTTOM.get(BRICK).get())
                //.add(FLBlocks.CURED_OVEN_HOPPER.get(BRICK).get())
                .add(PartAbility.ROTOR_HOLDER.getAllBlocks().stream().findAny().get());

    }

}
