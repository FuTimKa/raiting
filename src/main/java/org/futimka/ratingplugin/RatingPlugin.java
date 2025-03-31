package org.futimka.ratingplugin;

import org.bukkit.ChatColor;
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
        loadPlayerData(); // Загружаем данные игроков
        getCommand("rate").setExecutor(new RateCommand(this));
        getCommand("checkrating").setExecutor(new CheckRatingCommand(this));
        getCommand("merate").setExecutor(new MeRateCommand(this)); // Добавляем команду /merate
        getLogger().info("RatingPlugin включен!");
    }

    @Override
    public void onDisable() {
        savePlayerData(); // Сохраняем данные игроков
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
        String path = "reward.levels.level-" + level;
        if (config.contains(path)) {
            return config.getConfigurationSection(path).getValues(false);
        }
        return null;
    }

    // Новый метод для получения сообщений из конфигурации с поддержкой цветовых кодов
    public String getMessage(String key) {
        String message = config.getString("messages." + key, "Сообщение не найдено."); // Возвращает сообщение или текст по умолчанию
        return ChatColor.translateAlternateColorCodes('&', message); // Преобразует цветовые коды
    }

    // Метод для загрузки данных игроков из конфигурации
    private void loadPlayerData() {
        for (String playerName : config.getConfigurationSection("players").getKeys(false)) {
            int rating = config.getInt("players." + playerName + ".rating", 0);
            int rewardLevel = config.getInt("players." + playerName + ".rewardLevel", 0);
            playerRatings.put(playerName, rating);
            playerRewards.put(playerName, rewardLevel);
        }
    }

    // Метод для сохранения данных игроков в конфигурацию
    private void savePlayerData() {
        for (String playerName : playerRatings.keySet()) {
            config.set("players." + playerName + ".rating", playerRatings.get(playerName));
            config.set("players." + playerName + ".rewardLevel", playerRewards.get(playerName));
        }
        saveConfig(); // Сохраняем конфигурацию
    }
}