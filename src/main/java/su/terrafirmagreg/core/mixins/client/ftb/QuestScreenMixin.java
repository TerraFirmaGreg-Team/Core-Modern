package su.terrafirmagreg.core.mixins.client.ftb;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.patchouli.api.PatchouliAPI;


@OnlyIn(Dist.CLIENT)
@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestScreenMixin {
    private static final String GUIDE_RL = "tfc:field_guide";

    @Inject(method = "handleClick", at = @At(value = "HEAD"), cancellable = true)
    private void catchTfcFieldGuidePaths(String scheme, String path, CallbackInfoReturnable<Boolean> cir) {
        // Skip catch if button pressed isn't related to guide books
        if (!(scheme.equals("guide")) || path.isBlank()) {
            return;
        }

        // click is handled, return true on inject exit
        cir.setReturnValue(true);
        String[] bookArgs = path.split(" ");

        try {
            // Validation
            if (bookArgs.length > 3) {
                throw new IllegalArgumentException("more than 3 arguments");
            } else if (!(bookArgs[0].equals(GUIDE_RL))) {
                throw new IllegalArgumentException("invalid field guide resource location");
            } else if (bookArgs.length > 1 && ResourceLocation.tryParse(bookArgs[1]) == null) {
                throw new IllegalArgumentException("invalid entry resource location");
            } else if (bookArgs.length == 3 && !StringUtils.isNumeric(bookArgs[2])) {
                throw new IllegalArgumentException("invalid page number");
            }

            //open-patchouli-book command syntax: /... targets book entry (optional) page
            ResourceLocation bookAddress = ResourceLocation.tryParse(GUIDE_RL);
            if (bookArgs.length == 1) {
                PatchouliAPI.get().openBookGUI(bookAddress);
            } else {
                ResourceLocation entryAddress = ResourceLocation.tryParse(bookArgs[1]);
                int pageNumber = (bookArgs.length != 3) ? 0 : Integer.parseInt(bookArgs[2]);
                PatchouliAPI.get().openBookEntry(bookAddress, entryAddress, pageNumber);
            }

        } catch (Exception e) {
            String failureMessage = "failed to open guide, invalid path:" + path + "\n" + "reason: " + e;
            chatMessage(failureMessage);
        }
    }

    @Unique
    private void chatMessage(String message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }
}
