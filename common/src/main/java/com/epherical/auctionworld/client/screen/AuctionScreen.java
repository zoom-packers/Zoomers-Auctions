package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.PlayerWallet;
import com.epherical.auctionworld.WalletEntry;
import com.epherical.auctionworld.client.widgets.AuctionBase;
import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

public abstract class AuctionScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    protected AuctionBase auctionMenuBase;
    protected AuctionMenuWidget auctionMenuWidget;

    public AuctionScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        auctionMenuBase = new AuctionBase(inventory, title);
        auctionMenuWidget = new AuctionMenuWidget(inventory, title);
        imageWidth = 512;
        imageHeight = 480;
    }


    @Override
    protected void init() {
        super.init();
//        auctionMenuWidget.init(this, leftPos, topPos, font);
    }

    public void addRenderableWidgetExternal(AbstractWidget button) {
        this.addRenderableWidget(button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int x, int y) {
        auctionMenuBase.renderBg(graphics, leftPos, height, delta, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        if (leftPos >= 0 && topPos >= 0) {
            this.renderBackground(graphics);
            super.render(graphics, x, y, delta);
            this.drawTitle(graphics, leftPos + 10, topPos + 10);
            this.drawWallet(graphics, leftPos, topPos);
        } else {
            this.renderBackground(graphics);
            graphics.drawString(font, "Decrease your GUI scale to see the entire menu!",  50, 60, 0xFFFFFF);
        }
    }

    private void drawWallet(GuiGraphics graphics, int x, int y) {
        var wallet = AuctionTheWorldAbstract.getInstance().getPlayerWallet();
        var topOffset = 356;
        var leftOffset = 10;
        List<WalletEntry> walletEntries = wallet.walletEntries;
        if (walletEntries.isEmpty()) {
            return;
        }
        for (int i = 0; i < walletEntries.size(); i++) {
            var walletEntry = walletEntries.get(i);
            var currency = walletEntry.getCurrency();
            var currencyLabel = walletEntry.getCurrencyLabel();
            var available = walletEntry.getAvailable();
            var inAuctions = walletEntry.getInAuctions();
            var currencyItemStack = walletEntry.getCurrencyItemStack();
            var finalY = y + topOffset + (i * 20);
            var finalX = x + leftOffset;

            graphics.renderFakeItem(currencyItemStack, finalX, finalY);
            var finalComponent = Component.literal(currencyLabel + ": " + available + " in wallet, " + inAuctions + " in auctions");
            graphics.drawString(font, finalComponent, finalX + 20, finalY + 5, 0xFFFFFF);
        }
    }

    private void drawTitle(GuiGraphics graphics, int x, int y) {
        Font font = Minecraft.getInstance().font;
        var formattedTitle = title.getString().toUpperCase().charAt(0) + title.getString().substring(1).toLowerCase().replace("_", " ");
        graphics.drawString(font, formattedTitle, x, y, 0xFFFFFF);
    }

}
