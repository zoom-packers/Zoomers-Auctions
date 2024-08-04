package com.epherical.auctionworld.client.tooltip;

import com.epherical.auctionworld.object.AuctionItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.List;

public class BiddingTooltipClientComponent implements ClientTooltipComponent {

    private AuctionItem item;

    public BiddingTooltipClientComponent(AuctionItem item) {
        this.item = item;
    }

    @Override
    public int getHeight() {
        return 90;
    }

    @Override
    public int getWidth(Font pFont) {
        return 120;
    }

    @Override
    public void renderText(Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
        ClientTooltipComponent.super.renderText(pFont, pMouseX, pMouseY, pMatrix, pBufferSource);
        List<FormattedCharSequence> split = pFont.split(Component.translatable("There are %s items in this auction. Send a bid, or outright purchase the item for %s.", item.getCountOfItems(), item.getBuyoutPrice()), 120);
        for (int i = 0; i < split.size(); i++) {
            pFont.drawInBatch(split.get(i), (float) pMouseX, (float) (pMouseY + (i * 9)), -1, true, pMatrix, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        }

    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        ClientTooltipComponent.super.renderImage(pFont, pX, pY, pGuiGraphics);
    }
}
