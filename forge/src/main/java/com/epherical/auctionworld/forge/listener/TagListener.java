package com.epherical.auctionworld.forge.listener;

import com.epherical.auctionworld.data.AuctionFilterManager;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber()
public class TagListener {

    public static final AuctionFilterManager manager = new AuctionFilterManager();



    @SubscribeEvent
    public static void onTagUpdate(TagsUpdatedEvent event) {
        TagsUpdatedEvent.UpdateCause updateCause = event.getUpdateCause();
        if (updateCause == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            event.getRegistryAccess().registry(Registries.ITEM).ifPresent(manager::updateFilter);
        }
    }
}
