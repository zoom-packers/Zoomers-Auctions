package com.epherical.auctionworld;

import com.epherical.auctionworld.data.PlayerStorage;
import com.epherical.auctionworld.object.User;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class UserManager {


    private Map<UUID, User> players = new HashMap<>();
    private PlayerStorage playerStorage;


    public UserManager(PlayerStorage playerStorage) {
        this.playerStorage = playerStorage;
        User user = new User(Util.NIL_UUID, "Server");
        user.setSaveData(false);
        players.put(Util.NIL_UUID, user);
    }

    public User getUserByID(UUID uuid) {
        return players.get(uuid);
    }

    public Map<UUID, User> getPlayers() {
        return players;
    }

    public void playerJoined(ServerPlayer player) {
        if (!players.containsKey(player.getUUID())) {
            User user = playerStorage.loadUser(player);
            user.setPlayer(player);
            players.put(player.getUUID(), user);
            playerStorage.savePlayer(user);
            user.onPlayerJoin();
        } else {
            var user = getUserByID(player.getUUID());
            user.setPlayer(player);
            user.onPlayerJoin();
        }
    }

    public void loadPlayers() {
        this.players = playerStorage.loadUsers();
        User user = new User(Util.NIL_UUID, "Server");
        user.setSaveData(false);
        players.put(Util.NIL_UUID, user);
    }

    public void saveAllPlayers() {
        playerStorage.saveAllPlayers(players);
    }

    public void savePlayer(User user) {
        playerStorage.savePlayer(user);
    }

    public void playerLeft(ServerPlayer player) {
        User userByID = getUserByID(player.getUUID());
        playerStorage.savePlayer(userByID);
        userByID.setPlayer(null);
        userByID.onPlayerLeave();
    }

    public PlayerStorage getPlayerStorage() {
        return playerStorage;
    }
}
