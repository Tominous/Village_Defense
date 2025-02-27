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

package pl.plajer.villagedefense.api.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import pl.plajer.villagedefense.api.event.VillageEvent;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.creatures.upgrades.Upgrade;

/**
 * @author Plajer
 * @since 4.0.0
 * <p>
 * Called when player upgrades an entity.
 */
public class VillagePlayerEntityUpgradeEvent extends VillageEvent {

  private static final HandlerList HANDLERS = new HandlerList();
  private Entity entity;
  private Player player;
  private Upgrade appliedUpgrade;
  private int tier;

  public VillagePlayerEntityUpgradeEvent(Arena eventArena, Entity entity, Player player, Upgrade appliedUpgrade, int tier) {
    super(eventArena);
    this.entity = entity;
    this.player = player;
    this.appliedUpgrade = appliedUpgrade;
    this.tier = tier;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public Entity getEntity() {
    return entity;
  }

  /**
   * @return upgrade that was applied to entity
   */
  public Upgrade getAppliedUpgrade() {
    return appliedUpgrade;
  }

  /**
   * @return upgrade tier
   */
  public int getTier() {
    return tier;
  }

  public Player getPlayer() {
    return player;
  }

}
