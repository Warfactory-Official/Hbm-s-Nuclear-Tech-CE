package com.hbm.util;

import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.world.phased.PhasedStructureRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.internal.MathUtil;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BufferUtil {
    private static final int DEFAULT_MAX_STRING_BYTES = 32767;

    public static void writeVarInt(ByteBuf out, int value) {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    public static int readVarInt(ByteBuf in) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = in.readByte();
            int value = (read & 0x7F);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) {
                throw new IllegalArgumentException("VarInt too big");
            }
        } while ((read & 0x80) != 0);
        return result;
    }

    public static void writeZigZagVarInt(ByteBuf out, int value) {
        writeVarInt(out, (value << 1) ^ (value >> 31));
    }

    public static int readZigZagVarInt(ByteBuf in) {
        int encoded = readVarInt(in);
        return (encoded >>> 1) ^ -(encoded & 1);
    }

    public static void writeString(ByteBuf out, String s) {
        writeVarInt(out, utf8Bytes(s));
        ByteBufUtil.writeUtf8(out, s);
    }

    public static String readString(ByteBuf in) {
        return readString(in, DEFAULT_MAX_STRING_BYTES);
    }

    public static String readString(ByteBuf in, int maxBytes) {
        int len = readVarInt(in);
        if (len < 0 || len > maxBytes) {
            throw new IllegalArgumentException("String length out of bounds: " + len);
        }
        if (in.readableBytes() < len) {
            throw new IllegalArgumentException("String underflow: need " + len + " bytes");
        }
        return in.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }

    /**
     * Writes an integer array to a buffer.
     */
    public static void writeIntArray(ByteBuf buf, int @NotNull [] array) {
        buf.writeInt(array.length);
        for (int value : array) {
            buf.writeInt(value);
        }
    }

    /**
     * Reads an integer array from a buffer.
     */
    public static int @NotNull [] readIntArray(ByteBuf buf) {
        int length = buf.readInt();
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = buf.readInt();
        }
        return array;
    }

    /**
     * Writes a vector to a buffer.
     */
    public static void writeVec3(ByteBuf buf, @Nullable Vec3d vector) {
        buf.writeBoolean(vector != null);
        if (vector == null) return;
        buf.writeDouble(vector.x);
        buf.writeDouble(vector.y);
        buf.writeDouble(vector.z);
    }

    /**
     * Reads a vector from a buffer.
     */
    public static @Nullable Vec3d readVec3(ByteBuf buf) {
        boolean vectorExists = buf.readBoolean();
        if (!vectorExists) {
            return null;
        }
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();

        return new Vec3d(x, y, z);
    }

    /**
     * Writes a NBTTagCompound to a buffer.
     */
    public static void writeNBT(ByteBuf buf, @Nullable NBTTagCompound compound) {
        if (compound == null) {
            buf.writeShort(-1);
            return;
        }
        try (ByteBufOutputStream out = new ByteBufOutputStream(buf)) {
            int lengthWriterIndex = buf.writerIndex();
            buf.writeShort(0);
            int beforeDataIndex = buf.writerIndex();
            CompressedStreamTools.writeCompressed(compound, out);
            int afterDataIndex = buf.writerIndex();
            int length = afterDataIndex - beforeDataIndex;
            buf.setShort(lengthWriterIndex, (short) length);
        } catch (IOException e) {
            MainRegistry.logger.error("Failed to write NBTTagCompound to buffer", e);
            buf.writeShort(-1);
        }
    }

    /**
     * Reads a NBTTagCompound from a buffer.
     */
    public static NBTTagCompound readNBT(ByteBuf buf) {
        final short nbtLength = buf.readShort();
        if (nbtLength <= 0) return new NBTTagCompound();
        if (buf.readableBytes() < nbtLength) {
            MainRegistry.logger.error("Invalid NBT length: {} (readable={})", nbtLength, buf.readableBytes());
            return new NBTTagCompound();
        }
        ByteBuf nbtSlice = buf.readSlice(nbtLength);
        try (ByteBufInputStream in = new ByteBufInputStream(nbtSlice)) {
            return CompressedStreamTools.readCompressed(in);
        } catch (IOException e) {
            MainRegistry.logger.error("Failed to read NBTTagCompound from buffer", e);
            return new NBTTagCompound();
        }
    }

    /**
     * Writes an ItemStack to a buffer.
     */
    public static void writeItemStack(ByteBuf buf, @Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            buf.writeShort(-1);
        } else {
            buf.writeShort(Item.getIdFromItem(item.getItem()));
            buf.writeByte(item.getCount());
            buf.writeShort(item.getItemDamage());
            NBTTagCompound nbtTagCompound = null;

            if (item.getItem().isDamageable() || item.getItem().getShareTag()) {
                nbtTagCompound = item.getTagCompound();
            }
            writeNBT(buf, nbtTagCompound);
        }
    }

    /**
     * Reads an ItemStack from a buffer.
     */
    public static ItemStack readItemStack(ByteBuf buf) {
        short id = buf.readShort();
        if (id < 0) return ItemStack.EMPTY;

        byte quantity = buf.readByte();
        short meta = buf.readShort();
        Item item = Item.getItemById(id);

        ItemStack itemStack = new ItemStack(item, quantity, meta);
        itemStack.setTagCompound(readNBT(buf));

        return itemStack;
    }

    /**
     * Writes a position relative to a chunk as a packed short (4-bit X, 8-bit Y, 4-bit Z).
     * Assumes relX and relZ are [0, 15] and y is [0, 255].
     */
    public static void writePosCompact(ByteBuf out, int relX, int y, int relZ) {
        int packed = ((relX & 0xF) << 12) | ((y & 0xFF) << 4) | (relZ & 0xF);
        out.writeShort(packed);
    }

    /**
     * Reads a packed short position and returns it as a long block pos key where:
     * - X is relX
     * - Y is y
     * - Z is relZ
     */
    public static long readPosCompact(ByteBuf in) {
        int packed = in.readUnsignedShort();
        int relX = (packed >> 12) & 0xF;
        int y = (packed >> 4) & 0xFF;
        int relZ = packed & 0xF;
        return Library.blockPosToLong(relX, y, relZ);
    }

    /**
     * Writes a string ID from the global string table.
     * DO NOT USE THIS FOR PACKETS!
     */
    public static void writeStringStable(ByteBuf out, String s) {
        writeVarInt(out, PhasedStructureRegistry.getStringId(s));
    }

    /**
     * Reads a string ID and looks it up in the global string table.
     * DO NOT USE THIS FOR PACKETS!
     */
    public static @Nullable String readStringStable(ByteBuf in) {
        return PhasedStructureRegistry.getString(readVarInt(in));
    }

    // copied some modern netty methods below, they don't exist on current version
    // ------------------------ Netty Copy Paste Start ------------------------
    //
    // * Copyright 2012 The Netty Project
    // *
    // * The Netty Project licenses this file to you under the Apache License,
    // * version 2.0 (the "License"); you may not use this file except in compliance
    // * with the License. You may obtain a copy of the License at:
    // *
    // *   https://www.apache.org/licenses/LICENSE-2.0
    // *
    // * Unless required by applicable law or agreed to in writing, software
    // * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    // * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    // * License for the specific language governing permissions and limitations
    // * under the License.
    // */

    /**
     * Returns the exact bytes length of UTF8 character sequence.
     * <p>
     * This method is producing the exact length according to {@link #writeUtf8(ByteBuf, CharSequence)}.
     */
    public static int utf8Bytes(final CharSequence seq) {
        return utf8ByteCount(seq, 0, seq.length());
    }

    /**
     * Equivalent to <code>{@link #utf8Bytes(CharSequence) utf8Bytes(seq.subSequence(start, end))}</code>
     * but avoids subsequence object allocation.
     * <p>
     * This method is producing the exact length according to {@link #writeUtf8(ByteBuf, CharSequence, int, int)}.
     */
    public static int utf8Bytes(final CharSequence seq, int start, int end) {
        return utf8ByteCount(checkCharSequenceBounds(seq, start, end), start, end);
    }

    private static int utf8ByteCount(final CharSequence seq, int start, int end) {
        if (seq instanceof AsciiString) {
            return end - start;
        }
        int i = start;
        // ASCII fast path
        while (i < end && seq.charAt(i) < 0x80) {
            ++i;
        }
        // !ASCII is packed in a separate method to let the ASCII case be smaller
        return i < end ? (i - start) + utf8BytesNonAscii(seq, i, end) : i - start;
    }

    private static int utf8BytesNonAscii(final CharSequence seq, final int start, final int end) {
        int encodedLength = 0;
        for (int i = start; i < end; i++) {
            final char c = seq.charAt(i);
            // making it 100% branchless isn't rewarding due to the many bit operations necessary!
            if (c < 0x800) {
                // branchless version of: (c <= 127 ? 0:1) + 1
                encodedLength += ((0x7f - c) >>> 31) + 1;
            } else if (Character.isSurrogate(c)) {
                if (!Character.isHighSurrogate(c)) {
                    encodedLength++;
                    // WRITE_UTF_UNKNOWN
                    continue;
                }
                // Surrogate Pair consumes 2 characters.
                if (++i == end) {
                    encodedLength++;
                    // WRITE_UTF_UNKNOWN
                    break;
                }
                if (!Character.isLowSurrogate(seq.charAt(i))) {
                    // WRITE_UTF_UNKNOWN + (Character.isHighSurrogate(c2) ? WRITE_UTF_UNKNOWN : c2)
                    encodedLength += 2;
                    continue;
                }
                // See https://www.unicode.org/versions/Unicode7.0.0/ch03.pdf#G2630.
                encodedLength += 4;
            } else {
                encodedLength += 3;
            }
        }
        return encodedLength;
    }

    private static CharSequence checkCharSequenceBounds(CharSequence seq, int start, int end) {
        if (MathUtil.isOutOfBounds(start, end - start, seq.length())) {
            throw new IndexOutOfBoundsException("expected: 0 <= start(" + start + ") <= end (" + end
                    + ") <= seq.length(" + seq.length() + ')');
        }
        return seq;
    }

    // ------------------------ Netty Copy Paste End -------------------------
}
