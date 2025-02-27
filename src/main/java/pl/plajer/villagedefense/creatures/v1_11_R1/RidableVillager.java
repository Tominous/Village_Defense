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

package pl.plajer.villagedefense.creatures.v1_11_R1;

import java.util.LinkedHashSet;
import java.util.Random;

import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityAgeable;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.EntityInsentient;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.EntityVillager;
import net.minecraft.server.v1_11_R1.EntityZombie;
import net.minecraft.server.v1_11_R1.Navigation;
import net.minecraft.server.v1_11_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_11_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_11_R1.PathfinderGoalInteract;
import net.minecraft.server.v1_11_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_11_R1.PathfinderGoalLookAtTradingPlayer;
import net.minecraft.server.v1_11_R1.PathfinderGoalMakeLove;
import net.minecraft.server.v1_11_R1.PathfinderGoalMoveIndoors;
import net.minecraft.server.v1_11_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_11_R1.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_11_R1.PathfinderGoalPlay;
import net.minecraft.server.v1_11_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_11_R1.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_11_R1.PathfinderGoalSelector;
import net.minecraft.server.v1_11_R1.PathfinderGoalTradeWithPlayer;
import net.minecraft.server.v1_11_R1.World;

import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;

import pl.plajer.villagedefense.creatures.CreatureUtils;

/**
 * Created by Tom on 15/08/2014.
 */
public class RidableVillager extends EntityVillager {

  public RidableVillager(org.bukkit.World world) {
    this(((CraftWorld) world).getHandle());
  }

  public RidableVillager(World world) {
    super(world);

    LinkedHashSet goalB = (LinkedHashSet) CreatureUtils.getPrivateField("b", PathfinderGoalSelector.class, goalSelector);
    goalB.clear();
    LinkedHashSet goalC = (LinkedHashSet) CreatureUtils.getPrivateField("c", PathfinderGoalSelector.class, goalSelector);
    goalC.clear();
    LinkedHashSet targetB = (LinkedHashSet) CreatureUtils.getPrivateField("b", PathfinderGoalSelector.class, targetSelector);
    targetB.clear();
    LinkedHashSet targetC = (LinkedHashSet) CreatureUtils.getPrivateField("c", PathfinderGoalSelector.class, targetSelector);
    targetC.clear();

    this.setSize(0.6F, 1.8F);
    ((Navigation) getNavigation()).b(true);
    ((Navigation) getNavigation()).a(true);
    this.goalSelector.a(0, new PathfinderGoalFloat(this));
    this.goalSelector.a(1, new PathfinderGoalAvoidTarget<>(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
    this.goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
    this.goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
    this.goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
    this.goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
    this.goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
    this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.6D));
    this.goalSelector.a(6, new PathfinderGoalMakeLove(this));
    this.goalSelector.a(8, new PathfinderGoalPlay(this, 0.32D));
    this.goalSelector.a(9, new PathfinderGoalInteract(this, EntityHuman.class, 3.0F, 1.0F));
    this.goalSelector.a(9, new PathfinderGoalInteract(this, EntityVillager.class, 5.0F, 0.02F));
    this.goalSelector.a(9, new PathfinderGoalRandomStroll(this, 0.6D));
    this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
    this.setCustomName(CreatureUtils.getVillagerNames()[new Random().nextInt(CreatureUtils.getVillagerNames().length)]);
    this.setCustomNameVisible(true);
  }

  @Override
  public void g(float f, float f1) {
    EntityLiving entityliving = (EntityLiving) bw();
    if (entityliving == null) {
      // search first human passenger
      for (final Entity e : passengers) {
        if (e instanceof EntityHuman) {
          entityliving = (EntityLiving) e;
          break;
        }
      }
      if (entityliving == null) {
        this.l((float) 0.12);
        super.g(f, f1);
        return;
      }
    }
    this.lastYaw = this.yaw = entityliving.yaw;
    this.pitch = entityliving.pitch * 0.5F;
    this.setYawPitch(this.yaw, this.pitch);
    this.aQ = this.aO = this.yaw;
    f = entityliving.be * 0.75F;
    f1 = entityliving.bf;
    if (f1 <= 0.0f) {
      f1 *= 0.25F;
    }
    this.l((float) 0.12);
    super.g(f, f1);
    P = (float) 1.0;
  }

  @Override
  public EntityAgeable createChild(EntityAgeable entityAgeable) {
    return this.b(entityAgeable);
  }


}
