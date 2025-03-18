package su.terrafirmagreg.core.old_remove_after_1_21_release;//package su.terrafirmagreg.core.old_remove_after_1_21_release.recipes;
//
//import com.google.gson.JsonObject;
//import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
//import com.gregtechceu.gtceu.api.data.chemical.material.Material;
//import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
//import com.gregtechceu.gtceu.api.item.tool.GTToolType;
//import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.level.ItemLike;
//import net.minecraftforge.registries.ForgeRegistries;
//import org.jetbrains.annotations.NotNull;
//import su.terrafirmagreg.core.compat.gtceu.properties.TFCProperty;
//
//import javax.annotation.Nullable;
//import java.util.function.Consumer;
//
//public final class TFGData {
//
//    public static void remove(Consumer<ResourceLocation> consumer) {
//        //        consumer.accept(new ResourceLocation("tfc:data/tfc/item_heats/blooms"));
//    }
//
//    public static void init() {

//
//        //        addItemHeat(FirmacivItems.CANNON.get(), 2.875F, null, null);
//        //        addItemHeat(FirmacivItems.CANNON_BARREL.get(), 2.875F, null, null);
//        //        addItemHeat(FirmacivBlocks.CLEAT.get(), 2.875F, null, null);
//        //        addItemHeat(FirmacivItems.ANCHOR.get(), 2.875F, null, null);
//        //        addItemHeat(FirmacivItems.CANNONBALL.get(), 2.875F, null, null);
//    }
//
//    private static void addTagItemHeat(@NotNull final TagPrefix tagPrefix, @NotNull final Material material, @NotNull final TFCProperty prop, final float heatCapacity) {
//        final var stack = ChemicalHelper.get(tagPrefix, material);
//        addItemStackHeat(stack, prop, heatCapacity);
//    }
//
//    private static void addToolItemHeat(@NotNull final GTToolType toolType, @NotNull final Material material, @NotNull TFCProperty prop, final float heatCapacity) {
//        final var stack = ToolHelper.get(toolType, material);
//        addItemStackHeat(stack, prop, heatCapacity);
//    }
//
//    private static void addItemStackHeat(@NotNull final ItemStack stack, @NotNull final TFCProperty tfcProperty, final float heatCapacity) {
//        addItemHeat(stack.getItem(), heatCapacity, tfcProperty.getForgingTemp(), tfcProperty.getWeldingTemp());
//    }
//
//    private static void addItemHeat(@NotNull final ItemLike itemLike, final float heatCapacity, @Nullable final Integer forgingTemp, @Nullable final Integer weldingTemp) {
//        var rl = ForgeRegistries.ITEMS.getKey(itemLike.asItem());
//        if (rl == null) return;
//
//        var json = getItemHeatJson(itemLike.asItem(), heatCapacity, forgingTemp, weldingTemp);
//        TFGDynamicDataPack.addData("tfc:tfc/item_heats/" + rl.getPath(), json);
//    }
//
//    @NotNull
//    private static JsonObject getItemHeatJson(@NotNull final ItemLike item, final float heatCapacity, @Nullable final Integer forgingTemp, @Nullable final Integer weldingTemp) {
//        var json = new JsonObject();
//
//        json.add("ingredient", Ingredient.of(item).toJson());
//        json.addProperty("heat_capacity", heatCapacity);
//
//        if (forgingTemp != null) json.addProperty("forging_temperature", forgingTemp);
//        if (weldingTemp != null) json.addProperty("welding_temperature", weldingTemp);
//
//        return json;
//    }
//}
