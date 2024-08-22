package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.registry.Registry;
import com.epherical.auctionworld.menu.CreateAuctionMenu;
import com.epherical.auctionworld.menu.slot.SelectableSlot;
import com.epherical.auctionworld.networking.CreateAuctionListing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class CreateAuctionScreen extends AbstractContainerScreen<CreateAuctionMenu> {
    private static final ResourceLocation AUCTION_LOCATION = Registry.id("textures/gui/container/auction.png");


    private Button createAuction;
    private EditBox timeSelection;
    private Button days;
    private Button hours;
    //private EditBox bidIncrement;
    private EditBox startingBid;
    private EditBox buyoutPrice;
    private EditBox currencyBox;
    private boolean daysActive = false;
    private boolean hoursActive = false;



    public CreateAuctionScreen(CreateAuctionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        // buyout price
        // time selection - days button, hours button, textbox to input a time
        // min bid increment - editbox


        // item display to show all items in the listing
    }

    @Override
    protected void init() {
        imageWidth = 512;
        imageHeight = 512;
        super.init();

        PlainTextButton plainTextButton = new PlainTextButton(82 + leftPos, 249 + topPos, 80, 20, Component.translatable("Finish Auction"), button -> {
            validateAndSendToServer();
        }, font);
        plainTextButton.setTooltip(Tooltip.create(Component.literal("Don't forget to select the items in your inventory!")));

        createAuction = addRenderableWidget(plainTextButton);

        timeSelection = addRenderableWidget(new EditBox(font, 129 + leftPos, 44 + topPos, 100, 20, Component.translatable("Time Selection")));
        timeSelection.setFilter(s -> s.matches("[0-9]+") || s.isEmpty());
        timeSelection.setTooltip(Tooltip.create(Component.translatable("How much time until the auction expires. Max: 7D or 168H. (REQUIRED)")));

        days = addRenderableWidget(Button.builder(Component.literal("D"), button -> {
            daysActive = true;
            button.setFocused(true);
            hours.setFocused(false);
            hoursActive = false;
        }).pos(231 + leftPos, 44 + topPos).width(20).tooltip(Tooltip.create(Component.translatable("How many days until the auction expires"))).build());
        hours = addRenderableWidget(Button.builder(Component.literal("H"), button -> {
            hoursActive = true;
            button.setFocused(true);
            days.setFocused(false);
            daysActive = false;
        }).pos(263 + leftPos, 44 + topPos).width(20).tooltip(Tooltip.create(Component.translatable("How many hours until the auction expires"))).build());


        /*bidIncrement = addRenderableWidget(new EditBox(font, 126 + leftPos, 64 + topPos, 100, 20, Component.translatable("Bid Increment")));
        bidIncrement.setFilter(s -> s.matches("[0-9]+") || s.isEmpty());
        bidIncrement.setHint(Component.literal("Bid Increment"));
        bidIncrement.setTooltip(Tooltip.create(Component.translatable("Whenever a player bids, how much they have to bid at a minimum.")));*/


        startingBid = addRenderableWidget(new EditBox(font, 129 + leftPos, 84 + topPos, 100, 20, Component.translatable("Starting Bid")));
        startingBid.setFilter(s -> s.matches("[0-9]+") || s.isEmpty());
        startingBid.setTooltip(Tooltip.create(Component.translatable("What the starting bid will be. This is the minimum price someone must pay to receive the item (REQUIRED)")));
        buyoutPrice = addRenderableWidget(new EditBox(font, 129 + leftPos, 124 + topPos, 100, 20, Component.translatable("Buyout Price")));
        buyoutPrice.setFilter(s -> s.matches("[0-9]+") || s.isEmpty());
        buyoutPrice.setTooltip(Tooltip.create(Component.translatable("How much a user will have to pay to just straight up buy the item. Leave blank to not set one. (OPTIONAL)")));
        currencyBox = addRenderableWidget(new EditBox(font, 129 + leftPos, 164 + topPos, 100, 20, Component.translatable("Currency")));
        currencyBox.setFilter(s -> s.matches("[a-zA-Z]+") || s.isEmpty());
        currencyBox.setTooltip(Tooltip.create(Component.translatable("What currency the auction will be in. (REQUIRED)")));
    }


    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, x, y, delta);

        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, -500.0F);
        if (this.menu.getFirstSlot() != null) {
            for (Slot slot : this.menu.slots) {
                SelectableSlot select = (SelectableSlot) slot;
                int slotX = select.x;
                int slotY = select.y;
                if (select.isSelected()) {
                    renderSlotHighlight(graphics, slotX, slotY, 0);
                }
            }
            int slotX = menu.getFirstSlot().x;
            int slotY = menu.getFirstSlot().y;
            renderSlotHighlight(graphics, slotX, slotY, 0);
        }
        graphics.pose().popPose();

        this.renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int x, int y) {
        int left = this.leftPos;
        int center = (this.height - this.imageHeight) / 2;
        graphics.blit(AUCTION_LOCATION, left, center, 0, 0, this.imageWidth, this.imageHeight, 512, 512);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        //super.renderLabels(graphics, x, y);
    }

    private void validateAndSendToServer() {
        int time;
        if (daysActive) {
            String value = timeSelection.getValue();
            time = Integer.parseInt(value);
            if (time > 7) {
                // todo; maybe send error msg in screen
                return;
            }
            time *= 24;
        } else if (hoursActive) {
            String value = timeSelection.getValue();
            time = Integer.parseInt(value);
            if (time > 168) {
                // todo; maybe send error msg in screen
                return;
            }
        } else {
            // player didn't select the time... lets go default 24hrs
            time = 24;
        }

        if (startingBid.getValue().isEmpty()) {
            // todo; maybe send error msg in screen
            return;
        }
        int start = Integer.parseInt(startingBid.getValue());
        int buyout = -1;
        if (!buyoutPrice.getValue().isEmpty()) {
            buyout = Integer.parseInt(buyoutPrice.getValue());
        }
        String currency = currencyBox.getValue();


        AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new CreateAuctionListing(time, start, buyout, currency));
    }
}
