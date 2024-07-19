package com.kumoe.season_shop;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PricePacket {
    private final UUID playerUUID;
    private final double price;
    private final long blockPos;
    static SeasonShopPlugin plugin = SeasonShopPlugin.getInstance();

    public PricePacket(UUID playerUUID, double price, long blockPos) {
        this.playerUUID = playerUUID;
        this.price = price;
        this.blockPos = blockPos;
    }

    public static void handel(byte @NotNull [] message) {

        PricePacket pricePacket = read(message);

        if (pricePacket != null) {
            plugin.log("player id " + pricePacket.getPlayerUUID());
            plugin.log("item price " + pricePacket.getPrice());
            plugin.log("block pos " + pricePacket.getBlockPos());
            Player player = Bukkit.getPlayer(pricePacket.playerUUID);
            if (player != null) {
                SeasonShopPlugin.getEcon().depositPlayer(player, pricePacket.getPrice());
                double balance = SeasonShopPlugin.getEcon().getBalance(player);
                player.sendMessage("成功售卖商品，您当前剩余：" + balance);
            }
        }else {
            plugin.log("price packet error!");
        }
    }

    private static PricePacket read(byte[] array) {
        ByteBuf buf = Unpooled.wrappedBuffer(array);
        // read a custom version
        short version = buf.readUnsignedByte();
        plugin.log("version " + version);
        try {
            // must mach version
            if (version == 0) {
                UUID uuid = readUuid(buf);
                double price = buf.readDouble();
                long blockPos = buf.readLong();
                return new PricePacket(uuid, price, blockPos);
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown packet version " + version);
        }
        return null;
    }

    public static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public double getPrice() {
        return price;
    }

    public long getBlockPos() {
        return blockPos;
    }

    public void send(Player player) {
        ByteBuf buf = Unpooled.buffer(1 + Long.BYTES + Long.BYTES + 8 + Long.BYTES); // calculate buffer pool size
        buf.writeByte(1); // 1
        buf.writeLong(player.getUniqueId().getMostSignificantBits()); // 8 byte long
        buf.writeLong(player.getUniqueId().getLeastSignificantBits()); // 8
        buf.writeDouble(price); // 8
        buf.writeLong(blockPos); // 8
        player.sendPluginMessage(SeasonShopPlugin.getInstance(), SeasonShopPlugin.channel, buf.array());
    }
}
