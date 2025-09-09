package su.terrafirmagreg.core.mixins.common.minecraft;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {

    /**
     * Препятствует добавлению достижений в игру.
     * Но не удаляет достижения ваниллы, так как из-за этого перестает работать книга Patchouli.
     * Стоит поискать решение.
     * -
     * Prevents achievements from other mods.
     * However, it doesn't remove vanilla achievements, because otherwise Patchouli stops working.
     * It's worth looking for a better solution to hide the vanilla advancements.
     * */
    @Redirect(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementList;add(Ljava/util/Map;)V")
    )
    private void tfg$apply$advancementList$add(AdvancementList list, Map<ResourceLocation, Advancement.Builder> map) {
        map.entrySet().removeIf(entry -> !entry.getKey().getNamespace().equals("minecraft"));
        list.add(map);
    }
}
