package com.epherical.auctionworld;

import com.epherical.auctionworld.config.Config;
import net.minecraft.world.item.ItemStack;

public class WalletEntry {
    public String currency;
    public int available;
    public int inAuctions;

    public WalletEntry(String currency, int available, int inAuctions) {
        this.currency = currency;
        this.available = available;
        this.inAuctions = inAuctions;
    }

    public String getCurrencyLabel() {
        return getCurrencyItemStack().getItem().getDescription().getString();
    }

    public ItemStack getCurrencyItemStack() {
        return new ItemStack(Config.getCurrencyItem(currency));
    }

    public String getCurrency() {
        return currency;
    }

    public int getAvailable() {
        return available;
    }

    public int getInAuctions() {
        return inAuctions;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public void setInAuctions(int inAuctions) {
        this.inAuctions = inAuctions;
    }
}
