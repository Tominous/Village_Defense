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

package pl.plajer.villagedefense.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.villagedefense.Main;
import pl.plajerlair.core.utils.XMaterial;

/**
 * Bridge for compatibility between 1.12 and 1.13
 * for some materials.
 *
 * @author Plajer
 * <p>
 * Created at 11.01.2019
 */
public class CompatMaterialConstants {

  private static Main plugin = JavaPlugin.getPlugin(Main.class);
  public static final Material PLAYER_HEAD = plugin.is1_11_R1() || plugin.is1_12_R1() ? Material.SKULL_ITEM : XMaterial.PLAYER_HEAD.parseMaterial();
  public static final ItemStack PLAYER_HEAD_ITEM = plugin.is1_11_R1() || plugin.is1_12_R1() ? new ItemStack(Material.SKULL_ITEM, 1, (short) 3) : XMaterial.PLAYER_HEAD.parseItem();
  public static final Material OAK_DOOR_BLOCK = plugin.is1_11_R1() || plugin.is1_12_R1() ? Material.WOODEN_DOOR : XMaterial.OAK_DOOR.parseMaterial();
  public static final Material OAK_DOOR_ITEM = plugin.is1_11_R1() || plugin.is1_12_R1() ? Material.WOOD_DOOR : XMaterial.OAK_DOOR.parseMaterial();

}
