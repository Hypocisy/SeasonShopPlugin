package com.kumoe.season_shop;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class ByteBufHelper {
    public static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static <T extends Enum<T>> T readEnum(ByteBuf buf, Class<T> pEnumClass) {
        return (pEnumClass.getEnumConstants())[readVarInt(buf)];
    }

    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }
}
