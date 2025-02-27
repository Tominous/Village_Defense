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

package pl.plajer.villagedefense.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaManager;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.arena.ArenaState;
import pl.plajer.villagedefense.handlers.language.LanguageManager;
import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.LocationUtils;
import pl.plajerlair.core.utils.XMaterial;

public class SignManager implements Listener {

  private Main plugin;
  private Map<Sign, Arena> loadedSigns = new HashMap<>();
  private Map<ArenaState, String> gameStateToString = new HashMap<>();
  private List<String> signLines;

  public SignManager(Main plugin) {
    this.plugin = plugin;
    gameStateToString.put(ArenaState.WAITING_FOR_PLAYERS, plugin.getChatManager().colorMessage("Signs.Game-States.Inactive"));
    gameStateToString.put(ArenaState.STARTING, plugin.getChatManager().colorMessage("Signs.Game-States.Starting"));
    gameStateToString.put(ArenaState.IN_GAME, plugin.getChatManager().colorMessage("Signs.Game-States.In-Game"));
    gameStateToString.put(ArenaState.ENDING, plugin.getChatManager().colorMessage("Signs.Game-States.Ending"));
    gameStateToString.put(ArenaState.RESTARTING, plugin.getChatManager().colorMessage("Signs.Game-States.Restarting"));
    signLines = LanguageManager.getLanguageList("Signs.Lines");
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    loadSigns();
    updateSignScheduler();
  }

  @EventHandler
  public void onSignChange(SignChangeEvent e) {
    if (!e.getPlayer().hasPermission("villagedefense.admin.sign.create")
        || !e.getLine(0).equalsIgnoreCase("[villagedefense]")) {
      return;
    }
    if (e.getLine(1).isEmpty()) {
      e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Signs.Please-Type-Arena-Name"));
      return;
    }
    for (Arena arena : ArenaRegistry.getArenas()) {
      if (!arena.getId().equalsIgnoreCase(e.getLine(1))) {
        continue;
      }
      for (int i = 0; i < signLines.size(); i++) {
        e.setLine(i, formatSign(signLines.get(i), arena));
      }
      loadedSigns.put((Sign) e.getBlock().getState(), arena);
      e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Signs.Sign-Created"));
      String location = e.getBlock().getWorld().getName() + "," + e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ() + ",0.0,0.0";
      List<String> locs = ConfigUtils.getConfig(plugin, "arenas").getStringList("instances." + arena.getId() + ".signs");
      locs.add(location);
      FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
      config.set("instances." + arena.getId() + ".signs", locs);
      ConfigUtils.saveConfig(plugin, config, "arenas");
      return;
    }
    e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Signs.Arena-Doesnt-Exists"));
  }

  private String formatSign(String msg, Arena a) {
    String formatted = msg;
    formatted = StringUtils.replace(formatted, "%mapname%", a.getMapName());
    if (a.getPlayers().size() >= a.getMaximumPlayers()) {
      formatted = StringUtils.replace(formatted, "%state%", plugin.getChatManager().colorMessage("Signs.Game-States.Full-Game"));
    } else {
      formatted = StringUtils.replace(formatted, "%state%", gameStateToString.get(a.getArenaState()));
    }
    formatted = StringUtils.replace(formatted, "%playersize%", String.valueOf(a.getPlayers().size()));
    formatted = StringUtils.replace(formatted, "%maxplayers%", String.valueOf(a.getMaximumPlayers()));
    formatted = plugin.getChatManager().colorRawMessage(formatted);
    return formatted;
  }

  @EventHandler
  public void onSignDestroy(BlockBreakEvent e) {
    if (!e.getPlayer().hasPermission("villagedefense.admin.sign.break")
        || loadedSigns.get(e.getBlock().getState()) == null) {
      return;
    }
    loadedSigns.remove(e.getBlock().getState());
    String location = e.getBlock().getWorld().getName() + "," + e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ() + "," + "0.0,0.0";
    for (String arena : ConfigUtils.getConfig(plugin, "arenas").getConfigurationSection("instances").getKeys(false)) {
      for (String sign : ConfigUtils.getConfig(plugin, "arenas").getStringList("instances." + arena + ".signs")) {
        if (!sign.equals(location)) {
          continue;
        }
        List<String> signs = ConfigUtils.getConfig(plugin, "arenas").getStringList("instances." + arena + ".signs");
        signs.remove(location);
        FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
        config.set(arena + ".signs", signs);
        ConfigUtils.saveConfig(plugin, config, "arenas");
        e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Signs.Sign-Removed"));
        return;
      }
    }
    e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + ChatColor.RED + "Couldn't remove sign from configuration! Please do this manually!");
  }

  @EventHandler
  public void onJoinAttempt(PlayerInteractEvent e) {
    if (e.getAction() == Action.RIGHT_CLICK_BLOCK
        && e.getClickedBlock().getState() instanceof Sign && loadedSigns.containsKey(e.getClickedBlock().getState())) {

      Arena arena = loadedSigns.get(e.getClickedBlock().getState());
      if (arena == null) {
        return;
      }
      if (ArenaRegistry.isInArena(e.getPlayer())) {
        e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Already-Playing"));
        return;
      }
      if (!(arena.getPlayers().size() >= arena.getMaximumPlayers())) {
        ArenaManager.joinAttempt(e.getPlayer(), arena);
        return;
      }
      if (PermissionsManager.isPremium(e.getPlayer()) || e.getPlayer().hasPermission(PermissionsManager.getJoinFullGames())) {
        for (Player player : arena.getPlayers()) {
          if (PermissionsManager.isPremium(player) || player.hasPermission(PermissionsManager.getJoinFullGames())) {
            continue;
          }
          if (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
            ArenaManager.leaveAttempt(player, arena);
            player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.You-Were-Kicked-For-Premium-Slot"));
            plugin.getChatManager().broadcast(arena, plugin.getChatManager().formatMessage(arena, plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Kicked-For-Premium-Slot"), player));
            ArenaManager.joinAttempt(e.getPlayer(), arena);
            return;
          } else {
            ArenaManager.joinAttempt(e.getPlayer(), arena);
            return;
          }
        }
        e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.No-Slots-For-Premium"));
      } else {
        e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Full-Game-No-Permission"));
      }
    }
  }

  public void loadSigns() {
    loadedSigns.clear();
    if (!ConfigUtils.getConfig(plugin, "arenas").contains("instances")) {
      Debugger.debug(LogLevel.WARN, "Arena instances not found. Signs not loaded");
      return;
    }

    for (String path : ConfigUtils.getConfig(plugin, "arenas").getConfigurationSection("instances").getKeys(false)) {
      for (String sign : ConfigUtils.getConfig(plugin, "arenas").getStringList("instances." + path + ".signs")) {
        Location loc = LocationUtils.getLocation(sign);
        if (loc.getBlock().getState() instanceof Sign) {
          loadedSigns.put((Sign) loc.getBlock().getState(), ArenaRegistry.getArena(path));
        } else {
          Debugger.debug(LogLevel.WARN, "Block at loc " + LocationUtils.locationToString(loc) + " for arena " + path + " not a sign");
        }
      }
    }
  }

  private void updateSignScheduler() {
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (Map.Entry<Sign, Arena> entry : loadedSigns.entrySet()) {
        Sign sign = entry.getKey();
        for (int i = 0; i < signLines.size(); i++) {
          sign.setLine(i, formatSign(signLines.get(i), entry.getValue()));
        }
        if (plugin.getConfig().getBoolean("Signs-Block-States-Enabled", true)) {
          Block behind = sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
          switch (entry.getValue().getArenaState()) {
            case WAITING_FOR_PLAYERS:
              behind.setType(XMaterial.WHITE_STAINED_GLASS.parseMaterial());
              if (plugin.is1_11_R1() || plugin.is1_12_R1()) {
                behind.setData((byte) 0);
              }
              break;
            case STARTING:
              behind.setType(XMaterial.YELLOW_STAINED_GLASS.parseMaterial());
              if (plugin.is1_11_R1() || plugin.is1_12_R1()) {
                behind.setData((byte) 4);
              }
              break;
            case IN_GAME:
              behind.setType(XMaterial.ORANGE_STAINED_GLASS.parseMaterial());
              if (plugin.is1_11_R1() || plugin.is1_12_R1()) {
                behind.setData((byte) 1);
              }
              break;
            case ENDING:
              behind.setType(XMaterial.GRAY_STAINED_GLASS.parseMaterial());
              if (plugin.is1_11_R1() || plugin.is1_12_R1()) {
                behind.setData((byte) 7);
              }
              break;
            case RESTARTING:
              behind.setType(XMaterial.BLACK_STAINED_GLASS.parseMaterial());
              if (plugin.is1_11_R1() || plugin.is1_12_R1()) {
                behind.setData((byte) 15);
              }
              break;
            default:
              break;
          }
        }
        sign.update();
      }
    }, 10, 10);
  }

  public Map<Sign, Arena> getLoadedSigns() {
    return loadedSigns;
  }
}
