package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

    @Shadow
    private static ListTag getListOfCompoundsOrNull(CompoundTag tag, String key) {
        return null;
    };

    @Inject(method = "postLoadChunk", at = @At(value = "HEAD"))
    private static void tfg$postLoadChunk(ServerLevel level, CompoundTag tag, CallbackInfoReturnable<LevelChunk.PostLoadProcessor> cir) {
        ListTag listtag1 = getListOfCompoundsOrNull(tag, "block_entities");
        if (listtag1 != null) {
            for (int i = 0; i < listtag1.size(); ++i) {
                CompoundTag compoundtag = listtag1.getCompound(i);
                String id = compoundtag.getString("id");
                if (id.equals("create_factory_logistics:factory_fluid_panel")) { //replace gauge blockEntities
                    compoundtag.remove("id");
                    compoundtag.putString("id", "create:factory_panel"); //reference to the new blockEntity
                    //TFGCore.LOGGER.debug("Replacing create_factory_logistics:factory_fluid_panel blockEntity");

                    // Everything below here is to change the gauge programing to work automatically with fluidlogistics
                    String[] corners = { "top_right", "top_left", "bottom_left", "bottom_right" };

                    for (String corner : corners) { //Runs for each of the 4 corners of the gauge
                        var cornerTag = compoundtag.getCompound(corner);
                        var itemFilter = ItemStack.of(cornerTag.getCompound("Filter")); //get the current set item (a bucket or cell with fluid)

                        Pair<FluidStack, ItemStack> emptyResult = GenericItemEmptying.emptyItem(level, itemFilter, true); //empty it to get the fluid it contains
                        FluidStack fluidStack = emptyResult.getFirst();
                        if (!fluidStack.isEmpty()) {
                            String fluidID = fluidStack.getFluid().getFluidType().toString(); //get the ID of the fluid
                            TFGCore.LOGGER.debug("Section {} contains fluid {}", corner, fluidID);
                            try {
                                // Create a new filter tag that will work with fluidlogistics
                                // Yes this is hard-coded, but it should be fine
                                var newFilter = TagParser.parseTag("{id:\"fluidlogistics:compressed_storage_tank\",Count: 1b, tag:{Virtual: 1b, Fluid: {FluidName: \"" + fluidID + "\", Amount: 1}}}");
                                cornerTag.remove("Filter");
                                cornerTag.put("Filter", newFilter);
                                //TFGCore.LOGGER.info("created new tag for gauge migration");
                            } catch (CommandSyntaxException e) {
                                TFGCore.LOGGER.error("Error migrating fluid gauge containing fluid {}. Removing filter instead", fluidID);
                                cornerTag.getCompound("Filter").remove("id"); //remove item as fallback
                            }
                        }
                    }

                }
            }
        }
    }
}
