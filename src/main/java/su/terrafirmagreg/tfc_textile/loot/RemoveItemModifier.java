package su.terrafirmagreg.tfc_textile.loot;

import java.awt.*;
import java.util.Iterator;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoveItemModifier extends LootModifier {
    public static final Supplier<Codec<RemoveItemModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(ForgeRegistries.ITEMS.getCodec()
            .fieldOf("item").forGetter(m -> m.item)).apply(inst, RemoveItemModifier::new)));

    private final Item item;

    public RemoveItemModifier(LootItemCondition[] conditionsIn, Item item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        for (LootItemCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return generatedLoot;
            }
        }

        ItemStack removedItem = new ItemStack(this.item);
        Iterator<ItemStack> iterator = generatedLoot.iterator();

        while (iterator.hasNext()) {
            ItemStack currentItem = iterator.next();

            if (ItemStack.isSameItem(currentItem, removedItem)) {
                iterator.remove(); // Safe way to remove the item
            }
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
