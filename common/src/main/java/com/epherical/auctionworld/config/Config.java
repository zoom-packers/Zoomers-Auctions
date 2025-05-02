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
    public Integer[] startingCurrencies = new Integer[]{0, 0, 0};
    public boolean disableOpenGuiCommandForNonAdmins = true;
    public int listingFee = -1;
    public int addTimeAfterBid = -1;
    public boolean dotcoinModIntegration = true;

    public static Item getCurrencyItem(String currency) {
        var resourceLocation = new ResourceLocation(currency);
        return BuiltInRegistries.ITEM.get(resourceLocation);
    }

    public static String getCurrencyForItem(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static String getAlias(String currency) {
        var item = getCurrencyItem(currency);
        return item.getDescription().getString();
    }
}
