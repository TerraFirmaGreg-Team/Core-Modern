package su.terrafirmagreg.core.compat.kjs.events;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;

public class TFGMaterialInfoModification extends EventJS {

    public static final HashSet<ResourceLocation> EXCLUDED_ITEMS = new HashSet<>();
    public static final HashMap<Item, ItemMaterialInfo> ADD_ITEMS = new HashMap<>();

    public TFGMaterialInfoModification() {
        clear();
    }

    public void add(Item item, ItemMaterialInfo itemMaterialInfo) {
        ADD_ITEMS.put(item, itemMaterialInfo);
    }

    public void remove(Item itemToRemove) {
        var rl = ForgeRegistries.ITEMS.getKey(itemToRemove);
        EXCLUDED_ITEMS.add(rl);
    }

    @HideFromJS
    public static void clear() {
        EXCLUDED_ITEMS.clear();
        ADD_ITEMS.clear();
    }
}