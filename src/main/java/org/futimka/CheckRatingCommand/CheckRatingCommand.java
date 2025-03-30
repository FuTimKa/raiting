package org.futimka.CheckRatingCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.futimka.ratingplugin.RatingPlugin;

public class CheckRatingCommand implements CommandExecutor {

    private final RatingPlugin plugin;

    public CheckRatingCommand(RatingPlugin plugin) {
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
        if (!player.hasPermission("ratingplugin.checkrating")) {
            player.sendMessage("У вас нет прав для использования этой команды.");
            return true;
        }

        int rating = plugin.getPlayerRatings().getOrDefault(player.getName(), 0);
        int rewardLevel = plugin.getPlayerRewards().getOrDefault(player.getName(), 0);

        player.sendMessage("Ваш рейтинг: " + rating + ", Уровень награды: " + rewardLevel);
        return true;
    }
}
