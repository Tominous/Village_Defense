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

package pl.plajer.villagedefense.kits.premium;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.handlers.PermissionsManager;
import pl.plajer.villagedefense.kits.kitapi.KitRegistry;
import pl.plajer.villagedefense.kits.kitapi.basekits.PremiumKit;
import pl.plajer.villagedefense.user.User;
import pl.plajer.villagedefense.utils.ArmorHelper;
import pl.plajer.villagedefense.utils.Utils;
import pl.plajerlair.core.utils.ItemBuilder;
import pl.plajerlair.core.utils.XMaterial;

/**
 * @author Plajer
 * <p>
 * Created at 01.03.2018
 */
public class WizardKit extends PremiumKit implements Listener {

  private List<Player> wizardsOnDuty = new ArrayList<>();

  public WizardKit() {
    setName(getPlugin().getChatManager().colorMessage("Kits.Wizard.Kit-Name"));
    List<String> description = Utils.splitString(getPlugin().getChatManager().colorMessage("Kits.Wizard.Kit-Description"), 40);
    this.setDescription(description.toArray(new String[0]));
    KitRegistry.registerKit(this);
    getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
  }

  @Override
  public boolean isUnlockedByPlayer(Player player) {
    return PermissionsManager.isPremium(player) || player.hasPermission("villagedefense.kit.wizard");
  }

  @Override
  public void giveKitItems(Player player) {
    player.getInventory().addItem(new ItemBuilder(new ItemStack(Material.BLAZE_ROD))
        .name(getPlugin().getChatManager().colorMessage("Kits.Wizard.Staff-Item-Name"))
        .lore(Utils.splitString(getPlugin().getChatManager().colorMessage("Kits.Wizard.Staff-Item-Lore"), 40))
        .build());
    player.getInventory().addItem(new ItemBuilder(new ItemStack(XMaterial.INK_SAC.parseMaterial(), 4))
        .name(getPlugin().getChatManager().colorMessage("Kits.Wizard.Essence-Item-Name"))
        .lore(Utils.splitString(getPlugin().getChatManager().colorMessage("Kits.Wizard.Essence-Item-Lore"), 40))
        .build());

    ArmorHelper.setColouredArmor(Color.GRAY, player);
    player.getInventory().addItem(new ItemStack(Material.SADDLE));

  }

  @Override
  public Material getMaterial() {
    return Material.BLAZE_ROD;
  }

  @Override
  public void reStock(Player player) {
    player.getInventory().addItem(new ItemBuilder(new ItemStack(XMaterial.INK_SAC.parseMaterial()))
        .name(getPlugin().getChatManager().colorMessage("Kits.Wizard.Essence-Item-Name"))
        .lore(Utils.splitString(getPlugin().getChatManager().colorMessage("Kits.Wizard.Essence-Item-Lore"), 40))
        .build());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    wizardsOnDuty.remove(e.getPlayer());
  }

  @EventHandler
  public void onWizardDamage(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Zombie && e.getEntity() instanceof Player)) {
      return;
    }
    if (!wizardsOnDuty.contains(e.getEntity())) {
      return;
    }
    if (ArenaRegistry.getArena((Player) e.getEntity()) == null) {
      return;
    }
    ((Zombie) e.getDamager()).damage(2.0, e.getEntity());
  }

  //todo very complex code
  @EventHandler
  public void onStaffUse(PlayerInteractEvent e) {
    User user = getPlugin().getUserManager().getUser(e.getPlayer());
    if (ArenaRegistry.getArena(e.getPlayer()) == null) {
      return;
    }
    if (!(user.getKit() instanceof WizardKit)) {
      return;
    }
    final Player p = e.getPlayer();
    ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
    if (!Utils.isNamed(stack)) {
      return;
    }
    if (stack.getItemMeta().getDisplayName().equals(getPlugin().getChatManager().colorMessage("Kits.Wizard.Essence-Item-Name"))) {
      if (user.getCooldown("essence") > 0 && !user.isSpectator()) {
        String message = getPlugin().getChatManager().colorMessage("Kits.Ability-Still-On-Cooldown");
        message = message.replaceFirst("%COOLDOWN%", Long.toString(user.getCooldown("essence")));
        e.getPlayer().sendMessage(message);
        return;
      }
      wizardsOnDuty.add(p);
      if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() > (p.getHealth() + 3)) {
        p.setHealth(p.getHealth() + 3);
      } else {
        p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
      }
      if (stack.getAmount() <= 1) {
        p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
      } else {
        p.getInventory().getItemInMainHand().setAmount(stack.getAmount() - 1);
      }
      p.setGlowing(true);
      new BukkitRunnable() {
        @Override
        public void run() {
          Location loc = p.getLocation();
          loc.add(0, 0.8, 0);
          p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 5, 0, 0, 0, 0);
          if (!wizardsOnDuty.contains(p) || !ArenaRegistry.isInArena(p)) {
            this.cancel();
          }
        }
      }.runTaskTimer(getPlugin(), 0, 2);
      for (Entity en : p.getNearbyEntities(2, 2, 2)) {
        if (en instanceof Zombie) {
          ((Zombie) en).damage(9.0, p);
        }
      }
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
        p.setGlowing(false);
        wizardsOnDuty.remove(p);
      }, 20 * 15);
      user.setCooldown("essence", 15);
    } else if (stack.getItemMeta().getDisplayName().equals(getPlugin().getChatManager().colorMessage("Kits.Wizard.Staff-Item-Name"))) {
      if (user.isSpectator()) {
        e.getPlayer().sendMessage(getPlugin().getChatManager().colorMessage("Kits.Cleaner.Spectator-Warning"));
        return;
      }
      if (user.getCooldown("wizard_staff") > 0 && !user.isSpectator()) {
        String message = getPlugin().getChatManager().colorMessage("Kits.Ability-Still-On-Cooldown");
        message = message.replaceFirst("%COOLDOWN%", Long.toString(user.getCooldown("wizard_staff")));
        e.getPlayer().sendMessage(message);
        return;
      }
      new BukkitRunnable() {
        double positionModifier = 0;
        Location loc = p.getLocation();
        Vector direction = loc.getDirection().normalize();

        @Override
        public void run() {
          positionModifier += 0.5;
          double x = direction.getX() * positionModifier;
          double y = direction.getY() * positionModifier + 1.5;
          double z = direction.getZ() * positionModifier;
          loc.add(x, y, z);
          p.getWorld().spawnParticle(Particle.TOWN_AURA, loc, 5);
          for (Entity en : loc.getChunk().getEntities()) {
            if (!(en instanceof Zombie)) {
              continue;
            }
            if (en.getLocation().distance(loc) < 1.5) {
              if (!en.equals(p)) {
                ((LivingEntity) en).damage(6.0, p);
                en.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, en.getLocation(), 2, 0.5, 0.5, 0.5, 0);
              }
            }
          }
          loc.subtract(x, y, z);
          if (positionModifier > 40) {
            this.cancel();
          }
        }
      }.runTaskTimer(getPlugin(), 0, 1);
      user.setCooldown("wizard_staff", 1);
    }
  }
}
