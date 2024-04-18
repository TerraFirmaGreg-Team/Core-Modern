package su.terrafirmagreg.core;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;

@Config(id = TerraFirmaGreg.MOD_ID)
public final class TFGConfig {

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("List of modids, which advancements has been excluded from game.")
    public String[] modIds = new String[] { "minecraft", "data/tfc", "create", "gtceu" };

    @Configurable
    @Configurable.Synchronized
    @Configurable.Comment("Is TFC Compat Enabbled? Requied restart!")
    public boolean enableTFCAmbientCompat = true;
}
