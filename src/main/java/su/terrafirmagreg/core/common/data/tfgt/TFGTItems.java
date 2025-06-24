package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import su.terrafirmagreg.core.common.data.tfgt.item.InterplanetaryLinkBehaviour;

import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

public class TFGTItems {

    public static void register () {}

    public static ItemEntry<ComponentItem> INTERPLANETARY_LINK = REGISTRATE.item("interplanetary_link", ComponentItem::create)
            .lang("Interplanetary Link")
            .onRegister(item -> item.attachComponents(new InterplanetaryLinkBehaviour()))
            .register();

}
