package com.epherical.auctionworld.data;

import com.epherical.auctionworld.object.User;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public interface PlayerStorage {

    // todo; decide how we want to save the player, files for each or one big file for everyone?
    void savePlayer(User user);

    Map<UUID, User> loadUsers();

    User loadUser(ServerPlayer player);

    void saveAllPlayers(Map<UUID, User> players);

}
