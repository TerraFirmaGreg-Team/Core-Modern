package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.registry.ModItems;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;

/**
 * /tfg debug spacesuit: equip a full space suit with filled oxygen tank
 */
public class DebugSpaceSuitCommand {

    private static final ResourceLocation NITROX = new ResourceLocation("tfg", "compressed_nitrox");

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("spacesuit")
                .executes(c -> giveSpaceSuit(c.getSource())));
    }

    private static int giveSpaceSuit(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ItemStack helmet = new ItemStack(ModItems.SPACE_HELMET.get());
        ItemStack chestplate = new ItemStack(ModItems.SPACE_SUIT.get());
        ItemStack leggings = new ItemStack(ModItems.SPACE_PANTS.get());
        ItemStack boots = new ItemStack(ModItems.SPACE_BOOTS.get());

        if (!fillWithBreathableAir(chestplate)) {
            source.sendFailure(Component.literal("Fluid tfg:compressed_nitrox not found"));
            return 0;
        }

        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        player.setItemSlot(EquipmentSlot.LEGS, leggings);
        player.setItemSlot(EquipmentSlot.FEET, boots);

        return 1;
    }

    private static boolean fillWithBreathableAir(ItemStack chestplate) {
        if (!(chestplate.getItem() instanceof SpaceSuitItem suit))
            return false;

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(NITROX);
        if (fluid == null) {
            return false;
        }

        var container = suit.getFluidContainer(chestplate);
        long capacity = container.getTankCapacity(0);
        container.internalInsert(
                FluidHolder.ofMillibuckets(fluid, FluidConstants.toMillibuckets(capacity)),
                false);
        return true;
    }
}
