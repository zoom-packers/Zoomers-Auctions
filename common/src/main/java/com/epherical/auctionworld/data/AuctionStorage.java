package com.epherical.auctionworld.data;

import com.epherical.auctionworld.object.AuctionItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AuctionStorage {


    Map<UUID, AuctionItem> loadAuctionItems();

    boolean saveAuctionItems(Map<UUID, AuctionItem> items);

}
