package su.terrafirmagreg.core.mixins.common.minecraft;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BlockLootSubProvider.class)
public interface IBlockLootSubProviderAccessor {

    @Accessor(value = "HAS_NO_SILK_TOUCH")
    static LootItemCondition.Builder getHasNoSilkTouchCondition() {
        throw new UnsupportedOperationException();
    }
}
