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

package pl.plajer.villagedefense;

import java.util.HashMap;
import java.util.Map;

import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;

/**
 * @author Plajer
 * <p>
 * Created at 15.12.2018
 */
public class ConfigPreferences {

  private Main plugin;
  private Map<Option, Boolean> options = new HashMap<>();
  private Map<String, Integer> customPermissions = new HashMap<>();

  public ConfigPreferences(Main plugin) {
    this.plugin = plugin;
    loadOptions();

    //load custom permissions
    for (String key : plugin.getConfig().getConfigurationSection("CustomPermissions").getKeys(false)) {
      customPermissions.put(key, plugin.getConfig().getInt("CustomPermissions." + key));
      Debugger.debug(LogLevel.INFO, "Loaded custom permission " + key);
    }
  }

  /**
   * Returns whether option value is true or false
   *
   * @param option option to get value from
   * @return true or false based on user configuration
   */
  public boolean getOption(Option option) {
    return options.get(option);
  }

  public Map<String, Integer> getCustomPermissions() {
    return customPermissions;
  }

  private void loadOptions() {
    for (Option option : Option.values()) {
      options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
    }
  }

  public enum Option {
    BOSSBAR_ENABLED("Bossbar-Enabled", true), BUNGEE_ENABLED("BungeeActivated", false), CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true),
    DATABASE_ENABLED("DatabaseActivated", false), INVENTORY_MANAGER_ENABLED("InventoryManager", true);

    private String path;
    private boolean def;

    Option(String path, boolean def) {
      this.path = path;
      this.def = def;
    }

    public String getPath() {
      return path;
    }

    /**
     * @return default value of option if absent in config
     */
    public boolean getDefault() {
      return def;
    }
  }

}
