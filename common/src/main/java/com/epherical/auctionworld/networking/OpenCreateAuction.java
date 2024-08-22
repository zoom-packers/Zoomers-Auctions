package com.epherical.auctionworld.networking;

import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.menu.*;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public record OpenCreateAuction(AuctionMenuWidget.CurrentScreen currentScreen) {

    public static void handle(OpenCreateAuction auctions, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                switch (auctions.currentScreen()) {
                    case CREATE_AUCTION -> {
                        player.openMenu(new SimpleMenuProvider((id, inventory, player1) -> {
                            return new CreateAuctionMenu(id, inventory);
                        }, Component.translatable("CREATE_AUCTION")));
                    }
                    case BROWSE_AUCTIONS -> {
                        player.openMenu(new SimpleMenuProvider((id, inventory, player1) -> {
                            return new BrowseAuctionMenu(id, inventory);
                        }, Component.translatable("BROWSE_AUCTIONS")));
                    }
                    case WALLET_MANAGEMENT -> {
                        player.openMenu(new SimpleMenuProvider((id, inventory, player1) -> {
                            return new WalletManagementMenu(id, inventory);
                        }, Component.translatable("WALLET_MANAGEMENT")));
                    }
                    case AUCTION_PARTICIAPTION -> {
                        player.openMenu(new SimpleMenuProvider((id, inventory, player1) -> {
                            return new AuctionParticipationMenu(id, inventory);
                        }, Component.translatable("AUCTION_PARTICIAPTION")));
                    }
                    case AUCTION_OWNER -> {
                        player.openMenu(new SimpleMenuProvider((id, inventory, player1) -> {
                            return new AuctionOwnerMenu(id, inventory);
                        }, Component.translatable("AUCTION_OWNER")));
                    }
                }
            });
        }

    }
}
