package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.inventory.AbstractContainerMenu;

import dan200.computercraft.client.gui.ClientInputHandler;
import dan200.computercraft.client.network.ClientNetworking;
import dan200.computercraft.shared.network.server.KeyEventServerMessage;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8ClientInputAccess;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = ClientInputHandler.class, remap = false)
public class ClientInputHandlerMixin implements CcUtf8ClientInputAccess {

    @Shadow
    @Final
    private AbstractContainerMenu menu;

    @Override
    public void tfg$charTypedCodepoint(int codepoint) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        ClientNetworking.sendToServer(new KeyEventServerMessage(
                menu,
                KeyEventServerMessage.Action.CHAR,
                codepoint));
    }
}
