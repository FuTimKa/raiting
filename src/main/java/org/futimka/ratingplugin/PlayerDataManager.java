package org.futimka.ratingplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {

    private final RatingPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(RatingPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadPlayerData(Map<String, Integer> playerRatings, Map<String, Integer> playerRewards) {
        for (String playerName : dataConfig.getKeys(false)) {
            int rating = dataConfig.getInt(playerName + ".rating", 0);
            int rewardLevel = dataConfig.getInt(playerName + ".rewardLevel", 0);
            playerRatings.put(playerName, rating);
            playerRewards.put(playerName, rewardLevel);
        }
    }

    public void savePlayerData(Map<String, Integer> playerRatings, Map<String, Integer> playerRewards) {
        for (String playerName : playerRatings.keySet()) {
            dataConfig.set(playerName + ".rating", playerRatings.get(playerName));
            dataConfig.set(playerName + ".rewardLevel", playerRewards.get(playerName));
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить данные игроков: " + e.getMessage());
        }
    }
}
