package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.TFGCore;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GTOreVeinWidget.class, remap = false)
public abstract class GTOreVeinWidgetMixin extends WidgetGroup {

    @Shadow @Final
    public static int width;

    /**
     * Для того, чтобы в gui рудных жил отображались все виды руды (бедная, нормальная, богатая), а не только нормальная.
     * */
    @Inject(method = "setupBaseGui(Lcom/gregtechceu/gtceu/api/data/worldgen/GTOreDefinition;)V", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void tfg$setupBaseGui(GTOreDefinition oreDefinition, CallbackInfo ci){
        List<Integer> chances = oreDefinition.veinGenerator().getAllChances();

        var oreLists = tfg$getOreRawOres(oreDefinition);
        int n = oreLists.size();

        int x = (width - 18 * n) / 2;
        for (int i = 0; i < n; i++) {
            SlotWidget oreSlot = new SlotWidget(new CycleItemStackHandler(oreLists), i, x, 18, false, false);
            int finalI = i;
            oreSlot.setOnAddedTooltips((stack, tooltips) ->
                    tooltips.add(Component.nullToEmpty(LocalizationUtils.format("gtceu.jei.ore_vein_diagram.chance", chances.get(finalI)))));
            oreSlot.setIngredientIO(IngredientIO.OUTPUT);
            addWidget(oreSlot);
            x += 18;
        }

        ci.cancel();
    }

    /**
     * Метод используется для показывания игроку при нажатии R или U по сырому куску руды,
     * жилы в которых он появляется.
     * */
    @Inject(method = "getContainedOresAndBlocks", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void tfg$getContainedOresAndBlocks(GTOreDefinition oreDefinition, CallbackInfoReturnable<List<ItemStack>> cir) {

        var poorRawOre = TagPrefix.get("poorRawOre");
        var richRawOre = TagPrefix.get("richRawOre");

        if (poorRawOre == null || richRawOre == null)
            cir.setReturnValue(new ArrayList<>());

        var tempList = new ArrayList<ItemStack>();

        for (var oreEntry : oreDefinition.veinGenerator().getAllEntries()) {
            oreEntry.getKey().right().ifPresent(material -> {
                tempList.add(ChemicalHelper.get(poorRawOre, material));
                tempList.add(ChemicalHelper.get(TagPrefix.rawOre, material));
                tempList.add(ChemicalHelper.get(richRawOre, material));
            });
        }

        cir.setReturnValue(tempList);
    }

    /**
     * Метод возвращает список из списков типов руд жилы. Например в жиле с алмазами и графитом список будет следующий:
     * Список(Список(Алмаз бедный, Алмаз нормальный, Алмаз богатый), Список(Графит бедный, Графит нормальный, Графит богатый))
     * */
    @Unique
    private static List<List<ItemStack>> tfg$getOreRawOres(GTOreDefinition oreDefinition) {

        var poorRawOre = TagPrefix.get("poorRawOre");
        var richRawOre = TagPrefix.get("richRawOre");

        if (poorRawOre == null || richRawOre == null)
            return new ArrayList<>();


        var list = new ArrayList<List<ItemStack>>();

        oreDefinition.veinGenerator().getAllEntries().forEach(el -> {
            if (el.getKey().right().isPresent()) {
                var oreList = new ArrayList<ItemStack>();

                oreList.add(ChemicalHelper.get(poorRawOre, el.getKey().right().get()));
                oreList.add(ChemicalHelper.get(TagPrefix.rawOre, el.getKey().right().get()));
                oreList.add(ChemicalHelper.get(richRawOre, el.getKey().right().get()));

                list.add(oreList);
            }
        });

        return list;
    }
}