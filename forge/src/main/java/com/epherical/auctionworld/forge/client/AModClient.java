package com.epherical.auctionworld.forge.client;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.Constants;
import com.epherical.auctionworld.client.screen.BrowseAuctionScreen;
import com.epherical.auctionworld.client.screen.CreateAuctionScreen;
import com.epherical.auctionworld.client.tooltip.BiddingTooltipClientComponent;
import com.epherical.auctionworld.registry.Registry;
import com.epherical.auctionworld.object.AuctionItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AModClient {

    public static void initClient() {

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
    }

    @SubscribeEvent
    public static void tooltipRegister(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AuctionItem.class, BiddingTooltipClientComponent::new);
    }
}
