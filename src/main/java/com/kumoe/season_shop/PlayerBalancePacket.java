package com.kumoe.season_shop;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerBalancePacket {
    static SeasonShopPlugin plugin = SeasonShopPlugin.getInstance();
    private final UUID uuid;
    private double balance;

    public PlayerBalancePacket(UUID uuid, double balance) {
        this.uuid = uuid;
        this.balance = balance;
    }

    public static void handel(byte @NotNull [] message) {

        var playerBalancePacket = read(message);
        if (playerBalancePacket != null) {
            plugin.log("cached balance: " + playerBalancePacket.balance);
            plugin.log("player uuid: " + playerBalancePacket.uuid);
            var player = Bukkit.getPlayer(playerBalancePacket.uuid);
            if (player != null) {
                double balance = SeasonShopPlugin.getEcon().getBalance(player);
                playerBalancePacket.setBalance(balance);
                playerBalancePacket.sendToPlayer(player);
            }
        }else {
            plugin.log("player balance packet is null");
        }
    }
    void setBalance(double balance) {
        this.balance = balance;
    }

    @Nullable
    public static PlayerBalancePacket read(byte[] message) {
        ByteBuf buf = Unpooled.wrappedBuffer(message);
        // read a custom version
        short version = buf.readUnsignedByte();
        plugin.log("version " + version);
        // must mach version
        if (version == 2) {
            UUID uuid = ByteBufHelper.readUuid(buf);
            double balance = buf.readDouble();
            return new PlayerBalancePacket(uuid, balance);
        }
        return null;
    }

    public void sendToPlayer(@NotNull Player player) {
        // calculate buffer pool size
        int bufferSize = 1 // 包类型
                + Long.BYTES + Long.BYTES // uuid 占用
                + Double.BYTES;
        var buf = Unpooled.buffer(bufferSize);
        // 写入id让客户端mod 能够获取到对应的packet
        buf.writeByte(2);
        ByteBufHelper.writeUuid(buf, uuid);
        buf.writeDouble(balance);
        player.sendPluginMessage(SeasonShopPlugin.getInstance(), SeasonShopPlugin.channel, buf.array());
    }

    public UUID getUUID() {
        return uuid;
    }

    PlayerBalancePacket decode(ByteBuf buf) {
        return new PlayerBalancePacket(ByteBufHelper.readUuid(buf), buf.readDouble());
    }
}
