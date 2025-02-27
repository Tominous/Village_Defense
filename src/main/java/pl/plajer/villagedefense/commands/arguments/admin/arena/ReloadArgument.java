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

package pl.plajer.villagedefense.commands.arguments.admin.arena;

import org.bukkit.command.CommandSender;

import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaManager;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.commands.arguments.ArgumentsRegistry;
import pl.plajer.villagedefense.commands.arguments.data.CommandArgument;
import pl.plajer.villagedefense.commands.arguments.data.LabelData;
import pl.plajer.villagedefense.commands.arguments.data.LabeledCommandArgument;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class ReloadArgument {

  public ReloadArgument(ArgumentsRegistry registry) {
    registry.mapArgument("villagedefenseadmin", new LabeledCommandArgument("reload", "villagedefense.admin.reload", CommandArgument.ExecutorType.BOTH,
        new LabelData("/vda reload", "/vda reload", "&7Reload all game arenas\n&7&lThey will be stopped!\n"
            + "&c&lNot recommended!\n&6Permission: &7villagedefense.admin.reload")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        for (Arena arena : ArenaRegistry.getArenas()) {
          ArenaManager.stopGame(true, arena);
        }
        ArenaRegistry.registerArenas();
        sender.sendMessage(registry.getPlugin().getChatManager().getPrefix() + registry.getPlugin().getChatManager().colorMessage("Commands.Admin-Commands.Success-Reload"));
      }
    });
  }

}
