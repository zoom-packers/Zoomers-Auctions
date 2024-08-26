package com.epherical.auctionworld.client;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.screen.BrowseAuctionScreen;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.networking.UserSubmitBid;
import com.epherical.auctionworld.networking.UserSubmitBuyout;
import com.epherical.auctionworld.object.AuctionItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AuctionListWidget extends ContainerObjectSelectionList<AuctionListWidget.Entry> {
    private final EditBox bidAmt;

    private final Button bidButton;

    private final Button buyoutButton;
    private boolean tooltipActive = false;


    private final BrowseAuctionScreen screen;

    public AuctionListWidget(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight, BrowseAuctionScreen screen) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.screen = screen;
        this.bidAmt = new EditBox(minecraft.font, -100, -100, 70, 20, Component.translatable("Bid Amount"));
        this.bidButton = Button.builder(Component.translatable("Bid"), pButton -> {
            Entry selected = getSelected();
            if (selected != null) {
                int bid = 0;
                try {
                    bid = Integer.parseInt(bidAmt.getValue());
                } catch (NumberFormatException ignored) {
                }
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new UserSubmitBid(selected.item.getAuctionID(), bid));
            }
        }).width(32).build();
        bidAmt.setFilter(s -> s.matches("[0-9]+") || s.isEmpty());
        this.buyoutButton = Button.builder(Component.translatable("Purchase"), pButton -> {
            Entry selected = getSelected();
            if (selected != null) {
                try {
                    AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new UserSubmitBuyout(selected.item.getAuctionID()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).width(120).build();
    }

    public void reset() {
        tooltipActive = false;
        setSelected(null);
    }

    public void tick() {
        bidAmt.tick();
    }

    public void addEntries(Collection<AuctionItem> items) {
        for (AuctionItem item : items) {
            Entry entry = new Entry(item);
            addEntry(entry);
        }
    }


    @Override
    public int getRowWidth() {
        return 384;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 190;
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft();
    }

    @Override
    public int addEntry(Entry entry) {
        return super.addEntry(entry);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (bidButton.mouseClicked(pMouseX, pMouseY, pButton) || buyoutButton.mouseClicked(pMouseX, pMouseY, pButton) || bidAmt.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        return bidAmt.charTyped(pCodePoint, pModifiers) || super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return bidAmt.keyPressed(pKeyCode, pScanCode, pModifiers) || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }


    public class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private static final List<Component> COMPONENTS = List.of(Component.translatable("Bidding"));


        private AuctionItem item;

        private double clickedX;
        private double clickedY;


        public Entry(AuctionItem item) {
            this.item = item;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }


        @Override
        public void render(GuiGraphics graphics, int row, int top, int left, int width, int height, int x, int y, boolean hovered, float delta) {
            //left -= 246;
            Font font = AuctionListWidget.this.minecraft.font;
            ItemStack itemStack = item.getAuctionItems().get(0);
            var itemCurrency = item.getCurrency();
            var itemCurrencyItem = Config.getCurrencyItem(itemCurrency);
            ItemStack currencyStack = new ItemStack(itemCurrencyItem, 1);
            graphics.renderFakeItem(itemStack, left, top + 4);
            graphics.drawString(font, item.formatTimeLeft(), left + 120, top + 8, 0xFFFFFF, false);

            int width1 = font.width(itemStack.getHoverName());
            if (width1 >= 95) {
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.translate((left + 24), (top + 8), 1f);
                //poseStack.translate((1/scale), (1/scale), 1f);
                graphics.drawString(font, itemStack.getHoverName(), left + 24, top + 8, 0xFFFFFF, false);
                poseStack.scale(2f, 2f, 2f);
                poseStack.popPose();
            } else {
                graphics.drawString(font, itemStack.getHoverName(), left + 24, top + 8, 0xFFFFFF, false);
            }

            graphics.drawString(font, item.getSeller(), left + 220, top + 8, 0xFFFFFF, false);
            graphics.renderFakeItem(currencyStack, left + 316, top + 6);
            graphics.drawString(font, String.valueOf(item.getCurrentBidPrice()), left + 334, top + 2, 0xFFFFFF, false);
            graphics.drawString(font, String.valueOf(item.getBuyoutPrice()), left + 334, top + 15, 0xFFFFFF, false);


            PoseStack pose = graphics.pose();
            if (hovered) {
                pose.pushPose();
                pose.translate(0f, 10f, 800f);
                if (x > left && x < left + 115) {
                    List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, itemStack);
                    graphics.renderTooltip(font, tooltipFromItem, itemStack.getTooltipImage(), x, y);
                } else if (x > left + 316 && x < left + 328) {
                    // this is for the currency tooltip
                    graphics.renderTooltip(font, Component.translatable("Currency"), x, y);
                } else if (x > left + 328 && y < top + 8) {
                    graphics.renderTooltip(font, Component.translatable("Current Bidding Price"), x, y);
                } else if (x > left + 328 && y > top + 8 && y < top + 25) {
                    graphics.renderTooltip(font, Component.translatable("Buyout Price"), x, y);
                }
                pose.popPose();
            }

            if (this.equals(getSelected())) {

                screen.setTooltipEntry((font1, graphics1, mouseX, mouseY, delta1) -> {
                    graphics1.pose().pushPose();
                    graphics1.pose().translate(0, 0, 1f);
                    graphics1.pose().pushPose();
                    graphics1.pose().translate(0, 0, 532f);
                    bidButton.setX((int) (clickedX + 84));
                    bidButton.setY((int) (clickedY + 40));
                    bidButton.render(graphics1, mouseX, mouseY, delta1);

                    buyoutButton.setX((int) (clickedX + 12));
                    buyoutButton.setY((int) (clickedY + 70));
                    buyoutButton.render(graphics1, mouseX, mouseY, delta1);

                    bidAmt.setX((int) (clickedX + 12));
                    bidAmt.setY((int) (clickedY + 40));
                    bidAmt.render(graphics1, mouseX, mouseY, delta1);
                    graphics1.pose().translate(0, 0, -532f);
                    graphics1.pose().popPose();
                    graphics1.renderTooltip(font1, COMPONENTS, Optional.of((item)), (int) clickedX, (int) clickedY);
                    graphics1.pose().translate(0, 0, -1f);
                    graphics1.pose().popPose();
                });
                tooltipActive = true;
            } else if (!tooltipActive) {
                bidButton.setX(-100);
                bidButton.setY(-100);

                bidAmt.setX(-100);
                bidAmt.setY(-100);

                buyoutButton.setX(-100);
                buyoutButton.setY(-100);
            }


            pose = graphics.pose();
            pose.pushPose();
            pose.scale(0.5f, 0.5f, 0.5f);
            pose.translate(left + 308, top + 15, 0f);
            pose.scale(2f, 2f, 2f);
            graphics.pose().popPose();
        }

        @Override
        public void renderBack(GuiGraphics graphics, int row, int top, int left, int width, int height, int x, int y, boolean hovered, float delta) {
            super.renderBack(graphics, row, top, left, width, height, x, y, hovered, delta);
            if ((hovered && !tooltipActive) || this.equals(getSelected())) {
                graphics.fill(left, top, left + width - 3, top + 24, 0xff215581);
            } else {
                graphics.fill(left, top, left + width - 3, top + 24, 0xff42a4f5);
            }
            //System.out.println(hovered1);

        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            boolean b = super.mouseClicked(pMouseX, pMouseY, pButton);
            if (this.equals(getSelected())) {
                setSelected(null);
                tooltipActive = false;
                screen.setTooltipEntry(null);
            } else {
                setSelected(this);
                clickedX = pMouseX;
                clickedY = pMouseY;
                bidAmt.setFocused(true);
            }
            return b;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }
    }
}
