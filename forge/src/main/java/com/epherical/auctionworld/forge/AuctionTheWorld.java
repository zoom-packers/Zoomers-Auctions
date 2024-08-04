package com.epherical.auctionworld.forge;

import com.epherical.auctionworld.*;
import com.epherical.auctionworld.forge.client.AModClient;
import com.epherical.auctionworld.command.ClaimCommand;
import com.epherical.auctionworld.data.FlatAuctionStorage;
import com.epherical.auctionworld.data.FlatPlayerStorage;
import com.epherical.epherolib.CommonPlatform;
import com.epherical.epherolib.ForgePlatform;
import com.epherical.epherolib.networking.ForgeNetworking;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
