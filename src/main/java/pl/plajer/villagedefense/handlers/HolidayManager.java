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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import pl.plajer.villagedefense.Main;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.handlers.powerup.Powerup;
import pl.plajer.villagedefense.utils.Utils;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 14.10.2018
 */
public class HolidayManager implements Listener {

  private HolidayType currentHoliday = HolidayType.NONE;
  private Random rand;
  private boolean enabled = true;
  private Main plugin;

  public HolidayManager(Main plugin) {
    if (!plugin.getConfig().getBoolean("Holidays-Enabled", true)) {
      enabled = false;
      return;
    }
    this.plugin = plugin;
    rand = new Random();
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    LocalDateTime time = LocalDateTime.now();

    int day = time.getDayOfMonth();
    int month = time.getMonthValue();

    switch (month) {
      case 2:
        if (day >= 10 && day <= 18) {
          currentHoliday = HolidayType.VALENTINES_DAY;
          Powerup powerup = new Powerup("VALENTINES_HEALING", plugin.getChatManager().colorRawMessage("&c&l<3"),
              plugin.getChatManager().colorRawMessage("&d&lHappy Valentine's Day!"), XMaterial.POPPY, pickup -> {
            pickup.getPlayer().setHealth(pickup.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            pickup.getPlayer().sendTitle(pickup.getPowerup().getDescription(), null, 5, 30, 5);
          });
          plugin.getPowerupRegistry().registerPowerup(powerup);
        }
        break;
      case 10:
        //4 days before halloween
        if (31 - day <= 4) {
          currentHoliday = HolidayType.HALLOWEEN;
        }
        break;
      case 11:
        //4 days after halloween
        if (day <= 4) {
          currentHoliday = HolidayType.HALLOWEEN;
        }
        break;
      default:
        break;
    }
  }

  public HolidayType getCurrentHoliday() {
    return currentHoliday;
  }

  /**
   * Applies holiday effects for zombies based on current holiday
   * eg. pumpkin heads on halloween
   *
   * @param zombie entity to apply effects for
   */
  public void applyHolidayZombieEffects(Zombie zombie) {
    if (!enabled) {
      return;
    }
    switch (currentHoliday) {
      case HALLOWEEN:
        if (zombie.getEquipment().getHelmet() == null) {
          //randomizing head type
          if (rand.nextBoolean()) {
            zombie.getEquipment().setHelmet(new ItemStack(Material.JACK_O_LANTERN, 1));
          } else {
            zombie.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN, 1));
          }
        }
        break;
      case NONE:
        break;
      default:
        break;
    }
  }

  /**
   * Applies holiday death effects for entities based on current holiday
   * eg. scary sound and optional bats when entity dies on halloween
   *
   * @param en entity to apply effects for
   */
  public void applyHolidayDeathEffects(Entity en) {
    if (!enabled) {
      return;
    }
    switch (currentHoliday) {
      case HALLOWEEN:
        en.getWorld().strikeLightningEffect(en.getLocation());
        //randomizing sound
        if (rand.nextBoolean()) {
          Utils.playSound(en.getLocation(), "ENTITY_WOLF_HOWL", "ENTITY_WOLF_HOWL");
        } else {
          Utils.playSound(en.getLocation(), "ENTITY_WITHER_DEATH", "ENTITY_WITHER_DEATH");
        }
        //randomizing bats spawn chance
        if (rand.nextBoolean()) {
          final List<Entity> bats = new ArrayList<>();
          for (int i = 0; i < rand.nextInt(6); i++) {
            final Entity bat = en.getWorld().spawnEntity(en.getLocation(), EntityType.BAT);
            bat.setCustomName(plugin.getChatManager().colorRawMessage("&6Halloween!"));
            bats.add(bat);
          }
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Entity bat : bats) {
              bat.getWorld().playEffect(bat.getLocation(), Effect.SMOKE, 3);
              bat.remove();
            }
            bats.clear();
          }, 30);
        }
        break;
      case NONE:
        break;
      default:
        break;
    }
  }

  @EventHandler
  public void onBatDamage(EntityDamageEvent e) {
    if (e.getEntityType().equals(EntityType.BAT) || (e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equals(plugin.getChatManager().colorRawMessage("&6Halloween!")))) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onCupidArrowShot(EntityShootBowEvent e) {
    if (!(e.getEntity() instanceof Player) || ArenaRegistry.getArena((Player) e.getEntity()) == null) {
      return;
    }
    if (currentHoliday != HolidayType.VALENTINES_DAY) {
      return;
    }
    Entity en = e.getProjectile();
    new BukkitRunnable() {
      @Override
      public void run() {
        if (en == null || en.isOnGround() || en.isDead()) {
          this.cancel();
          return;
        }
        en.getLocation().getWorld().spawnParticle(Particle.HEART, en.getLocation(), 1, 0, 0, 0, 1);
      }
    }.runTaskTimer(plugin, 1, 1);
  }

  public enum HolidayType {
    HALLOWEEN, NONE, VALENTINES_DAY
  }

}
