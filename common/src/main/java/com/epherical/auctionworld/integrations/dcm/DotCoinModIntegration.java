package com.epherical.auctionworld.integrations.dcm;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.config.Config;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import uk.co.dotcode.coin.entity.PlayerEntityExtension;
import uk.co.dotcode.coin.item.BasicCoin;
import uk.co.dotcode.coin.item.ModItems;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DotCoinModIntegration {
    public static boolean enabled = false;
    private static final Map<String, Integer> dotcoinMappings = Map.ofEntries(
            new HashMap.SimpleEntry<>("copper", 0),
            new HashMap.SimpleEntry<>("iron", 1),
            new HashMap.SimpleEntry<>("gold", 2),
            new HashMap.SimpleEntry<>("platinum", 3),
            new HashMap.SimpleEntry<>("token", 4),
            new HashMap.SimpleEntry<>("tin", 5),
            new HashMap.SimpleEntry<>("nickel", 6),
            new HashMap.SimpleEntry<>("silver", 7),
            new HashMap.SimpleEntry<>("steel", 8),
            new HashMap.SimpleEntry<>("bronze", 9),
            new HashMap.SimpleEntry<>("brass", 10),
            new HashMap.SimpleEntry<>("osmium", 11),
            new HashMap.SimpleEntry<>("diamond", 12),
            new HashMap.SimpleEntry<>("emerald", 13),
            new HashMap.SimpleEntry<>("ruby", 14),
            new HashMap.SimpleEntry<>("sapphire", 15),
            new HashMap.SimpleEntry<>("topaz", 16)
    );

    public static void initDotcoinModIntegration() {
        if (Config.INSTANCE.dotcoinModIntegration) {
            changeCurrenciesToDotcoin();
            enabled = true;
        }
    }

    private static void changeCurrenciesToDotcoin() {
        LifecycleEvent.SETUP.register(() -> {
            Config.INSTANCE.currencies = Arrays.stream(ModItems.coinConfigOrder).map(coin -> "dotcoinmod:" + coin + "_coin").toArray(String[]::new);
        });
    }

    public static int getCurrencyAmount(ServerPlayer player, String currency) {
        var coinName = currency.replace("dotcoinmod:", "").replace("_coin", "");
        var slot = dotcoinMappings.get(coinName);
        if (slot == null) return 0;
        var dotcoinPlayer = (PlayerEntityExtension) player;
        var coinData = dotcoinPlayer.getCoinData();
        return coinData.getValue(slot);
    }

    public static void removeCurrency(ServerPlayer player, String currency, int amountToTake) {
        var coinName = currency.replace("dotcoinmod:", "").replace("_coin", "");
        var slot = dotcoinMappings.get(coinName);
        if (slot == null) return;
        var dotcoinPlayer = (PlayerEntityExtension) player;
        var coinData = dotcoinPlayer.getCoinData();
        var currentValue = coinData.getValue(slot);
        coinData.setValue(slot, currentValue - amountToTake);

        coinData.sendChangedData(player);
        player.inventoryMenu.broadcastChanges();
    }

    public static void addCurrency(ServerPlayer player, String currency, int amountToAdd) {
        var coinName = currency.replace("dotcoinmod:", "").replace("_coin", "");
        var slot = dotcoinMappings.get(coinName);
        if (slot == null) return;
        var dotcoinPlayer = (PlayerEntityExtension) player;
        var coinData = dotcoinPlayer.getCoinData();
        coinData.addToTotal(slot, amountToAdd);

        coinData.sendChangedData(player);
        player.inventoryMenu.broadcastChanges();
    }
}
