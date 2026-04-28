package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;

import net.createmod.catnip.data.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.TFGCore;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(method = "lambda$postLoadChunk$10(Lnet/minecraft/nbt/ListTag;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/ListTag;Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getBoolean(Ljava/lang/String;)Z", shift = At.Shift.AFTER))
    private static void tfg$postLoadChunk(ListTag listtag, ServerLevel level, ListTag listtag1, LevelChunk p_196904_, CallbackInfo ci, @Local(name = "compoundtag") CompoundTag compoundtag) {
        String id = compoundtag.getString("id");
        if (!id.equals("create_factory_logistics:factory_fluid_panel")) //replace fluid gauge blockEntities
            return;

        compoundtag.remove("id");
        compoundtag.putString("id", "create:factory_panel"); //reference to the new blockEntity

        // Everything below here is to change the gauge programing to work automatically with fluidlogistics
        String[] corners = { "top_right", "top_left", "bottom_left", "bottom_right" };

        for (String corner : corners) { //Runs for each of the 4 corners of the gauge
            var cornerTag = compoundtag.getCompound(corner);
            var itemFilter = ItemStack.of(cornerTag.getCompound("Filter")); //get the current set item (a bucket or cell with fluid)

            Pair<FluidStack, ItemStack> emptyResult = GenericItemEmptying.emptyItem(level, itemFilter, true); //empty it to get the fluid it contains
            FluidStack fluidStack = emptyResult.getFirst();
            if (fluidStack.isEmpty())
                continue;

            String fluidID = fluidStack.getFluid().getFluidType().toString(); //get the ID of the fluid
            try {
                // Create a new filter tag that will work with fluidlogistics
                // Yes this is hard-coded, but it should be fine
                var newFilter = TagParser.parseTag("{id:\"fluidlogistics:compressed_storage_tank\",Count: 1b, tag:{Virtual: 1b, Fluid: {FluidName: \"" + fluidID + "\", Amount: 1}}}");
                cornerTag.remove("Filter");
                cornerTag.put("Filter", newFilter);

            } catch (Exception e) {
                TFGCore.LOGGER.error("Error migrating fluid gauge containing fluid {} at position ({} {} {}). Removing filter instead", fluidID, compoundtag.getInt("x"), compoundtag.getInt("y"),
                        compoundtag.getInt("z"));
                cornerTag.getCompound("Filter").remove("id"); //remove filter as fallback
            }
            TFGCore.LOGGER.info("Successfully migrated fluid gauge containing fluid {} at position ({} {} {})", fluidID, compoundtag.getInt("x"), compoundtag.getInt("y"),
                    compoundtag.getInt("z"));
        }
    }

}
