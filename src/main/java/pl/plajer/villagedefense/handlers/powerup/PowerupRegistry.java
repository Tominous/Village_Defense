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

package pl.plajer.villagedefense.handlers.powerup;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.api.event.player.VillagePlayerPowerupPickupEvent;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.arena.ArenaUtils;
import pl.plajer.villagedefense.handlers.ChatManager;
import pl.plajerlair.core.debug.Debugger;
import pl.plajerlair.core.debug.LogLevel;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 15.01.2019
 */
public class PowerupRegistry {

  private List<Powerup> registeredPowerups = new ArrayList<>();
  private boolean enabled = false;
  private Main plugin;

  public PowerupRegistry(Main plugin) {
    if (!plugin.getConfig().getBoolean("Powerups.Enabled", true)) {
      return;
    }
    if (!plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
      Debugger.debug(LogLevel.WARN, "Power up module: Holographic Displays dependency not found, disabling");
      return;
    }
    enabled = true;
    this.plugin = plugin;
    Debugger.debug(LogLevel.INFO, "Registering power ups module!");
    registerPowerups();
    if (registeredPowerups.isEmpty()) {
      Debugger.debug(LogLevel.WARN, "Disabling power up module, all power ups disabled");
      enabled = false;
    }
  }

  private void registerPowerups() {
    ChatManager chatManager = plugin.getChatManager();
    registerPowerup(new Powerup("MAP_CLEAN", chatManager.colorMessage("Powerups.Map-Clean-Powerup.Name"),
        chatManager.colorMessage("Powerups.Map-Clean-Powerup.Description"), XMaterial.BLAZE_POWDER, pickup -> {
      if (pickup.getArena().getZombies() != null) {
        ArenaUtils.removeSpawnedZombies(pickup.getArena());
        pickup.getArena().getZombies().clear();
      }

      for (Player p : pickup.getArena().getPlayers()) {
        p.sendTitle(pickup.getPowerup().getName(), pickup.getPowerup().getDescription(), 5, 30, 5);
      }
    }));
    registerPowerup(new Powerup("DOUBLE_DAMAGE", chatManager.colorMessage("Powerups.Double-Damage-Powerup.Name"),
        chatManager.colorMessage("Powerups.Double-Damage-Powerup.Description"), XMaterial.REDSTONE, pickup -> {
      for (Player p : pickup.getArena().getPlayers()) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20
            * plugin.getConfig().getInt("Powerups.List.Double-Damage-For-Players.Time", 15), 0, false, false));
      }

      String subTitle = pickup.getPowerup().getDescription();
      subTitle = StringUtils.replace(subTitle, "%time%", plugin.getConfig().getString("Powerups.List.Double-Damage-For-Players.Time", "15"));

      for (Player p : pickup.getArena().getPlayers()) {
        p.sendTitle(pickup.getPowerup().getName(), subTitle, 5, 30, 5);
      }
    }));
    registerPowerup(new Powerup("GOLEM_RAID", chatManager.colorMessage("Powerups.Golem-Raid-Powerup.Name"),
        chatManager.colorMessage("Powerups.Golem-Raid-Powerup.Description"), XMaterial.GOLDEN_APPLE, pickup -> {
      for (int i = 0; i < plugin.getConfig().getInt("Powerups.List.Golem-Raid.Golems-Amount", 3); i++) {
        pickup.getArena().spawnGolem(pickup.getArena().getStartLocation(), pickup.getPlayer());
      }

      for (Player p : pickup.getArena().getPlayers()) {
        p.sendTitle(pickup.getPowerup().getName(), pickup.getPowerup().getDescription(), 5, 30, 5);
      }
    }));
    registerPowerup(new Powerup("HEALING", chatManager.colorMessage("Powerups.Healing-Powerup.Name"),
        chatManager.colorMessage("Powerups.Healing-Powerup.Description"), XMaterial.IRON_INGOT, pickup -> {
      for (Player p : pickup.getArena().getPlayers()) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20
            * plugin.getConfig().getInt("Powerups.List.Healing-For-Players.Time-Of-Healing", 10), 0, false, false));
      }
      String subTitle = pickup.getPowerup().getDescription();
      subTitle = StringUtils.replace(subTitle, "%time%", plugin.getConfig().getString("Powerups.List.Healing-For-Players.Time-Of-Healing", "10"));

      for (Player p : pickup.getArena().getPlayers()) {
        p.sendTitle(pickup.getPowerup().getName(), subTitle, 5, 30, 5);
      }
    }));
    registerPowerup(new Powerup("ONE_SHOT_ONE_KILL", chatManager.colorMessage("Powerups.One-Shot-One-Kill-Powerup.Name"),
        chatManager.colorMessage("Powerups.One-Shot-One-Kill-Powerup.Description"), XMaterial.DIAMOND_SWORD, pickup -> {
      for (Player p : pickup.getArena().getPlayers()) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20
            * plugin.getConfig().getInt("Powerups.List.One-Shot-One-Kill.Time", 15), 255, false, false));
      }
      String subTitle = pickup.getPowerup().getDescription();
      subTitle = StringUtils.replace(subTitle, "%time%", plugin.getConfig().getString("Powerups.List.One-Shot-One-Kill.Time", "15"));

      for (Player p : pickup.getArena().getPlayers()) {
        p.sendTitle(pickup.getPowerup().getName(), subTitle, 5, 30, 5);
      }
    }));
  }

  /**
   * @return random powerup from list of registered ones
   */
  public Powerup getRandomPowerup() {
    Random r = new Random();
    return registeredPowerups.get(r.nextInt(registeredPowerups.size()));
  }

  public void spawnPowerup(Location loc, Arena arena) {
    if (!enabled) {
      return;
    }
    final Powerup powerup = getRandomPowerup();
    if (!(ThreadLocalRandom.current().nextDouble(0.0, 100.0)
        <= plugin.getConfig().getDouble("Powerups.Drop-Chance", 1.0))) {
      return;
    }

    final Hologram hologram = HologramsAPI.createHologram(plugin, loc.clone().add(0.0, 1.2, 0.0));
    hologram.appendTextLine(powerup.getName());
    ItemLine itemLine = hologram.appendItemLine(powerup.getMaterial().parseItem());
    itemLine.setPickupHandler(player -> {
      if (ArenaRegistry.getArena(player) != arena) {
        return;
      }
      VillagePlayerPowerupPickupEvent villagePowerupPickEvent = new VillagePlayerPowerupPickupEvent(arena, player, powerup);
      Bukkit.getPluginManager().callEvent(villagePowerupPickEvent);
      powerup.getOnPickup().accept(new PowerupPickupHandler(powerup, arena, player));
      hologram.delete();
    });
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!hologram.isDeleted()) {
        hologram.delete();
      }
    }, /* remove after 40 seconds to prevent staying even if arena is finished */ 20 * 40);
  }

  /**
   * Attempts to register a powerup
   *
   * @param powerup powerup to register
   * @throws IllegalArgumentException if power-up with same ID currently exist
   */
  public void registerPowerup(Powerup powerup) {
    for (Powerup pwup : registeredPowerups) {
      if (pwup.getId().equals(powerup.getId())) {
        throw new IllegalArgumentException("Cannot register new power-up with same ID!");
      }
    }
    registeredPowerups.add(powerup);
  }

  /**
   * Unregisters target powerup from registry
   *
   * @param powerup powerup to remove
   */
  public void unregisterPowerup(Powerup powerup) {
    registeredPowerups.remove(powerup);
  }

}
