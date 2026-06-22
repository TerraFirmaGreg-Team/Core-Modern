package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.ItemHandlerHelper;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = MovementBehaviour.class, remap = false)
public interface MovementBehaviourMixin {

    /**
     * @author Pyritie
     * @reason Inject doesn't work inside interfaces
     */
    @Overwrite
    default void dropItem(MovementContext context, ItemStack stack) {
        // Prevent any ores from being collected
        if (stack.is(Tags.Items.RAW_MATERIALS) || stack.is(TFGTags.Items.RICH_RAW_MATERIALS) || stack.is(TFGTags.Items.POOR_RAW_MATERIALS)) {
            var dim = context.world.dimension();

            // Don't drop ores below Y=80
            if (dim == Level.NETHER && TFGConfig.SERVER.enableBeneathMiningRestrictions.get()) {
                if (context.position.y <= TFGConfig.SERVER.disabledBeneathMiningYLevel.get()) {
                    return;
                }
            }
            // Don't drop ores at all on venus/mercury
            else if ((dim == Planet.VENUS || dim == Planet.MERCURY) && TFGConfig.SERVER.enableHotPlanetMiningRestrictions.get()) {
                return;
            }
        }

        // Here downwards is copied and pasted from the un-overwritten method

        ItemStack remainder;
        if (AllConfigs.server().kinetics.moveItemsToStorage.get())
            remainder = ItemHandlerHelper.insertItem(context.contraption.getStorage().getAllItems(), stack, false);
        else
            remainder = stack;

        if (remainder.isEmpty())
            return;

        Vec3 vec = context.position;
        if (vec == null)
            return;

        ItemEntity itemEntity = new ItemEntity(context.world, vec.x, vec.y, vec.z, remainder);
        itemEntity.setDeltaMovement(context.motion.add(0, 0.5f, 0)
                .scale(context.world.random.nextFloat() * .3f));
        context.world.addFreshEntity(itemEntity);
    }
}
