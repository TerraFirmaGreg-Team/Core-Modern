package su.terrafirmagreg.tfc_textile.datagen;

import java.util.HashMap;
import java.util.Map;

import net.dries007.tfc.common.items.HideItemType;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import su.terrafirmagreg.tfc_textile.TFCtextile;
import su.terrafirmagreg.tfc_textile.item.TFCTextileItems;
import su.terrafirmagreg.tfc_textile.loot.AddItemModifier;
import su.terrafirmagreg.tfc_textile.loot.RemoveItemModifier;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, TFCtextile.MODID);
    }

    private static final Map<String, Item> FUR_DEFS = new HashMap<>();

    static {
        FUR_DEFS.put("black_bear", TFCTextileItems.BLACK_BEAR_FUR.get());
        FUR_DEFS.put("caribou", TFCTextileItems.CARIBOU_FUR.get());
        FUR_DEFS.put("crocodile", TFCTextileItems.CROCODILE_LEATHER.get());
        FUR_DEFS.put("direwolf", TFCTextileItems.DIREWOLF_FUR.get());
        FUR_DEFS.put("grizzly_bear", TFCTextileItems.GRIZZLY_BEAR_FUR.get());
        FUR_DEFS.put("lion", TFCTextileItems.LION_FUR.get());
        FUR_DEFS.put("panther", TFCTextileItems.PANTHER_FUR.get());
        FUR_DEFS.put("polar_bear", TFCTextileItems.POLAR_BEAR_FUR.get());
        FUR_DEFS.put("sabertooth", TFCTextileItems.SABERTOOTH_FUR.get());
        FUR_DEFS.put("tiger", TFCTextileItems.TIGER_FUR.get());
        FUR_DEFS.put("cougar", TFCTextileItems.COUGAR_FUR.get());
    }

    @Override
    protected void start() {

        FUR_DEFS.forEach((k, v) -> {
            add(k + "_fur_add", new AddItemModifier(new LootItemCondition[] {
                    new LootTableIdCondition.Builder(new ResourceLocation("tfc:entities/" + k)).build() }, v));
            add(k + "_remove_medium_hide", new RemoveItemModifier(new LootItemCondition[] {
                    new LootTableIdCondition.Builder(new ResourceLocation("tfc:entities/" + k)).build() }, TFCItems.HIDES.get(HideItemType.RAW).get(HideItemType.Size.LARGE).get()));
            add(k + "_remove_large_hide", new RemoveItemModifier(new LootItemCondition[] {
                    new LootTableIdCondition.Builder(new ResourceLocation("tfc:entities/" + k)).build() }, TFCItems.HIDES.get(HideItemType.RAW).get(HideItemType.Size.MEDIUM).get()));

        });

    }
}
