package com.infamous.pirates_and_cowboys.goal;

import java.util.EnumSet;

import com.infamous.pirates_and_cowboys.RangedMobHelper;
import com.infamous.pirates_and_cowboys.entity.IBowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.BowItem;

public class ShootBowGoal<T extends MonsterEntity & IBowUser> extends Goal {
   private final T bowUser;
   private final double moveSpeedAmp;
   private int attackCooldown;
   private final float maxAttackDistance;
   private int attackTime = -1;
   private int seeTime;
   private boolean strafingClockwise;
   private boolean strafingBackwards;
   private int strafingTime = -1;

   public ShootBowGoal(T bowUser, double moveSpeedAmpIn, int attackCooldownIn, float maxAttackDistanceIn) {
      this.bowUser = bowUser;
      this.moveSpeedAmp = moveSpeedAmpIn;
      this.attackCooldown = attackCooldownIn;
      this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   public void setAttackCooldown(int attackCooldownIn) {
      this.attackCooldown = attackCooldownIn;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.hasValidTarget() && RangedMobHelper.shouldUseBow(this.bowUser);
   }


   private boolean hasValidTarget() {
      return this.bowUser.getTarget() != null && this.bowUser.getTarget().isAlive();
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return (this.canUse() || !this.bowUser.getNavigation().isDone()) && RangedMobHelper.shouldUseBow(this.bowUser);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.bowUser.setAggressive(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.bowUser.setAggressive(false);
      this.seeTime = 0;
      this.attackTime = -1;
      this.bowUser.stopUsingItem();
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      LivingEntity attackTarget = this.bowUser.getTarget();
      if (attackTarget != null) {
         double distanceSqToTarget = this.bowUser.distanceToSqr(attackTarget.getX(), attackTarget.getY(), attackTarget.getZ());
         boolean canSeeTarget = this.bowUser.getSensing().canSee(attackTarget);
         boolean isSeeing = this.seeTime > 0;
         if (canSeeTarget != isSeeing) {
            this.seeTime = 0;
         }

         if (canSeeTarget) {
            ++this.seeTime;
         } else {
            --this.seeTime;
         }

         if (!(distanceSqToTarget > (double)this.maxAttackDistance) && this.seeTime >= 20) {
            this.bowUser.getNavigation().stop();
            ++this.strafingTime;
         } else {
            this.bowUser.getNavigation().moveTo(attackTarget, this.moveSpeedAmp);
            this.strafingTime = -1;
         }

         if (this.strafingTime >= 20) {
            if ((double)this.bowUser.getRandom().nextFloat() < 0.3D) {
               this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.bowUser.getRandom().nextFloat() < 0.3D) {
               this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
         }

         if (this.strafingTime > -1) {
            if (distanceSqToTarget > (double)(this.maxAttackDistance * 0.75F)) {
               this.strafingBackwards = false;
            } else if (distanceSqToTarget < (double)(this.maxAttackDistance * 0.25F)) {
               this.strafingBackwards = true;
            }

            this.strafe();
            this.bowUser.lookAt(attackTarget, 30.0F, 30.0F);
         } else {
            this.bowUser.getLookControl().setLookAt(attackTarget, 30.0F, 30.0F);
         }

         if (this.bowUser.isUsingItem()) {
            if (!canSeeTarget && this.seeTime < -60) {
               this.bowUser.stopUsingItem();
            } else if (canSeeTarget) {
               int i = this.bowUser.getTicksUsingItem();
               if (i >= 20) {
                  this.bowUser.stopUsingItem();
                  this.bowUser.performRangedAttack(attackTarget, BowItem.getPowerForTime(i));
                  this.attackTime = this.attackCooldown;
               }
            }
         } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.bowUser.startUsingItem(RangedMobHelper.getHandWithBow(this.bowUser));
         }

      }
   }

   protected void strafe() {
      this.bowUser.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
   }

}