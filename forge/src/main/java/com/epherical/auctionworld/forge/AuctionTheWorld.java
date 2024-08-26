package com.epherical.auctionworld.forge;

import com.epherical.auctionworld.*;
import com.epherical.auctionworld.forge.client.AModClient;
import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.ForgePlatform;
import com.epherical.epherolib.networking.ForgeNetworking;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.epherical.auctionworld.Constants.MOD_ID;

@Mod(MOD_ID)
public class AuctionTheWorld {

    public static AuctionTheWorldAbstract abs;

    public AuctionTheWorld() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::commonInit);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
            AuctionTheWorldAbstract.client = true;
            AuctionTheWorldAbstract.auctionManager = new AuctionManager(null, true, null);
            eventBus.addListener(AModClient::tooltipRegister);
            return AModClient::initClient;
        });
        MinecraftForge.EVENT_BUS.register(this);
        EventBuses.registerModEventBus(MOD_ID, eventBus);

        var networking = new ForgeNetworking(AuctionTheWorldAbstract.MOD_CHANNEL, "1", s -> true, s -> true);
        abs = new AuctionTheWorldAbstract(networking);
        CommonPlatform.create(new ForgePlatform());
    }

    private void commonInit(FMLCommonSetupEvent event) {

    }
}
