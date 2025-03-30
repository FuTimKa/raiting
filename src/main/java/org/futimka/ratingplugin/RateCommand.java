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
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;

        // Проверка прав
        if (!player.hasPermission("ratingplugin.rate")) {
            player.sendMessage("У вас нет прав для использования этой команды.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("Используйте: /rate <игрок>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("Игрок не найден.");
            return true;
        }

        // Запретить игроку оценивать самого себя
        if (player.equals(target)) {
            player.sendMessage("Вы не можете оценивать самого себя.");
            return true;
        }

        // Проверка на повторную оценку одного и того же игрока
        String lastRatedPlayer = lastRatedPlayers.get(player.getName());
        if (lastRatedPlayer != null && lastRatedPlayer.equals(target.getName())) {
            player.sendMessage("Вы уже оценили " + target.getName() + ". Вы больше не можете его оценивать.");
            return true;
        }

        // Проверка на повторную оценку
        int lastRatingTime = plugin.getPlayerRewards().getOrDefault(player.getName() + "_lastRated", 0);
        int currentTime = (int) (System.currentTimeMillis() / 1000); // Текущее время в секундах

        // Если игрок оценивал целевого игрока менее 60 секунд назад, запретить повторную оценку
        if (currentTime - lastRatingTime < 60) {
            player.sendMessage("Вы не можете оценивать " + target.getName() + " снова так быстро. Подождите 60 секунд.");
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
                            player.sendMessage("Вы оценили " + target.getName() + ". Он получил " + amount + " монет!");
                            break;
                        case "item":
                            // Выдача предмета
                            target.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.valueOf(item), amount));
                            player.sendMessage("Вы оценили " + target.getName() + ". Он получил " + amount + " " + item + "!");
                            break;
                        case "command":
                            // Выполнение команды
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandStr.replace("{player}", target.getName()));
                            break;
                        default:
                            player.sendMessage("Неправильный тип приза.");
                            break;
                    }
                }

                // Оповещение игрока о похвале
                target.sendMessage("Вас похвалили! Теперь у вас " + currentRating + " очков рейтинга и уровень награды " + rewardLevel + "!");
                player.sendMessage("Вы оценили " + target.getName() + ". Он получил награду уровня " + rewardLevel + "!");
            } else {
                player.sendMessage(target.getName() + " уже достиг максимального уровня награды.");
            }
        } else {
            player.sendMessage("Вы оценили " + target.getName() + ". У него теперь " + currentRating + " очков рейтинга.");
        }

        return true;
    }
}