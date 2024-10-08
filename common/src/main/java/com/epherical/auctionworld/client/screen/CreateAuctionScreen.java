package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.menu.CreateAuctionMenu;
import com.epherical.auctionworld.menu.slot.SelectableSlot;
import com.epherical.auctionworld.networking.CreateAuctionListing;
import com.epherical.auctionworld.networking.OpenCreateAuction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class CreateAuctionScreen extends AuctionScreen<CreateAuctionMenu> {

    private Button createAuction;
    private EditBox timeSelection;
    //private EditBox bidIncrement;
    private EditBox startingBid;
    private EditBox buyoutPrice;
    private Button browseAuctions;
    private String currencySelected = "";
    private char timeUnit = ' ';


    public CreateAuctionScreen(CreateAuctionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }


    private EditBox drawStringField(int x, int y, int width, int height) {
        var result = new EditBox(font, x, y, width, height, Component.empty());
        addRenderableWidget(result);
        return result;
    }

    private EditBox drawNumericalField(int x, int y, int width, int height) {
        var result = new EditBox(font, x, y, width, height, Component.empty());
        result.setFilter(s -> s.matches("[0-9]+") || s.isEmpty());
        addRenderableWidget(result);
        return addRenderableWidget(result);
    }

    private Button drawButton(int x, int y, int width, int height, Component text, Button.OnPress onPress) {
        var result = Button.builder(text, onPress).pos(x, y).size(width, height).build();
        addRenderableWidget(result);
        return result;
    }

    private void drawMultiOption(int x, int y, int width, int height, List<SelectionButton> options) {
        var optionSize = options.size();
        var singleButtonSize = width / optionSize;
        for (int i = 0; i < optionSize; i++) {
            var option = options.get(i);
            var button = Button.builder(Component.literal(option.label), option.onPress).pos(x + (i * singleButtonSize), y).size(singleButtonSize, height).build();
            addRenderableWidget(button);
        }
    }

    private void drawString(GuiGraphics graphics, int x, int y, Component text) {
        graphics.drawString(font, text, x, y, 0xFFFFFF);
    }

    @Override
    protected void init() {
        super.init();
        browseAuctions = Button.builder(Component.literal("Browse Auctions"), button1 -> {
                    AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(AuctionMenuWidget.CurrentScreen.BROWSE_AUCTIONS));
                })
                .pos(leftPos + 387, topPos + 4)
                .size(120, 20)
                .build();
        addRenderableWidget(browseAuctions);

        var currencies = Config.INSTANCE.currencies;
        var options = new ArrayList<SelectionButton>();
        for (int i = 0; i < currencies.length; i++) {
            var currency = currencies[i];
            var alias = Config.getAlias(currency);
            options.add(new SelectionButton(currency, alias, button -> {
                currencySelected = currency;
            }));
        }

        timeSelection = drawNumericalField(leftPos + 10, topPos + 40, 310, 20);
        drawMultiOption(leftPos + 322, topPos + 40, 180, 20, List.of(
                new SelectionButton("m", "Minutes", button -> timeUnit = 'm'),
                new SelectionButton("h", "Hours", button -> timeUnit = 'h'),
                new SelectionButton("d", "Days", button -> timeUnit = 'd')
        ));

        startingBid = drawNumericalField(leftPos + 10, topPos + 80, 492, 20);
        buyoutPrice = drawNumericalField(leftPos + 10, topPos + 120, 492, 20);
        drawMultiOption(leftPos + 10, topPos + 160, 492, 20, options);

        createAuction = drawButton(leftPos + 10, topPos + 220, 492, 20, CommonComponents.GUI_DONE, button -> {
            validateAndSendToServer();
        });
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        super.render(graphics, x, y, delta);

        renderInventoryHighlight(graphics);

        var timeResult = timeSelection.getValue() + " " + timeUnitToString(timeUnit);
        var timeLabel = timeSelection.getValue().isEmpty() || timeUnit == ' ' ?
                Component.translatable("How many much time until auction expires? - Insert time & unit") :
                Component.translatable("How many much time until auction expires? - " + timeResult);

        var startingBidLabel = startingBid.getValue().isEmpty() ?
                Component.translatable("Starting bid - Insert starting bid") :
                Component.translatable("Starting bid - " + startingBid.getValue());

        var buyoutPriceLabel = buyoutPrice.getValue().isEmpty() ?
                Component.translatable("Buyout price - Insert buyout price") :
                Component.translatable("Buyout price - " + buyoutPrice.getValue());

        var currencyLabel = currencySelected.isEmpty() ?
                Component.translatable("Currency - Select Currency") :
                Component.translatable("Currency - " + Config.getAlias(currencySelected));

        drawString(graphics, leftPos + 10, topPos + 30, timeLabel);
        drawString(graphics, leftPos + 10, topPos + 70, startingBidLabel);
        drawString(graphics, leftPos + 10, topPos + 110, buyoutPriceLabel);
        drawString(graphics, leftPos + 10, topPos + 150, currencyLabel);


        this.renderTooltip(graphics, x, y);
    }

    private String timeUnitToString(char timeUnit) {
        return switch (timeUnit) {
            case 'm' -> "minutes";
            case 'd' -> "days";
            case 'h' -> "hours";
            default -> "invalid";
        };
    }


    private void renderInventoryHighlight(GuiGraphics graphics) {
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
    }

    private void validateAndSendToServer() {
        int time;
        if (timeUnit == 'd') {
            String value = timeSelection.getValue();
            if (value.isEmpty()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Time is empty"));
                return;
            }
            time = Integer.parseInt(value);
            if (time > 7) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Max days is 7"));
                return;
            }
            time *= 24;
            time *= 60;
        } else if (timeUnit == 'h') {
            String value = timeSelection.getValue();
            if (value.isEmpty()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Time is empty"));
                return;
            }
            time = Integer.parseInt(value);
            if (time > 168) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Max hours is 168"));
                return;
            }
            time *= 60;
        } else if (timeUnit == 'm') {
            String value = timeSelection.getValue();
            if (value.isEmpty()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Time is empty"));
                return;
            }
            time = Integer.parseInt(value);
            if (time > 1080) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Max minutes is 1080"));
                return;
            }
        } else {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Invalid time unit"));
            return;
        }

        if (startingBid.getValue().isEmpty()) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Starting bid is empty"));
            return;
        }
        String startingBidValue = startingBid.getValue();
        if (startingBidValue.length() > 9) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Max starting bid is 999999999"));
            return;
        }
        int start = Integer.parseInt(startingBidValue);
        int buyout = -1;
        if (!buyoutPrice.getValue().isEmpty()) {
            buyout = Integer.parseInt(buyoutPrice.getValue());
        }
        String currency = currencySelected;
        if (currency.isEmpty()) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Please select a currency"));
            return;
        }

        var foundCurrency = false;
        for (String c : Config.INSTANCE.currencies) {
            if (c.equals(currency)) {
                foundCurrency = true;
                break;
            }
        }
        if (!foundCurrency) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Invalid currency"));
            return;
        }


        AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new CreateAuctionListing(time, start, buyout, currency));
    }

    private record SelectionButton<T>(T value, String label, Button.OnPress onPress) {
        private SelectionButton(T value, String label, Button.OnPress onPress) {
            this.value = value;
            this.label = label;
            this.onPress = onPress;
        }
    }
}
