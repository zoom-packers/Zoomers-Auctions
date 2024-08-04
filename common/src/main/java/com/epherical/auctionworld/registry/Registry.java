package com.epherical.auctionworld.registry;

import com.epherical.auctionworld.Constants;
import com.epherical.auctionworld.block.AuctionBlock;
import com.epherical.auctionworld.menu.BrowseAuctionMenu;
import com.epherical.auctionworld.menu.CreateAuctionMenu;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class Registry {

    public static AuctionBlock AUCTION_HOUSE;

    public static final MenuType<BrowseAuctionMenu> BROWSE_AUCTION_MENU = new MenuType<>(BrowseAuctionMenu::new, FeatureFlags.VANILLA_SET);
    public static final MenuType<CreateAuctionMenu> CREATE_AUCTION_MENU = new MenuType<>(CreateAuctionMenu::new, FeatureFlags.VANILLA_SET);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Constants.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Constants.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Constants.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<Block> AUCTION_HOUSE_SUPPLIER = BLOCKS.register(new ResourceLocation(Constants.MOD_ID, "auction_house"), () -> new AuctionBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava()));
    public static final RegistrySupplier<Item> AUCTION_HOUSE_ITEM_SUPPLIER = ITEMS.register(new ResourceLocation(Constants.MOD_ID, "auction_house"), () -> new ItemNameBlockItem(AUCTION_HOUSE_SUPPLIER.get(), new Item.Properties()));
    public static final RegistrySupplier<MenuType<BrowseAuctionMenu>> BROWSE_AUCTION_MENU_SUPPLIER = MENU_TYPES.register(new ResourceLocation(Constants.MOD_ID, "browse_auction_menu"), () -> BROWSE_AUCTION_MENU);
    public static final RegistrySupplier<MenuType<CreateAuctionMenu>> CREATE_AUCTION_MENU_SUPPLIER = MENU_TYPES.register(new ResourceLocation(Constants.MOD_ID, "create_auction_menu"), () -> CREATE_AUCTION_MENU);

    public static void bootstrap() {
        BLOCKS.register();
        ITEMS.register();
        MENU_TYPES.register();
    }

    public static ResourceLocation id(String value) {
        return new ResourceLocation(Constants.MOD_ID, value);
    }
}
