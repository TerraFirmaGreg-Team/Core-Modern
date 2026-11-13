package su.terrafirmagreg.core.compat.ae2.terminal;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

public record StagedStack(AEKey what, long amount, boolean craftMissing) {

    private static final String NBT_STACK = "stack";
    private static final String NBT_CRAFT = "craftMissing";

    public void write(FriendlyByteBuf buffer) {
        GenericStack.writeBuffer(new GenericStack(what, amount), buffer);
        buffer.writeBoolean(craftMissing);
    }

    public static StagedStack read(FriendlyByteBuf buffer) {
        var stack = GenericStack.readBuffer(buffer);
        return new StagedStack(stack.what(), stack.amount(), buffer.readBoolean());
    }

    public CompoundTag toTag() {
        var tag = new CompoundTag();
        tag.put(NBT_STACK, GenericStack.writeTag(new GenericStack(what, amount)));
        tag.putBoolean(NBT_CRAFT, craftMissing);
        return tag;
    }

    @Nullable
    public static StagedStack fromTag(CompoundTag tag) {
        var stack = GenericStack.readTag(tag.getCompound(NBT_STACK));
        return stack != null ? new StagedStack(stack.what(), stack.amount(), tag.getBoolean(NBT_CRAFT)) : null;
    }
}
