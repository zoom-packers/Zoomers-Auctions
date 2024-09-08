package com.epherical.auctionworld;

import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class PlayerWallet {
    public List<WalletEntry> walletEntries = new ArrayList<>();

    public PlayerWallet() {
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
            wallet.walletEntries.add(new WalletEntry(currency, available, inAuctions));
        }
        return wallet;
    }
}

