package com.epherical.auctionworld.config;

import elocindev.necronomicon.api.config.v1.NecConfigAPI;
import elocindev.necronomicon.config.NecConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Temporary class to build out a config that we populate later.
 */
public class Config {

    @NecConfig
    public static Config INSTANCE;

    public static String getFile() {
        return NecConfigAPI.getFile("auctionworld.json5");
    }

    public String[] currencies = new String[]{"minecraft:diamond", "minecraft:emerald", "minecraft:netherite_ingot"};
    public String[] currencyAliases = new String[]{"Diamond", "Emerald", "Netherite Ingot"};
    public Integer[] startingCurrencies = new Integer[]{64, 64, 64};
    public boolean disableOpenGuiCommandForNonAdmins = true;
    public int listingFee = -1;
    public int addTimeAfterBid = -1;

    public static Item getCurrencyItem(String currency) {
        var resourceLocation = new ResourceLocation(currency);
        return BuiltInRegistries.ITEM.get(resourceLocation);
    }

    public static String getCurrencyForItem(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static String getAlias(String currency) {
        var index = -1;
        for (int i = 0; i < INSTANCE.currencies.length; i++) {
            if (INSTANCE.currencies[i].equals(currency)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return "Unknown";
        }
        return INSTANCE.currencyAliases[index];
    }
}
