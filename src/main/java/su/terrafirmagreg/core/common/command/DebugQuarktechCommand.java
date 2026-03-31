package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.literal;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * /tfg debug quarktech: equip a fully charged QuarkTech suit
 */
public class DebugQuarktechCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("quark")
                .executes(c -> giveQuarktech(c.getSource())));
    }

    private static int giveQuarktech(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be run by a player"));
            return 0;
        }

        ItemStack helmet = chargeToMax(new ItemStack(GTItems.QUANTUM_HELMET.get()));
        ItemStack chestplate = chargeToMax(new ItemStack(GTItems.QUANTUM_CHESTPLATE.get()));
        ItemStack leggings = chargeToMax(new ItemStack(GTItems.QUANTUM_LEGGINGS.get()));
        ItemStack boots = chargeToMax(new ItemStack(GTItems.QUANTUM_BOOTS.get()));

        DebugCommandUtils.equipSlot(player, EquipmentSlot.HEAD, helmet);
        DebugCommandUtils.equipSlot(player, EquipmentSlot.CHEST, chestplate);
        DebugCommandUtils.equipSlot(player, EquipmentSlot.LEGS, leggings);
        DebugCommandUtils.equipSlot(player, EquipmentSlot.FEET, boots);

        return 1;
    }

    private static ItemStack chargeToMax(ItemStack stack) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null)
            electricItem.charge(electricItem.getMaxCharge(), electricItem.getTier(), true, false);
        return stack;
    }
}
