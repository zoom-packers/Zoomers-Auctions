package com.epherical.auctionworld.data;

import com.epherical.auctionworld.object.User;
import com.epherical.epherolib.data.WorldBasedStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class FlatPlayerStorage extends WorldBasedStorage implements PlayerStorage {

    private static final Logger LOGGER = LogUtils.getLogger();

    public FlatPlayerStorage(LevelResource resource, MinecraftServer server, String path) {
        super(resource, server, path);
    }

    @Override
    protected Gson buildGson(GsonBuilder builder) {
        return builder.create();
    }


    public Path resolve(UUID uuid) {
        return basePath.resolve(uuid.toString() + ".json");
    }

    @Override
    public void savePlayer(User user) {
        try {
            writeTagToFile(user.saveUser(), resolve(user.getUuid()));
        } catch (IOException e) {
            LOGGER.warn("Could not save user {}", user.getName(), e);
        }
    }

    @Override
    public Map<UUID, User> loadUsers() {
        try (Stream<Path> pathStream = Files.walk(basePath, FileVisitOption.FOLLOW_LINKS)) {
           Map<UUID, User> userMap = new HashMap<>();
           pathStream.forEach(path -> {
               if (!path.equals(getBasePath())) {
                   try {
                       Tag tag = readTagFromFile(path);
                       User user = User.loadUser((CompoundTag) tag);
                       userMap.put(user.getUuid(), user);
                   } catch (IOException e) {
                       LOGGER.warn("Could not load user from path {},", path, e);
                   }
               }
           });
           return userMap;
        } catch (IOException e) {
           LOGGER.warn("FolderMissing {}", basePath, e);
        }
        return new HashMap<>();
    }

    @Override
    public User loadUser(ServerPlayer player) {
        Path resolve = resolve(player.getUUID());
        Tag tag;
        try {
            tag = readTagFromFile(resolve);
            return User.loadUser((CompoundTag) tag);
        } catch (IOException ignored) {}
        return new User(player.getUUID(), player.getScoreboardName(), 0);
    }

    @Override
    public void saveAllPlayers(Map<UUID, User> players) {
        for (User value : players.values()) {
            if (value.canBeSaved()) {
                savePlayer(value);
            }
        }
    }
}
