package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.EntryGroup;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import su.terrafirmagreg.core.mixins.common.minecraft.IBlockLootSubProviderAccessor;

@Mixin(value = MixinHelpers.class, remap = false)
public abstract class MixinHelpersMixin {

    @Redirect(method = "lambda$generateGTDynamicLoot$23", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/loot/BlockLootSubProvider;createSilkTouchDispatchTable(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/storage/loot/entries/LootPoolEntryContainer$Builder;)Lnet/minecraft/world/level/storage/loot/LootTable$Builder;"), remap = true)
    private static LootTable.Builder tfg$lambda$generateGTDynamicLoot$23(Block pBlock, LootPoolEntryContainer.Builder<?> pBuilder, @Local int oreMultiplier, @Local Block block, @Local(argsOnly = true) Material material, @Local(argsOnly = true) TagPrefix.OreType type) {

        var poorDropStack = ChemicalHelper.get(TFGTagPrefix.poorRawOre, material);
        if (poorDropStack.isEmpty()) poorDropStack = ChemicalHelper.get(TagPrefix.gem, material);
        if (poorDropStack.isEmpty()) poorDropStack = ChemicalHelper.get(TagPrefix.dust, material);

        var normalDropStack = ChemicalHelper.get(TagPrefix.rawOre, material);
        if (normalDropStack.isEmpty()) normalDropStack = ChemicalHelper.get(TagPrefix.gem, material);
        if (normalDropStack.isEmpty()) normalDropStack = ChemicalHelper.get(TagPrefix.dust, material);

        var richDropStack = ChemicalHelper.get(TFGTagPrefix.richRawOre, material);
        if (richDropStack.isEmpty()) richDropStack = ChemicalHelper.get(TagPrefix.gem, material);
        if (richDropStack.isEmpty()) richDropStack = ChemicalHelper.get(TagPrefix.dust, material);

        return LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(
                                AlternativesEntry.alternatives(
                                                LootItem.lootTableItem(block)
                                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                                        .when(IBlockLootSubProviderAccessor.getHasNoSilkTouchCondition().invert())
                                        )
                                        .otherwise(
                                                EntryGroup.list(
                                                        LootItem.lootTableItem(poorDropStack.getItem())
                                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, Math.max(1, oreMultiplier))))
                                                                .setWeight(20)
                                                                .apply(ApplyExplosionDecay.explosionDecay()),
                                                        LootItem.lootTableItem(normalDropStack.getItem())
                                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, Math.max(1, oreMultiplier))))
                                                                .setWeight(60)
                                                                .apply(ApplyExplosionDecay.explosionDecay()),
                                                        LootItem.lootTableItem(richDropStack.getItem())
                                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, Math.max(1, oreMultiplier))))
                                                                .setWeight(20)
                                                                .apply(ApplyExplosionDecay.explosionDecay())
                                                )

                                        )
                        )
        );
    }

}
