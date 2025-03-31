package org.futimka.ratingplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RateCommand implements CommandExecutor {

    private final RatingPlugin plugin;
    private final Map<String, String> lastRatedPlayers = new HashMap<>(); // Хранит информацию о последнем оцененном игроке

    public RateCommand(RatingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("notPlayer")); // Используем сообщение из конфигурации
            return true;
        }

        Player player = (Player) sender;

        // Проверка прав
        if (!player.hasPermission("ratingplugin.rate")) {
            player.sendMessage(plugin.getMessage("noPermission")); // Используем сообщение из конфигурации
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessage("usage")); // Используем сообщение из конфигурации
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getMessage("playerNotFound")); // Используем сообщение из конфигурации
            return true;
        }

        // Запретить игроку оценивать самого себя
        if (player.equals(target)) {
            player.sendMessage(plugin.getMessage("selfRating")); // Используем сообщение из конфигурации
            return true;
        }

        // Проверка на повторную оценку одного и того же игрока
        String lastRatedPlayer = lastRatedPlayers.get(player.getName());
        if (lastRatedPlayer != null && lastRatedPlayer.equals(target.getName())) {
            player.sendMessage(plugin.getMessage("alreadyRated").replace("{player}", target.getName())); // Используем сообщение из конфигурации
            return true;
        }

        // Проверка на повторную оценку
        int lastRatingTime = plugin.getPlayerRewards().getOrDefault(player.getName() + "_lastRated", 0);
        int currentTime = (int) (System.currentTimeMillis() / 1000); // Текущее время в секундах

        // Если игрок оценивал целевого игрока менее 60 секунд назад, запретить повторную оценку
        if (currentTime - lastRatingTime < 60) {
            player.sendMessage(plugin.getMessage("ratingCooldown").replace("{player}", target.getName())); // Используем сообщение из конфигурации
            return true;
        }

        // Увеличиваем рейтинг
        int currentRating = plugin.getPlayerRatings().getOrDefault(target.getName(), 0);
        currentRating++;
        plugin.getPlayerRatings().put(target.getName(), currentRating);

        // Обновляем время последней оценки
        plugin.getPlayerRewards().put(player.getName() + "_lastRated", currentTime);
        lastRatedPlayers.put(player.getName(), target.getName()); // Сохраняем последнего оцененного игрока

        // Проверяем, достиг ли игрок порога рейтинга
        int rewardThreshold = plugin.getRewardThreshold();
        if (currentRating >= rewardThreshold) {
            int rewardLevel = plugin.getPlayerRewards().getOrDefault(target.getName(), 0);
            int maxRewardLevel = plugin.getMaxRewardLevel();
            if (rewardLevel < maxRewardLevel) {
                rewardLevel++;
                plugin.getPlayerRewards().put(target.getName(), rewardLevel);

                // Получаем приз для текущего уровня
                Map<String, Object> prize = plugin.getPrizeForLevel(rewardLevel);
                if (prize != null) {
                    String prizeType = (String) prize.get("type");
                    int amount = (int) prize.get("amount");
                    String item = (String) prize.get("item");
                    String commandStr = (String) prize.get("command");

                    // Обработка приза
                    switch (prizeType) {
                        case "money":
                            // Здесь вы можете интегрировать с плагином Vault или другим плагином для выдачи денег
                            // Например: Economy.add(target.getName(), amount);
                            player.sendMessage(plugin.getMessage("rewardMessage").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount))); // Используем сообщение из конфигурации
                            break;
                        case "item":
                            // Выдача предмета
                            target.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(item), amount));
                            player.sendMessage(plugin.getMessage("rewardMessage").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)).replace("{item}", item)); // Используем сообщение из конфигурации
                            break;
                        case "command":
                            // Выполнение команды
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandStr.replace("{player}", target.getName()));
                            break;
                        default:
                            player.sendMessage(plugin.getMessage("invalidPrizeType")); // Используем сообщение из конфигурации
                            break;
                    }
                }

                // Оповещение игрока о похвале
                target.sendMessage(plugin.getMessage("praise").replace("{rating}", String.valueOf(currentRating)).replace("{level}", String.valueOf(rewardLevel))); // Теперь сообщение о новом рейтинге приходит только целевому игроку
            } else {
                player.sendMessage(plugin.getMessage("maxRewardLevel").replace("{player}", target.getName())); // Используем сообщение из конфигурации
            }
        } else {
            target.sendMessage(plugin.getMessage("ratingInfo").replace("{player}", target.getName()).replace("{rating}", String.valueOf(currentRating))); // Теперь сообщение о текущем рейтинге приходит только целевому игроку
        }

        return true;
    }
}