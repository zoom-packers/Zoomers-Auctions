package com.epherical.auctionworld.fabric.client;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.screen.BrowseAuctionScreen;
import com.epherical.auctionworld.client.screen.CreateAuctionScreen;
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
        MenuScreens.register(Registry.BROWSE_AUCTION_MENU, BrowseAuctionScreen::new);
        MenuScreens.register(Registry.CREATE_AUCTION_MENU, CreateAuctionScreen::new);

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
