package su.terrafirmagreg.core.common.data.tfgt.interdim_logistics;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

public class NetworkReceiverConfigEntry {
    @Getter
    @Setter
    private int distinctInventory;
    @Getter
    @Setter
    private LogicMode currentMode;
    @Getter
    @Setter
    private int currentCooldown;

    public enum LogicMode implements EnumSelectorWidget.SelectableEnum {
        COOLDOWN("Cooldown after receiving (seconds)", "transfer_any"),
        REDSTONE_ENABLE("Enable when receiving redstone signal", "transfer_any"),
        REDSTONE_DISABLE("Disable when receiving redstone signal", "transfer_any");

        @Getter
        public final String tooltip;
        @Getter
        public final IGuiTexture icon;

        LogicMode(String tooltip, String textureName) {
            this.tooltip = tooltip;
            this.icon = new ResourceTexture("gtceu:textures/gui/icon/transfer_mode/" + textureName + ".png");
        }

    }

    public NetworkReceiverConfigEntry(int inv) {
        distinctInventory = inv;
        currentCooldown = 0;
        currentMode = LogicMode.COOLDOWN;

    }

    public NetworkReceiverConfigEntry(CompoundTag tag) {
        distinctInventory = tag.getInt("circuit");
        currentMode = LogicMode.values()[tag.getInt("currentMode")];
        currentCooldown = tag.getInt("currentCooldown");
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putInt("circuit", distinctInventory);
        tag.putInt("currentMode", currentMode.ordinal());
        tag.putInt("currentCooldown", currentCooldown);
        return tag;
    }
}
