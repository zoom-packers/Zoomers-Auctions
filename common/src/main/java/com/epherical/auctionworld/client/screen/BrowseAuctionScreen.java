package com.epherical.auctionworld.client.screen;

import com.epherical.auctionworld.AuctionTheWorldAbstract;
import com.epherical.auctionworld.client.AModClient;
import com.epherical.auctionworld.client.AuctionListWidget;
import com.epherical.auctionworld.client.SortableButton;
import com.epherical.auctionworld.client.widgets.AuctionMenuWidget;
import com.epherical.auctionworld.menu.BrowseAuctionMenu;
import com.epherical.auctionworld.networking.C2SPageChange;
import com.epherical.auctionworld.networking.OpenCreateAuction;
import com.epherical.auctionworld.object.AuctionItem;
import com.epherical.auctionworld.object.Page;
import com.epherical.epherolib.networking.AbstractNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Comparator;

public class BrowseAuctionScreen extends AuctionScreen<BrowseAuctionMenu> {


    private Page page = new Page(1, 10);

    private SortableButton<AuctionItem> item;
    private SortableButton<AuctionItem> time;
    private SortableButton<AuctionItem> seller;
    private SortableButton<AuctionItem> highestBidder;
    private SortableButton<AuctionItem> bid;
    private Button createAuctionButton;

    private AuctionListWidget list;

    private ScreenRender tooltipEntry;


    public BrowseAuctionScreen(BrowseAuctionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void addRenderableWidgetExternal(AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        super.init();
        list = new AuctionListWidget(minecraft, this.width + 121, this.height, topPos + 45, topPos + 245, 25, this);
        list.setRenderBackground(false);
        list.setRenderTopAndBottom(false);
        //list.setLeftPos(leftPos);
        addWidget(list);

        if (leftPos >= 0 && topPos >= 0) {

            createAuctionButton = Button.builder(Component.literal("Create Auction"), button1 -> {
                AuctionTheWorldAbstract.getInstance().getNetworking().sendToServer(new OpenCreateAuction(AuctionMenuWidget.CurrentScreen.CREATE_AUCTION));
                    })
                    .pos(leftPos + 387, topPos + 4)
                    .size(120, 20)
                    .build();
            addRenderableWidget(createAuctionButton);

            item = new SortableButton<>(false, Comparator.comparing(auctionItem -> auctionItem.getAuctionItems().get(0).getHoverName().getString()),
                    this.addRenderableWidget(Button.builder(Component.literal("Item -"),
                                    button -> {
                                        //button.setMessage(item.sortDirection("Item"));
                                        time.setActivated(false);
                                        highestBidder.setActivated(false);
                                        item.setActivated(true);
                                        seller.setActivated(false);
                                        bid.setActivated(false);
                                    })
                            .pos(leftPos + 3, topPos + 26)
                            .width(139)
                            .build()));
            time = new SortableButton<>(false, Comparator.comparing(AuctionItem::getTimeLeft),
                    this.addRenderableWidget(Button.builder(Component.literal("Time -"),
                            button -> {
                                //button.setMessage(time.sortDirection("Time"));
                                time.setActivated(true);
                                highestBidder.setActivated(false);
                                item.setActivated(false);
                                seller.setActivated(false);
                                bid.setActivated(false);
                            })
                    .pos(leftPos + 141, topPos + 26)
                    .width(100)
                    .build()));
            seller = new SortableButton<>(false, Comparator.comparing(AuctionItem::getSeller),
                    this.addRenderableWidget(Button.builder(Component.literal("Seller -"),
                            button -> {
                                //button.setMessage(seller.sortDirection("Seller"));
                                seller.setActivated(true);
                                highestBidder.setActivated(false);
                                time.setActivated(false);
                                item.setActivated(false);
                                bid.setActivated(false);

                            })
                    .pos(leftPos + 241, topPos + 26)
                    .width(100)
                    .build()));
            highestBidder = new SortableButton<>(false, Comparator.comparing(AuctionItem::getHighestBidder),
                    this.addRenderableWidget(Button.builder(Component.literal("Highest Bidder -"),
                            button -> {
                                //button.setMessage(highestBidder.sortDirection("Highest Bidder"));
                                highestBidder.setActivated(true);
                                time.setActivated(false);
                                item.setActivated(false);
                                seller.setActivated(false);
                                bid.setActivated(false);
                            })
                    .pos(leftPos + 341, topPos + 26)
                    .width(100)
                    .build()));
            bid = new SortableButton<>(false, Comparator.comparing(AuctionItem::getBuyoutPrice),
                    this.addRenderableWidget(Button.builder(Component.literal("Price -"),
                            button -> {
                                //button.setMessage(bid.sortDirection("Buyout"));
                                bid.setActivated(true);
                                highestBidder.setActivated(false);
                                time.setActivated(false);
                                item.setActivated(false);
                                seller.setActivated(false);
                            })
                    .pos(leftPos + 441, topPos + 26)
                    .width(66)
                    .build()));
            this.list.addEntries(AuctionTheWorldAbstract.getInstance().getAuctionManager(true).getAuctions());


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
        super.render(graphics, x, y, delta);
        if (leftPos >= 0 && topPos >= 0) {
            list.render(graphics, x, y, delta);
            this.renderTooltip(graphics, x, y + 1);
            if (tooltipEntry != null) {
                getTooltipEntry().render(font, graphics, x, y, delta);
            }
        }
    }

    public void setTooltipEntry(ScreenRender tooltipEntry) {
        this.tooltipEntry = tooltipEntry;
    }

    public ScreenRender getTooltipEntry() {
        return tooltipEntry;
    }


    public interface ScreenRender {
        void render(Font font, GuiGraphics graphics, int mouseX, int mouseY, float delta);
    }
}
