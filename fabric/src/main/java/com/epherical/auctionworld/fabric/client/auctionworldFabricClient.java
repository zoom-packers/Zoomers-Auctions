package com.epherical.auctionworld.fabric.client;

import com.epherical.auctionworld.AuctionManager;
import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.screen.*;
import com.epherical.auctionworld.client.tooltip.BiddingTooltipClientComponent;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.auctionworld.registry.Registry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;

public final class auctionworldFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AuctionTheWorldAbstract.client = true;
        AuctionTheWorldAbstract.clientAuctionManager = new AuctionManager(null, true, null);

        MenuScreens.register(Registry.BROWSE_AUCTION_MENU, BrowseAuctionScreen::new);
        MenuScreens.register(Registry.CREATE_AUCTION_MENU, CreateAuctionScreen::new);
        MenuScreens.register(Registry.WALLET_MANAGEMENT_MENU, WalletManagementScreen::new);
        MenuScreens.register(Registry.AUCTION_PARTICIPATION_MENU, AuctionParticipationScreen::new);
        MenuScreens.register(Registry.AUCTION_OWNER_MENU, AuctionOwnerScreen::new);

        AuctionTheWorldAbstract.auctionListeners.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> {
                if (minecraft.screen instanceof BrowseAuctionScreen screen) {
                    screen.reset();
                }
            });
        });

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AuctionItem item) {
                return new BiddingTooltipClientComponent(item);
            }
            return null;
        });
    }
}
