package org.futimka.ratingplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeRateCommand implements CommandExecutor {

    private final RatingPlugin plugin;

    public MeRateCommand(RatingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда доступна только игрокам.");
            return true;
        }

        Player player = (Player) sender;
        int rating = plugin.getPlayerRatings().getOrDefault(player.getName(), 0);
        int rewardLevel = plugin.getPlayerRewards().getOrDefault(player.getName(), 0);

        player.sendMessage("Ваш рейтинг: " + rating + ", Уровень награды: " + rewardLevel);
        return true;
    }
}
