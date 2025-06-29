package su.terrafirmagreg.core.compat.kjs;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import su.terrafirmagreg.core.common.TFGHelpers;
import su.terrafirmagreg.core.compat.gtceu.materials.TFGMaterialFlags;
import su.terrafirmagreg.core.compat.gtceu.TFGPropertyKeys;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;
import su.terrafirmagreg.core.compat.kjs.events.TFGStartupEvents;

public final class TFGKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {

        RegistryInfo.BLOCK.addType("tfg:decorative_plant", DecorativePlantBlockBuilder.class, DecorativePlantBlockBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("TFGTagPrefix", TFGTagPrefix.class);
        event.add("TFGPropertyKey", TFGPropertyKeys.class);
        event.add("TFGMaterialFlags", TFGMaterialFlags.class);
        event.add("TFGHelpers", TFGHelpers.class);

        event.add("ToolHelper", ToolHelper.class);
        event.add("MaterialFlags", MaterialFlags.class);
        event.add("ItemMaterialInfo", ItemMaterialInfo.class);
        event.add("MaterialStack", MaterialStack.class);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        TFGStartupEvents.GROUP.register();
    }
}
