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

package pl.plajer.villagedefense.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.villagedefense.ConfigPreferences;
import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.utils.MessageUtils;
import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;
import pl.plajerlair.core.utils.ConfigUtils;

/**
 * @author Plajer, TomTheDeveloper
 * @since 2.0.0
 * <p>
 * Class for accessing users statistics.
 */
public class StatsStorage {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  private static Map sortByValue(Map unsortMap) {
    List list = new LinkedList(unsortMap.entrySet());
    list.sort((o1, o2) -> ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue()));
    Map sortedMap = new LinkedHashMap();
    for (Object sort : list) {
      Map.Entry entry = (Map.Entry) sort;
      sortedMap.put(entry.getKey(), entry.getValue());
    }
    return sortedMap;
  }

  /**
   * Get all UUID's sorted ascending by Statistic Type
   *
   * @param stat Statistic type to get (kills, deaths etc.)
   * @return Map of UUID keys and Integer values sorted in ascending order of requested statistic type
   */
  public static Map<UUID, Integer> getStats(StatisticType stat) {
    Debugger.debug(LogLevel.INFO, "VillageDefense API getStats(" + stat.getName() + ") run");
    if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
      ResultSet set = plugin.getMySQLDatabase().executeQuery("SELECT UUID, " + stat.getName() + " FROM playerstats ORDER BY " + stat.getName() + " ASC;");
      Map<java.util.UUID, java.lang.Integer> column = new LinkedHashMap<>();
      try {
        while (set.next()) {
          column.put(java.util.UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
        }
      } catch (SQLException e) {
        e.printStackTrace();
        MessageUtils.errorOccurred();
        Bukkit.getConsoleSender().sendMessage("Cannot get contents from MySQL database!");
        Bukkit.getConsoleSender().sendMessage("Check configuration of mysql.yml file or disable mysql option in config.yml");
      }
      return column;
    } else {
      FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
      Map<UUID, Integer> stats = new TreeMap<>();
      for (String string : config.getKeys(false)) {
        if (string.equals("data-version")) {
          continue;
        }
        stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
      }
      return sortByValue(stats);
    }
  }

  /**
   * Get user statistic based on StatisticType
   *
   * @param player        Online player to get data from
   * @param statisticType Statistic type to get (kills, deaths etc.)
   * @return int of statistic
   * @see StatisticType
   */
  public static int getUserStats(Player player, StatisticType statisticType) {
    return plugin.getUserManager().getUser(player).getStat(statisticType);
  }

  /**
   * Available statistics to get.
   */
  public enum StatisticType {
    ORBS("orbs", false), KILLS("kills", true), DEATHS("deaths", true), GAMES_PLAYED("gamesplayed", true),
    HIGHEST_WAVE("highestwave", true), LEVEL("level", true), XP("xp", true);

    private String name;
    private boolean persistent;

    StatisticType(String name, boolean persistent) {
      this.name = name;
      this.persistent = persistent;
    }

    public String getName() {
      return name;
    }

    public boolean isPersistent() {
      return persistent;
    }

  }

}
