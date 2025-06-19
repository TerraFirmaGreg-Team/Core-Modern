package su.terrafirmagreg.core.common.data.tfgt.item;

import dev.emi.emi.screen.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InterplanetaryLink {

    private final ItemStack attachedTo;
    private long linkId = 0;

    public InterplanetaryLink(ItemStack item) {
        attachedTo = item;
        var tag = item.getOrCreateTag();
        if (tag.isEmpty()) return;
        linkId = tag.getInt("linkId");
    }

    public void save() {
        var tag = attachedTo.getOrCreateTag();
        tag.putLong("linkId", linkId);
    }

    public static void clear(ItemStack link) {
        link.setTag(new CompoundTag());
    }

    public void initLink(Level level, Player player) {
        linkId = level.getGameTime() * level.random.nextInt();
        save();
    }

    public WidgetGroup getWidget() {

        return null;
    }


    public void appendHoverText(@Nullable Level level, List<Component> tooltipComponents) {
        if (linkId == 0) {
            tooltipComponents.add(Component.translatable("item.tfg.interplanetarylink.not_linked"));
            tooltipComponents.add(Component.translatable("item.tfg.interplanetarylink.not_linked_hint"));
        } else {
            tooltipComponents.add(Component.translatable("item.tfg.interplanetarylink.linked"));
        }
    }
}
