package com.kumoe.season_shop;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PricePacket {
    private static final Logger log = LoggerFactory.getLogger(PricePacket.class);
    static SeasonShopPlugin plugin = SeasonShopPlugin.getInstance();
    private final UUID playerUUID;
    private final int slotCount;
    private final long blockPos;
    private final List<SlotData> slotDataList;

    public PricePacket(UUID playerUUID, int slotCount, List<SlotData> slotDataList, long blockPos) {
        this.playerUUID = playerUUID;
        this.slotCount = slotCount;
        this.slotDataList = slotDataList;
        this.blockPos = blockPos;
    }

    public static void handel(byte @NotNull [] message) {

        PricePacket pricePacket = read(message);

        if (pricePacket != null) {
            plugin.log("player id " + pricePacket.getPlayerUUID());
            plugin.log("item price " + pricePacket.getPriceByCoinType());
            for (SlotData slotData : pricePacket.slotDataList) {
                plugin.log("SlotData type " + slotData.type);
                plugin.log("SlotData count " + slotData.count);
            }
            var totalPrice = getTotalPriceBySlotDataList(pricePacket.slotDataList);
            plugin.log("total price" + totalPrice);
            plugin.log("block pos " + pricePacket.getBlockPos());
            Player player = Bukkit.getPlayer(pricePacket.playerUUID);
            if (player != null) {
                SeasonShopPlugin.depositPlayer(player, totalPrice);
                double balance = SeasonShopPlugin.getEcon().getBalance(player);
                player.sendMessage("成功售卖商品，您当前剩余：" + balance);
            }
        }
    }

    private static PricePacket read(byte[] array) {
        ByteBuf buf = Unpooled.wrappedBuffer(array);
        // read a custom version
        short version = buf.readUnsignedByte();
        plugin.log("version " + version);
        if (version == 0) {
            UUID uuid = ByteBufHelper.readUuid(buf);
            int slotCount = buf.readInt();
            List<SlotData> slotDataList = new ArrayList<>();
            for (int i = 0; i < slotCount; i++) {
                slotDataList.add(SlotData.decode(buf));
            }
            long blockPos = buf.readLong();
            return new PricePacket(uuid, slotCount, slotDataList, blockPos);
        } else {
            plugin.log("Unknown packet version " + version);
        }
        return null;
    }


    private static double getPriceByCoinType(CoinType coinType) {
        return switch (coinType) {
            case GOLD -> 100d;
            case COPPER -> 1d;
            case SILVER -> 10d;
        };
    }

    private static double getTotalPriceBySlotDataList(List<SlotData> slotDataList) {
        var totalPrice = 0d;
        for (SlotData slotData : slotDataList) {
            totalPrice += getPriceByCoinType(slotData.type) * slotData.count;
        }
        return totalPrice;
    }

    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    private double getPriceByCoinType() {
        return 0;
    }

    public long getBlockPos() {
        return blockPos;
    }

    public void send(Player player) {
        // calculate buffer pool size
        int bufferSize = 1 // 包类型
                + Long.BYTES + Long.BYTES // uuid 占用
                + Integer.BYTES;
        // 计算动态部分大小
        bufferSize += slotDataList.size() * (Integer.BYTES + Integer.BYTES); // slot and count size

        ByteBuf buf = Unpooled.buffer(bufferSize);
        buf.writeByte(1); // 1
        ByteBufHelper.writeUuid(buf, player.getUniqueId());
        buf.writeInt(slotDataList.size()); // Number of slots
        for (SlotData slotData : slotDataList) {
            slotData.encode(buf);
        }
        player.sendPluginMessage(SeasonShopPlugin.getInstance(), SeasonShopPlugin.channel, buf.array());
    }

    public static class SlotData {
        public CoinType type;
        public int count;

        SlotData(CoinType type, int count) {
            this.type = type;
            this.count = count;
        }

        public static SlotData decode(ByteBuf buf) {
            return new SlotData(ByteBufHelper.readEnum(buf, CoinType.class), buf.readInt());
        }

        public void encode(ByteBuf buf) {
            buf.writeInt(type.ordinal());
            buf.writeInt(count);
        }
    }
}
