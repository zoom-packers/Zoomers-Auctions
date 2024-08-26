package com.epherical.auctionworld;

import com.epherical.auctionworld.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerWallet {
    public List<WalletEntry> walletEntries = new ArrayList<>();

    public record WalletEntry(String currency, int available, int inAuctions) {
        public WalletEntry(String currency, int available, int inAuctions) {
            this.currency = currency;
            this.available = available;
            this.inAuctions = inAuctions;
        }

        public String getCurrencyLabel() {
            return Config.getAlias(currency);
        }

        public ItemStack getCurrencyItemStack() {
            return new ItemStack(Config.getCurrencyItem(currency));
        }
    }

    public void update(PlayerWallet wallet) {
        walletEntries = wallet.walletEntries;
    }

    public void networkSerialize(FriendlyByteBuf buf) {
        buf.writeInt(walletEntries.size());
        for (WalletEntry entry : walletEntries) {
            buf.writeUtf(entry.currency);
            buf.writeInt(entry.available);
            buf.writeInt(entry.inAuctions);
        }
    }

    public static PlayerWallet networkDeserialize(FriendlyByteBuf buf) {
        PlayerWallet wallet = new PlayerWallet();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            wallet.walletEntries.add(new WalletEntry(buf.readUtf(), buf.readInt(), buf.readInt()));
        }
        return wallet;
    }
}
