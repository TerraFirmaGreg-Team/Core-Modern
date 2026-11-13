package su.terrafirmagreg.core.compat.ae2.terminal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;

public class OrderTerminalPart extends AbstractTerminalPart implements OrderTerminalHost {
    @PartModels
    public static final ResourceLocation MODEL_OFF = TFGCore.id("part/order_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = TFGCore.id("part/order_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    @Getter
    private final OrderTerminalLogic orderLogic = new OrderTerminalLogic(() -> getMainNode().getGrid(), () -> getHost().markForSave());

    public OrderTerminalPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public MenuType<?> getMenuType(Player player) {
        return OrderTerminalMenu.TYPE;
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        orderLogic.readFromNBT(data);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        orderLogic.writeToNBT(data);
    }
}
