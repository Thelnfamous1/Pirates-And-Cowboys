package com.infamous.pirates_and_cowboys.goal;

import com.infamous.pirates_and_cowboys.RangedMobHelper;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RangedInteger;

import java.util.EnumSet;

public class ShootCrossbowGoal<T extends MobEntity & ICrossbowUser> extends Goal {
   public static final RangedInteger PATHFINDING_DELAY_RANGE = new RangedInteger(20, 40);
   private final T crossbowUser;
   private ShootCrossbowGoal.CrossbowState crossbowState = ShootCrossbowGoal.CrossbowState.UNCHARGED;
   private final double speedModifier;
   private final float attackRadiusSq;
   private int seeTime;
   private int attackDelay;
   private int updatePathDelay;

   public ShootCrossbowGoal(T crossbowUser, double moveSpeedAmpIn, float maxAttackDistanceIn) {
      this.crossbowUser = crossbowUser;
      this.speedModifier = moveSpeedAmpIn;
      this.attackRadiusSq = maxAttackDistanceIn * maxAttackDistanceIn;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.hasValidTarget() && RangedMobHelper.shouldUseCrossbow(this.crossbowUser);
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.hasValidTarget() && (this.canUse() || !this.crossbowUser.getNavigation().isDone()) && RangedMobHelper.shouldUseCrossbow(this.crossbowUser);
   }

   private boolean hasValidTarget() {
      return this.crossbowUser.getTarget() != null && this.crossbowUser.getTarget().isAlive();
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.crossbowUser.setAggressive(false);
      this.crossbowUser.setTarget((LivingEntity)null);
      this.seeTime = 0;
      if (this.crossbowUser.isUsingItem()) {
         this.crossbowUser.stopUsingItem();
         this.crossbowUser.setChargingCrossbow(false);
         CrossbowItem.setCharged(this.crossbowUser.getUseItem(), false);
      }

   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      LivingEntity attackTarget = this.crossbowUser.getTarget();
      if (attackTarget != null) {
         boolean canSeeTarget = this.crossbowUser.getSensing().canSee(attackTarget);
         boolean flag1 = this.seeTime > 0;
         if (canSeeTarget != flag1) {
            this.seeTime = 0;
         }

         if (canSeeTarget) {
            ++this.seeTime;
         } else {
            --this.seeTime;
         }

         double distanceSqToTarget = this.crossbowUser.distanceToSqr(attackTarget);
         boolean cannotAttack = (distanceSqToTarget > (double)this.attackRadiusSq || this.seeTime < 5) && this.attackDelay == 0;
         if (cannotAttack) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
               this.crossbowUser.getNavigation().moveTo(attackTarget, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
               this.updatePathDelay = PATHFINDING_DELAY_RANGE.randomValue(this.crossbowUser.getRandom());
            }
         } else {
            this.updatePathDelay = 0;
            this.crossbowUser.getNavigation().stop();
         }

         this.crossbowUser.getLookControl().setLookAt(attackTarget, 30.0F, 30.0F);
         if (this.crossbowState == ShootCrossbowGoal.CrossbowState.UNCHARGED) {
            if (!cannotAttack) {
               this.crossbowUser.startUsingItem(RangedMobHelper.getHandWithCrossbow(this.crossbowUser));
               this.crossbowState = ShootCrossbowGoal.CrossbowState.CHARGING;
               this.crossbowUser.setChargingCrossbow(true);
            }
         } else if (this.crossbowState == ShootCrossbowGoal.CrossbowState.CHARGING) {
            if (!this.crossbowUser.isUsingItem()) {
               this.crossbowState = ShootCrossbowGoal.CrossbowState.UNCHARGED;
            }

            int i = this.crossbowUser.getTicksUsingItem();
            ItemStack activeItemStack = this.crossbowUser.getUseItem();
            if (i >= CrossbowItem.getChargeDuration(activeItemStack)) {
               this.crossbowUser.releaseUsingItem();
               this.crossbowState = ShootCrossbowGoal.CrossbowState.CHARGED;
               this.attackDelay = 20 + this.crossbowUser.getRandom().nextInt(20);
               this.crossbowUser.setChargingCrossbow(false);
            }
         } else if (this.crossbowState == ShootCrossbowGoal.CrossbowState.CHARGED) {
            --this.attackDelay;
            if (this.attackDelay == 0) {
               this.crossbowState = ShootCrossbowGoal.CrossbowState.READY_TO_ATTACK;
            }
         } else if (this.crossbowState == ShootCrossbowGoal.CrossbowState.READY_TO_ATTACK && canSeeTarget) {
            this.crossbowUser.performRangedAttack(attackTarget, 1.0F);
            ItemStack crossbowStack = this.crossbowUser.getItemInHand(RangedMobHelper.getHandWithCrossbow(this.crossbowUser));
            CrossbowItem.setCharged(crossbowStack, false);
            this.crossbowState = ShootCrossbowGoal.CrossbowState.UNCHARGED;
         }
      }
   }

   private boolean canRun() {
      return this.crossbowState == ShootCrossbowGoal.CrossbowState.UNCHARGED;
   }

   enum CrossbowState {
      UNCHARGED,
      CHARGING,
      CHARGED,
      READY_TO_ATTACK;
   }
}