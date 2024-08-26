package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.config.Config;
import com.epherical.auctionworld.menu.WalletManagementMenu;
import com.epherical.auctionworld.object.User;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WalletManagementScreen extends AuctionScreen<WalletManagementMenu> {

    private WalletCurrencyList list;

    public WalletManagementScreen(WalletManagementMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    public void init() {
        list = new WalletCurrencyList(minecraft, this.width + 121, this.height, topPos + 45, topPos + 245, 25, this);
        addWidget(list);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        super.render(graphics, x, y, delta);
        if (leftPos >= 0 && topPos >= 0) {
            if (list != null) {
                list.render(graphics, x, y, delta);
            }
        }
    }

    public class WalletCurrencyList extends ContainerObjectSelectionList<WalletCurrencyList.WalletCurrencyEntry> {

        private final WalletManagementScreen walletManagementScreen;
        private final List<String> currencies;
        private final List<ItemStack> currencyItems;

        public WalletCurrencyList(Minecraft minecraft, int i, int j, int k, int l, int m, WalletManagementScreen walletManagementScreen) {
            super(minecraft, i, j, k, l, m);
            this.walletManagementScreen = walletManagementScreen;
            currencies = List.of(Config.INSTANCE.currencies);
            currencyItems = currencies.stream().map(Config::getCurrencyItem).map(ItemStack::new).toList();

            for (int index = 0; index < currencies.size(); index++) {
                addEntry(new WalletCurrencyEntry(currencies.get(index), currencyItems.get(index), AuctionTheWorldAbstract.getInstance().getUserManager().getUserByID(minecraft.player.getUUID())));
            }
        }

        public class WalletCurrencyEntry extends ContainerObjectSelectionList.Entry<WalletCurrencyEntry> {

            private String currency;
            private ItemStack currencyItem;
            private User user;

            public WalletCurrencyEntry(String currency, ItemStack currencyItem, User user) {
                this.currency = currency;
                this.currencyItem = currencyItem;
                this.user = user;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of();
            }

            @Override
            public void render(GuiGraphics graphics, int row, int top, int left, int width, int height, int x, int y, boolean hovered, float delta) {
                // Structure: Table Row
                // 1'st Column: Currency Item Stack
                // 2'nd Column: Currency In Wallet
                // 3'rd Column: Currency In Auctions
                // 4'th Column: Deposit button
                // 5'th Column: Withdraw button

                graphics.renderFakeItem(currencyItem, left, top);
//                graphics.drawString(font, currency, left + 20, top + 5, 0xFFFFFF);
                graphics.drawString(font, "In Wallet: " + user.getCurrencyAmount(currency), left + 20, top + 5, 0xFFFFFF);
                graphics.drawString(font, "In Auctions: " + user.getCurrencyInAuctions(currency), left + 120, top + 5, 0xFFFFFF);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }
        }
    }
}
