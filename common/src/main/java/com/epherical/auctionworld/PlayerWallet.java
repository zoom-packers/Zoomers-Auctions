package com.epherical.auctionworld;

import com.epherical.auctionworld.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerWallet {
    public List<WalletEntry> walletEntries = new ArrayList<>();

    public PlayerWallet() {
        for (String currency : Config.INSTANCE.currencies) {
            walletEntries.add(new WalletEntry(currency, 0, 0));
        }
    }

    public class WalletEntry {
        private String currency;
        private int available;
        private int inAuctions;

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

    public void update(PlayerWallet wallet) {
        for (WalletEntry entry : wallet.walletEntries) {
            WalletEntry current = walletEntries.stream().filter(e -> e.currency.equals(entry.currency)).findFirst().orElse(null);
            if (current != null) {
                current.available = entry.available;
                current.inAuctions = entry.inAuctions;
            }
        }
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
            var currency = buf.readUtf();
            var available = buf.readInt();
            var inAuctions = buf.readInt();
            var entry = wallet.walletEntries.stream().filter(e -> e.currency.equals(currency)).findFirst().orElse(null);
            if (entry != null) {
                entry.available = available;
                entry.inAuctions = inAuctions;
            }
        }
        return wallet;
    }
}
