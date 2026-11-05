package su.terrafirmagreg.core.mixins.common.firmalife;

import com.eerussianguy.firmalife.common.blockentities.FLBeehiveBlockEntity;
import com.eerussianguy.firmalife.common.blocks.FLBeehiveBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.common.environment.IGreenhouseReceiver;

import java.util.List;

@Mixin(value = FLBeehiveBlock.class, remap = false)
public abstract class FLBeehiveBlockMixin {

    @Inject(method = "lambda$addHoeOverlayInfo$6", at = @At(value = "NEW", target = "java/util/ArrayList"))
    private static void addGreenhouseInfo(List<Component> text, Level level, BlockPos pos, FLBeehiveBlockEntity hive, CallbackInfo ci) {
        if (((IGreenhouseReceiver) hive).tfg$isInsideValidGreenhouse()) {
            //            text.add(Component.literal("Inside a valid greenhouse").withStyle(ChatFormatting.GREEN));
            text.add(Component.translatable("tfg.tooltip.firmalife_inside_valid_greenhouse").withStyle(ChatFormatting.GREEN));
        }
    }
}
