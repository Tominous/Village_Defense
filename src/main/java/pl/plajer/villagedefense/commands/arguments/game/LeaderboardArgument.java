/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.villagedefense.commands.arguments.game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import pl.plajer.villagedefense.ConfigPreferences;
import pl.plajer.villagedefense.api.StatsStorage;
import pl.plajer.villagedefense.commands.arguments.ArgumentsRegistry;
import pl.plajer.villagedefense.commands.arguments.data.CommandArgument;
import pl.plajer.villagedefense.commands.completion.CompletableArgument;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class LeaderboardArgument {

  public LeaderboardArgument(ArgumentsRegistry registry) {
    List<String> stats = new ArrayList<>();
    for (StatsStorage.StatisticType val : StatsStorage.StatisticType.values()) {
      if (!val.isPersistent() || val == StatsStorage.StatisticType.XP) {
        continue;
      }
      stats.add(val.name().toLowerCase());
    }
    registry.getTabCompletion().registerCompletion(new CompletableArgument("villagedefense", "top", stats));
    registry.mapArgument("villagedefense", new CommandArgument("top", "", CommandArgument.ExecutorType.PLAYER) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        try {
          if (args.length == 1) {
            sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Type-Name"));
            return;
          }
          StatsStorage.StatisticType statisticType = StatsStorage.StatisticType.valueOf(args[1].toUpperCase());
          if (statisticType == StatsStorage.StatisticType.XP) {
            sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Invalid-Name"));
            return;
          }
          LinkedHashMap<UUID, Integer> stats = (LinkedHashMap<UUID, Integer>) StatsStorage.getStats(statisticType);
          sender.sendMessage(registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Header"));
          String statistic = StringUtils.capitalize(statisticType.toString().toLowerCase().replace("_", " "));
          for (int i = 0; i < 10; i++) {
            try {
              UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
              String message = registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Format");
              message = StringUtils.replace(message, "%position%", String.valueOf(i + 1));
              message = StringUtils.replace(message, "%name%", Bukkit.getOfflinePlayer(current).getName());
              message = StringUtils.replace(message, "%value%", String.valueOf(stats.get(current)));
              message = StringUtils.replace(message, "%statistic%", statistic); //Games_played > Games played etc
              sender.sendMessage(message);
              stats.remove(current);
            } catch (IndexOutOfBoundsException ex) {
              String message = registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Format");
              message = StringUtils.replace(message, "%position%", String.valueOf(i + 1));
              message = StringUtils.replace(message, "%name%", "Empty");
              message = StringUtils.replace(message, "%value%", "0");
              message = StringUtils.replace(message, "%statistic%", statistic); //Games_played > Games played etc
              sender.sendMessage(message);
            } catch (NullPointerException ex) {
              UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
              if (registry.getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
                ResultSet set = registry.getPlugin().getMySQLDatabase().executeQuery("SELECT name FROM playerstats WHERE UUID='" + current.toString() + "'");
                try {
                  if (set.next()) {
                    String message = registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Format");
                    message = StringUtils.replace(message, "%position%", String.valueOf(i + 1));
                    message = StringUtils.replace(message, "%name%", set.getString(1));
                    message = StringUtils.replace(message, "%value%", String.valueOf(stats.get(current)));
                    message = StringUtils.replace(message, "%statistic%", statistic); //Games_played > Games played etc
                    sender.sendMessage(message);
                    continue;
                  }
                } catch (SQLException ignored) {
                }
              }
              String message = registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Format");
              message = StringUtils.replace(message, "%position%", String.valueOf(i + 1));
              message = StringUtils.replace(message, "%name%", "Unknown Player");
              message = StringUtils.replace(message, "%value%", String.valueOf(stats.get(current)));
              message = StringUtils.replace(message, "%statistic%", statistic); //Games_played > Games played etc
              sender.sendMessage(message);
            }
          }
        } catch (IllegalArgumentException e) {
          sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + registry.getPlugin().getChatManager().colorMessage("Commands.Statistics.Invalid-Name"));
        }
      }
    });
  }

}
