package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.AModClient;
import com.epherical.auctionworld.client.AuctionListWidget;
import com.epherical.auctionworld.client.SortableButton;
import com.epherical.auctionworld.registry.Registry;
import com.epherical.auctionworld.menu.BrowseAuctionMenu;
import com.epherical.auctionworld.networking.C2SPageChange;
import com.epherical.auctionworld.networking.OpenCreateAuction;
import com.epherical.auctionworld.networking.SlotManipulation;
import com.epherical.auctionworld.object.Action;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.auctionworld.object.Page;
import com.epherical.auctionworld.object.User;
import com.epherical.epherolib.client.Icon;
import com.epherical.epherolib.client.SmallIconButton;
import com.epherical.epherolib.client.widgets.DiscordButton;
import com.epherical.epherolib.client.widgets.PatreonButton;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Comparator;

public class BrowseAuctionScreen extends AbstractContainerScreen<BrowseAuctionMenu> {
    private static final ResourceLocation AUCTION_LOCATION = Registry.id("textures/gui/container/auction.png");


    private Page page = new Page(1, 10);

    private Button auctionScreenButton;
    //private Button browse;

    private SortableButton<AuctionItem> item;
    private SortableButton<AuctionItem> time;
    private SortableButton<AuctionItem> seller;
    private SortableButton<AuctionItem> bid;

    private AuctionListWidget list;

    private ScreenRender tooltipEntry;


    public BrowseAuctionScreen(BrowseAuctionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        imageWidth = 512;
        imageHeight = 480;
        super.init();
        list = new AuctionListWidget(minecraft, this.width + 121, this.height, topPos + 45, topPos + 245, 25, this);
        list.setRenderBackground(false);
        list.setRenderTopAndBottom(false);
        //list.setLeftPos(leftPos);
        addWidget(list);

        if (leftPos >= 0 && topPos >= 0) {
            auctionScreenButton = this.addRenderableWidget(new PlainTextButton(leftPos + 81, topPos + 248, 80, 20, Component.translatable("Create Auction"), press -> {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction());
            }, font));

            this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Insert Stack"), var1 -> {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new SlotManipulation(User.CURRENCY_SLOT, Action.INSERT_SLOT));
            }).pos(leftPos + 351, topPos + 252).setIcon(Icon.INCREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Insert a stack."))).build());
            this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Insert as much as possible"), var1 -> {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new SlotManipulation(User.CURRENCY_SLOT, Action.INSERT_ALL));
            }).pos(leftPos + 351, topPos + 262).setIcon(Icon.INCREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Insert all available items"))).build());

            // Right orange, remove items
            this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Remove Stack"), var1 -> {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new SlotManipulation(User.CURRENCY_SLOT, Action.REMOVE_STACK));
            }).pos(leftPos + 380, topPos + 252).setIcon(Icon.DECREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Remove a stack."))).build());
            this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Remove as much as possible"), var1 -> {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new SlotManipulation(User.CURRENCY_SLOT, Action.REMOVE_ALL));
            }).pos(leftPos + 380, topPos + 262).setIcon(Icon.DECREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Remove all available items"))).build());


            this.addRenderableWidget(new DiscordButton(leftPos + 515, topPos + 5, press -> {
                this.minecraft.setScreen(new ConfirmLinkScreen(b -> {
                    if (b) {
                        Util.getPlatform().openUri("https://discord.gg/a3cX6ttPJK");
                    }

                    this.minecraft.setScreen(this);
                }, "https://discord.gg/a3cX6ttPJK", true));
            }));
            this.addRenderableWidget(new PatreonButton(leftPos + 515, topPos + 25, press -> {
                this.minecraft.setScreen(new ConfirmLinkScreen(b -> {
                    if (b) {
                        Util.getPlatform().openUri("https://www.patreon.com/thethonk");
                    }

                    this.minecraft.setScreen(this);
                }, "https://www.patreon.com/thethonk", true));
            }));

            // 83, 247

            time = new SortableButton<>(false, Comparator.comparing(AuctionItem::getTimeLeft), this.addRenderableWidget(Button.builder(Component.literal("Time -"),
                    button -> {
                        //button.setMessage(time.sortDirection("Time"));
                        time.setActivated(true);
                        item.setActivated(false);
                        seller.setActivated(false);
                        bid.setActivated(false);
                    }).pos(leftPos + 242, topPos + 26).width(100).build()));
            item = new SortableButton<>(false, Comparator.comparing(auctionItem -> auctionItem.getAuctionItems().get(0).getHoverName().getString()),
                    this.addRenderableWidget(Button.builder(Component.literal("Item -"),
                                    button -> {
                                        //button.setMessage(item.sortDirection("Item"));
                                        time.setActivated(false);
                                        item.setActivated(true);
                                        seller.setActivated(false);
                                        bid.setActivated(false);
                                    })
                            .pos(leftPos + 125, topPos + 26).width(117)
                            .build()));
            seller = new SortableButton<>(false, Comparator.comparing(AuctionItem::getSeller), this.addRenderableWidget(Button.builder(Component.literal("Seller -"),
                            button -> {
                                //button.setMessage(seller.sortDirection("Seller"));
                                seller.setActivated(true);
                                time.setActivated(false);
                                item.setActivated(false);
                                bid.setActivated(false);

                            })
                    .pos(leftPos + 342, topPos + 26).width(100)
                    .build()));
            bid = new SortableButton<>(false, Comparator.comparing(AuctionItem::getBuyoutPrice), this.addRenderableWidget(Button.builder(Component.literal("Price -"),
                            button -> {
                                //button.setMessage(bid.sortDirection("Buyout"));
                                bid.setActivated(true);
                                time.setActivated(false);
                                item.setActivated(false);
                                seller.setActivated(false);
                            })
                    .pos(leftPos + 442, topPos + 26).width(67)
                    .build()));
            this.list.addEntries(AuctionTheWorldAbstract.getInstance().getAuctionManager().getAuctions());


            AbstractNetworking<?, ?> networking = AuctionTheWorldAbstract.getInstance().getNetworking();
            // todo; position buttons for making pages
            this.addRenderableWidget(Button.builder(Component.literal(">>"), p_93751_ -> {
                int newPage = page.getPage() + 1;
                if (newPage > AModClient.maxPages) {
                    newPage = AModClient.maxPages;
                }
                page = new Page(newPage, 10);
                networking.sendToServer(new C2SPageChange(page.getPage()));
            }).pos(leftPos + 395 + 50, topPos + 250).width(50).build());
            this.addRenderableWidget(Button.builder(Component.literal("<<"), p_93751_ -> {
                int newPage = page.getPage() - 1;
                if (newPage <= 0 || newPage > AModClient.maxPages) {
                    newPage = Math.min(AModClient.maxPages, 1);
                }
                page = new Page(newPage, 10);
                networking.sendToServer(new C2SPageChange(page.getPage()));
            }).pos(leftPos + 395, topPos + 250).width(50).build());
        }



        /*browse = this.addRenderableWidget(Button.builder(Component.translatable("Browse"), press -> {

        }).width(60).pos(leftPos, 258).build());*/
    }

    public void reset() {
        tooltipEntry = null;
        minecraft.setScreen(this);
        list.reset();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        list.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        if (leftPos >= 0 && topPos >= 0) {
            this.renderBackground(graphics);
            super.render(graphics, x, y, delta);
            list.render(graphics, x, y, delta);
            this.renderTooltip(graphics, x, y + 1);
            if (tooltipEntry != null) {
                getTooltipEntry().render(font, graphics, x, y, delta);
            }
        } else {
            this.renderBackground(graphics);
            graphics.drawString(font, "Decrease your GUI scale to see the entire menu!",  50, 60, 0xFFFFFF);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int x, int y) {
        int left = this.leftPos;
        int center = (this.height - this.imageHeight) / 2;
        graphics.blit(AUCTION_LOCATION, left, center, 0, 0, this.imageWidth, this.imageHeight, 512, 512);
    }

    public void setTooltipEntry(ScreenRender tooltipEntry) {
        this.tooltipEntry = tooltipEntry;
    }

    public ScreenRender getTooltipEntry() {
        return tooltipEntry;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        // todo; filtering function
        // AuctionFilterManager.Node<Item> tree = TagListener.manager.getTree();
        // tree.beginRenderText(graphics, this.font, this.titleLabelX, this.titleLabelY, 1);
        /*seller.sort(auctionItems);
        bid.sort(auctionItems);
        item.sort(auctionItems);
        time.sort(auctionItems);*/
        //graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

    }

    public interface ScreenRender {
        void render(Font font, GuiGraphics graphics, int mouseX, int mouseY, float delta);
    }
}
