package su.terrafirmagreg.core.mixins.client.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.common.data.utils.CustomSpawnHelper;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab")
@OnlyIn(Dist.CLIENT)
public abstract class GameTabMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tfg$addCustomSpawn(CreateWorldScreen this$0, CallbackInfo ci, @Local(ordinal = 0) GridLayout.RowHelper gridlayout$rowhelper) {

        CycleButton<CustomSpawnHelper.CustomSpawnCondition> spawnCycleButton = gridlayout$rowhelper
                .addChild(CycleButton.<CustomSpawnHelper.CustomSpawnCondition>builder(s -> (Component.translatable("tfg.gui.spawn_condition." + s.id()).append(" ").append(s.difficulty())))
                        .withValues(CustomSpawnHelper.CUSTOM_SPAWN_CONDITIONS.values()).create(0, 0, 210, 20, Component.translatable("tfg.gui.spawn_condition.title"), (button, condition) -> {
                            TFGConfig.COMMON.NEW_WORLD_SPAWN.set(condition.id());
                            button.setTooltip(Tooltip.create(Component.translatable("tfg.gui.spawn_condition.tooltip." + condition.id())));
                        }));
        spawnCycleButton.setValue(CustomSpawnHelper.CUSTOM_SPAWN_CONDITIONS.get("default"));
        spawnCycleButton.setTooltip(Tooltip.create(Component.translatable("tfg.gui.spawn_condition.tooltip.default")));
    }

}
