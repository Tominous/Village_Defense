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

package pl.plajer.villagedefense.commands.arguments.admin;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;

import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.arena.ArenaUtils;
import pl.plajer.villagedefense.arena.options.ArenaOption;
import pl.plajer.villagedefense.commands.arguments.ArgumentsRegistry;
import pl.plajer.villagedefense.commands.arguments.data.CommandArgument;
import pl.plajer.villagedefense.commands.arguments.data.LabelData;
import pl.plajer.villagedefense.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.villagedefense.commands.completion.CompletableArgument;
import pl.plajer.villagedefense.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class ClearEntitiesArgument {

  public ClearEntitiesArgument(ArgumentsRegistry registry) {
    registry.getTabCompletion().registerCompletion(new CompletableArgument("villagedefenseadmin", "clear", Arrays.asList("zombie", "villager", "golem", "wolf")));
    registry.mapArgument("villagedefenseadmin", new LabeledCommandArgument("clear", "villagedefense.admin.clear", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/vda clear &6<zombie/villager/golem/wolf>", "/vda clear <mob>",
            "&7Clear specific mob type from arena you're in\n&7Valid mob types:\n&7• ZOMBIE - clear spawned zombies\n"
                + "&7• VILLAGER - clear alive villagers\n&7• GOLEM - clear spawned golems\n&7• WOLF - clear spawned wolves\n&6Permission: &7villagedefense.admin.clear")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (!Utils.checkIsInGameInstance((Player) sender)) {
          return;
        }
        if (args.length == 1) {
          sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + ChatColor.RED + "Please type valid mob type to clear: VILLAGER, ZOMBIE, IRON_GOLEM");
          return;
        }
        Arena arena = ArenaRegistry.getArena((Player) sender);
        String clearMessage;
        switch (args[1].toLowerCase()) {
          case "villager":
            if (arena.getVillagers() == null || arena.getVillagers().isEmpty()) {
              sender.sendMessage(registry.getPlugin().getChatManager().colorMessage("Kits.Cleaner.Nothing-To-Clean"));
              return;
            }
            for (Villager villager : arena.getVillagers()) {
              villager.getWorld().spawnParticle(Particle.LAVA, villager.getLocation(), 20);
              villager.remove();
            }
            arena.getVillagers().clear();
            Utils.playSound(((Player) sender).getLocation(), "ENTITY_VILLAGER_DEATH", "ENTITY_VILLAGER_DEATH");
            clearMessage = registry.getPlugin().getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Removed-Villagers");
            break;
          case "zombie":
            if (arena.getZombies() == null || arena.getZombies().isEmpty()) {
              sender.sendMessage(registry.getPlugin().getChatManager().colorMessage("Kits.Cleaner.Nothing-To-Clean"));
              return;
            }
            ArenaUtils.removeSpawnedZombies(arena);
            arena.getZombies().clear();
            arena.setOptionValue(ArenaOption.ZOMBIES_TO_SPAWN, 0);
            Utils.playSound(((Player) sender).getLocation(), "ENTITY_ZOMBIE_DEATH", "ENTITY_ZOMBIE_DEATH");
            clearMessage = registry.getPlugin().getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Removed-Zombies");
            break;
          case "golem":
            if (arena.getIronGolems() == null || arena.getIronGolems().isEmpty()) {
              sender.sendMessage(registry.getPlugin().getChatManager().colorMessage("Kits.Cleaner.Nothing-To-Clean"));
              return;
            }
            for (IronGolem golem : arena.getIronGolems()) {
              golem.getWorld().spawnParticle(Particle.LAVA, golem.getLocation(), 20);
              golem.remove();
            }
            arena.getIronGolems().clear();
            Utils.playSound(((Player) sender).getLocation(), "ENTITY_IRONGOLEM_DEATH", "ENTITY_IRON_GOLEM_DEATH");
            clearMessage = registry.getPlugin().getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Removed-Golems");
            break;
          case "wolf":
            if (arena.getWolfs() == null || arena.getWolfs().isEmpty()) {
              sender.sendMessage(registry.getPlugin().getChatManager().colorMessage("Kits.Cleaner.Nothing-To-Clean"));
              return;
            }
            for (Wolf wolf : arena.getWolfs()) {
              wolf.getWorld().spawnParticle(Particle.LAVA, wolf.getLocation(), 20);
              wolf.remove();
            }
            arena.getWolfs().clear();
            Utils.playSound(((Player) sender).getLocation(), "ENTITY_WOLF_DEATH", "ENTITY_WOLF_DEATH");
            clearMessage = registry.getPlugin().getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Removed-Wolves");
            break;
          default:
            sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + ChatColor.RED + "Please type valid mob type to clear: VILLAGER, ZOMBIE, GOLEM, WOLF");
            return;
        }
        String message = registry.getPlugin().getChatManager().formatMessage(arena, clearMessage, (Player) sender);
        for (Player loopPlayer : arena.getPlayers()) {
          loopPlayer.sendMessage(registry.getPlugin().getChatManager().getPrefix() + message);
        }
      }
    });
  }

}
