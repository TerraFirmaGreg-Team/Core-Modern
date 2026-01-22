package su.terrafirmagreg.core.common.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

import lombok.Getter;

public class ArtisanPattern {
    public static final int MAX_WIDTH = 6;
    public static final int MAX_HEIGHT = 6;

    public static ArtisanPattern fromJson(JsonObject json) {
        final JsonArray array = json.getAsJsonArray("pattern");
        final boolean empty = GsonHelper.getAsBoolean(json, "outside_slot_required", true);

        final int height = array.size();
        if (height > MAX_HEIGHT)
            throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_HEIGHT + " is maximum");
        if (height == 0)
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");

        final int width = GsonHelper.convertToString(array.get(0), "pattern[ 0 ]").length();
        if (width > MAX_WIDTH)
            throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_WIDTH + " is maximum");

        final ArtisanPattern pattern = new ArtisanPattern(width, height, empty);
        for (int r = 0; r < height; ++r) {
            String row = GsonHelper.convertToString(array.get(r), "pattern[" + r + "]");
            if (r > 0 && width != row.length())
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            for (int c = 0; c < width; c++) {
                pattern.set(r * width + c, row.charAt(c) != ' ');
            }
        }
        return pattern;
    }

    public static ArtisanPattern fromNetwork(FriendlyByteBuf buffer) {
        final int width = buffer.readVarInt();
        final int height = buffer.readVarInt();
        final long data = buffer.readLong();
        final boolean empty = buffer.readBoolean();
        return new ArtisanPattern(width, height, data, empty);
    }

    @Getter
    private final int width;
    @Getter
    private final int height;
    private final boolean empty;

    @Getter
    private long data;

    public ArtisanPattern() {
        this(MAX_WIDTH, MAX_HEIGHT, false);
    }

    public ArtisanPattern(int width, int height, boolean empty) {
        this(width, height, (1L << (width * height)) - 1, empty);
    }

    private ArtisanPattern(int width, int height, long data, boolean empty) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.empty = empty;
    }

    public boolean isOutsideSlotRequired() {
        return empty;
    }

    public void setAll(boolean value) {
        data = value ? (1L << (width * height)) - 1 : 0;
    }

    public void set(int x, int y, boolean value) {
        set(x + (long) y * width, value);
    }

    public void set(long index, boolean value) {
        assert index >= 0 && index < 64;
        if (value) {
            data |= 1L << index;
        } else {
            data &= ~(1L << index);
        }
    }

    public boolean get(int x, int y) {
        return get(x + (long) y * width);
    }

    public boolean get(long index) {
        assert index >= 0 && index < 64;
        return ((data >> index) & 0b1) == 1;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(width);
        buffer.writeVarInt(height);
        buffer.writeLong(data);
        buffer.writeBoolean(empty);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other instanceof ArtisanPattern p) {
            final long mask = (1L << (width * height)) - 1;
            return width == p.width && height == p.height && empty == p.empty && (data & mask) == (p.data & mask);
        }
        return false;
    }

    public boolean matches(ArtisanPattern other) {
        for (int dx = 0; dx <= this.width - other.width; dx++) {
            for (int dy = 0; dy <= this.height - other.height; dy++) {
                if (matches(other, dx, dy, false) || matches(other, dx, dy, true)) {
                    return true;
                }
            }
        }
        //System.out.println("no match");
        return false;
    }

    private boolean matches(ArtisanPattern other, int startX, int startY, boolean mirror) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                int patternIdx = y * width + x;
                if (x < startX || y < startY || x - startX >= other.width || y - startY >= other.height) {
                    // If the current position in the matrix is outside the pattern, the value should be set by other.empty
                    if (get(patternIdx) != other.empty) {
                        return false;
                    }
                } else {
                    int otherIdx;
                    if (mirror) {
                        otherIdx = (y - startY) * other.width + (other.width - 1 - (x - startX));
                    } else {
                        otherIdx = (y - startY) * other.width + (x - startX);
                    }

                    if (get(patternIdx) != other.get(otherIdx)) {
                        return false;
                    }
                }
            }
        }
        //System.out.println("Pattern Match");
        return true;
    }
}
