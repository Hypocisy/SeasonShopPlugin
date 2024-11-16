package com.kumoe.season_shop;

import com.Zrips.CMI.CMI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class SeasonShopPlugin extends JavaPlugin implements Listener {
    public static final String channel = "atm_mod:main";
    private static Economy econ = null;

    public static SeasonShopPlugin getInstance() {
        return getPlugin(SeasonShopPlugin.class);
    }

    public static Economy getEcon() {
        return econ;
    }

    public static void depositPlayer(Player player, double price) {
        SeasonShopPlugin.getEcon().depositPlayer(player, price);
    }

    public static void withdrawPlayer(Player player, double price) {
        SeasonShopPlugin.getEcon().withdrawPlayer(player, price);
    }

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, channel, (channel, player, message) -> {
            PricePacket.handel(message);
            PlayerBalancePacket.handel(message);
        });
        getServer().getMessenger().registerOutgoingPluginChannel(this, channel);
        getServer().getPluginManager().registerEvents(this, this);

        // setupEconomy
        if (!setupEconomy()) {
            log("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void log(String message) {
        getLogger().info(message);
    }

    private boolean setupEconomy() {
        // cmi vault support
        if (getServer().getPluginManager().getPlugin("CMI") != null) {
            if (CMI.getInstance().getEconomyManager().isEnabled()) {
                econ = CMI.getInstance().getEconomyManager().getVaultManager().getVaultEconomy();
                log("Enabled CMI Vault!");
                return econ != null;
            }
        } else if (getServer().getPluginManager().getPlugin("Vault") != null) {
            // normal vault support
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                log("Enabled" + econ.getName() + " Vault!");
                return econ != null;
            }
        }

        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws IllegalAccessException, NoSuchMethodException {
        Player player = event.getPlayer();
        try {
            Class<? extends CommandSender> senderClass = player.getClass();
            Method addChannel = senderClass.getDeclaredMethod("addChannel", String.class);
            addChannel.setAccessible(true);
            addChannel.invoke(player, channel);
        } catch (InvocationTargetException | IllegalArgumentException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("SeasonShopPlugin has been disabled!");
        getServer().getPluginManager().disablePlugin(this);
    }
}
