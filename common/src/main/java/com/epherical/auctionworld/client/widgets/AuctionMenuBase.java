package com.epherical.auctionworld.client.widgets;

import com.epherical.auctionworld.client.screen.AuctionScreen;
import com.epherical.auctionworld.registry.Registry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AuctionMenuBase {
    private Inventory inventory;
    private Component title;

    int imageWidth;
    int imageHeight;


    private static final ResourceLocation AUCTION_LOCATION = Registry.id("textures/gui/container/auction.png");

    public AuctionMenuBase(Inventory inventory, Component title) {
        this.inventory = inventory;
        this.title = title;


        imageWidth = 512;
        imageHeight = 480;
    }

    public void init(AuctionScreen screen, int leftPos, int topPos, Font font) {

    }


    public void renderBg(GuiGraphics graphics, int leftPos, int height, float delta, int x, int y) {
        int left = leftPos;
        int center = (height - this.imageHeight) / 2;
        graphics.blit(AUCTION_LOCATION, left, center, 0, 0, this.imageWidth, this.imageHeight, 512, 512);
    }
}
