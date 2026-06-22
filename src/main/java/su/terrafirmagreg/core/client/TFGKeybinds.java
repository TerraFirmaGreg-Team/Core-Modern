package su.terrafirmagreg.core.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;

@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TFGKeybinds {

    public static final KeyMapping SPRAY_CAN_MENU = new KeyMapping(
            "key.tfg.spray_can_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.tfg");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SPRAY_CAN_MENU);
    }
}
