package com.infamous.pirates_and_cowboys.goal;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class FollowMobOwnerGoal extends Goal {
   private final TameableEntity tameable;
   private MobEntity owner;
   private final IWorldReader world;
   private final double followSpeed;
   private final PathNavigator navigator;
   private int timeToRecalcPath;
   private final float maxDist;
   private final float minDist;
   private float oldWaterCost;
   private final boolean teleportToLeaves;

   public FollowMobOwnerGoal(TameableEntity tameable, double speed, float minDist, float maxDist, boolean teleportToLeaves) {
      this.tameable = tameable;
      this.world = tameable.level;
      this.followSpeed = speed;
      this.navigator = tameable.getNavigation();
      this.minDist = minDist;
      this.maxDist = maxDist;
      this.teleportToLeaves = teleportToLeaves;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      if (!(tameable.getNavigation() instanceof GroundPathNavigator) && !(tameable.getNavigation() instanceof FlyingPathNavigator)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   @Nullable
   private MobEntity getOwner(){
      UUID ownerId = this.tameable.getOwnerUUID();
      if(this.tameable.level instanceof ServerWorld && ownerId != null){
         Entity ownerEntity = ((ServerWorld) this.tameable.level).getEntity(ownerId);
         if(ownerEntity instanceof MobEntity){
            return (MobEntity) ownerEntity;
         }
         else return null;
      }
      return null;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      MobEntity owner = this.getOwner();
      if (owner != null && this.tameable.isInSittingPose()) {
         this.tameable.setOrderedToSit(false);
      }
      if (owner == null) {
         return false;
      } else if (owner.isSpectator()) {
         return false;
      } else if (this.tameable.distanceToSqr(owner) < (double)(this.minDist * this.minDist)) {
         return false;
      } else {
         this.owner = owner;
         return true;
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      if (this.owner != null && this.tameable.isInSittingPose()) {
         this.tameable.setOrderedToSit(false);
      }
      if(this.owner == null){
         return false;
      }
      if (this.navigator.isDone()) {
         return false;
      } else {
         return !(this.tameable.distanceToSqr(this.owner) <= (double)(this.maxDist * this.maxDist));
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.tameable.getPathfindingMalus(PathNodeType.WATER);
      this.tameable.setPathfindingMalus(PathNodeType.WATER, 0.0F);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void resetTask() {
      this.owner = null;
      this.navigator.stop();
      this.tameable.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.tameable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tameable.getMaxHeadXRot());
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         if (!this.tameable.isLeashed() && !this.tameable.isPassenger()) {
            if (this.tameable.distanceToSqr(this.owner) >= 144.0D) {
               this.tryToTeleportNearEntity();
            } else {
               this.navigator.moveTo(this.owner, this.followSpeed);
            }

         }
      }
   }

   private void tryToTeleportNearEntity() {
      BlockPos blockpos = this.owner.blockPosition();

      for(int i = 0; i < 10; ++i) {
         int j = this.getRandomNumber(-3, 3);
         int k = this.getRandomNumber(-1, 1);
         int l = this.getRandomNumber(-3, 3);
         boolean flag = this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
         if (flag) {
            return;
         }
      }

   }

   private boolean tryToTeleportToLocation(int x, int y, int z) {
      if (Math.abs((double)x - this.owner.getX()) < 2.0D && Math.abs((double)z - this.owner.getZ()) < 2.0D) {
         return false;
      } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
         return false;
      } else {
         this.tameable.moveTo((double)x + 0.5D, (double)y, (double)z + 0.5D, this.tameable.yRot, this.tameable.xRot);
         this.navigator.stop();
         return true;
      }
   }

   private boolean isTeleportFriendlyBlock(BlockPos pos) {
      PathNodeType pathnodetype = WalkNodeProcessor.getBlockPathTypeStatic(this.world, pos.mutable());
      if (pathnodetype != PathNodeType.WALKABLE) {
         return false;
      } else {
         BlockState blockstate = this.world.getBlockState(pos.below());
         if (!this.teleportToLeaves && blockstate.getBlock() instanceof LeavesBlock) {
            return false;
         } else {
            BlockPos blockpos = pos.subtract(this.tameable.blockPosition());
            return this.world.noCollision(this.tameable, this.tameable.getBoundingBox().move(blockpos));
         }
      }
   }

   private int getRandomNumber(int min, int max) {
      return this.tameable.getRandom().nextInt(max - min + 1) + min;
   }
}