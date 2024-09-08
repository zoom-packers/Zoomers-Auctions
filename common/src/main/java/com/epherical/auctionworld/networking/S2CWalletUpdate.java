package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.PlayerWallet;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.client.Minecraft;

public record S2CWalletUpdate(PlayerWallet wallet) {

    public static void handle(S2CWalletUpdate wallet, AbstractNetworking.Context<?> context) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            var mod = AuctionTheWorldAbstract.getInstance();
            mod.setPlayerWallet(wallet.wallet());
        });
    }

}
