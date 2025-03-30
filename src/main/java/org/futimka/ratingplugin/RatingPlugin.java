package org.futimka.ratingplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.futimka.CheckRatingCommand.CheckRatingCommand;

import java.util.HashMap;
import java.util.Map;

public class RatingPlugin extends JavaPlugin {

    private Map<String, Integer> playerRatings = new HashMap<>();
    private Map<String, Integer> playerRewards = new HashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Создаем конфиг, если его нет
        config = getConfig();
        getCommand("rate").setExecutor(new RateCommand(this));
        getCommand("checkrating").setExecutor(new CheckRatingCommand(this));
        getCommand("merate").setExecutor(new MeRateCommand(this)); // Добавляем команду /merate
        getLogger().info("RatingPlugin включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RatingPlugin выключен!");
    }

    public Map<String, Integer> getPlayerRatings() {
        return playerRatings;
    }

    public Map<String, Integer> getPlayerRewards() {
        return playerRewards;
    }

    public int getRewardThreshold() {
        return config.getInt("reward.threshold", 20); // Значение по умолчанию 20
    }

    public int getMaxRewardLevel() {
        return config.getInt("reward.max-level", 5); // Значение по умолчанию 5
    }

    public int getRewardIncrement() {
        return config.getInt("reward.increment", 10); // Значение по умолчанию 10
    }

    public Map<String, Object> getPrizeForLevel(int level) {
        String path = "reward.prizes.level-" + level;
        if (config.contains(path)) {
            return config.getConfigurationSection(path).getValues(false);
        }
        return null;
    }
}
