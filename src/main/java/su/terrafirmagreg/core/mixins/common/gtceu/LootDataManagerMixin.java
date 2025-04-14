package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.common.data.TFGBlocks;

import java.util.Map;

@Mixin(LootDataManager.class)
public abstract class LootDataManagerMixin {

	@Inject(method = "apply", at = @At(value = "HEAD"))
	public void gtceu$injectLootTables(Map<LootDataType<?>, Map<ResourceLocation, ?>> allElements, CallbackInfo ci) {
		if (GTCEu.isDataGen()) return;

		Map<ResourceLocation, LootTable> lootTables = (Map<ResourceLocation, LootTable>) allElements.get(LootDataType.TABLE);

		TFGBlocks.generateBudIndicatorLoot(lootTables);
	}
}
