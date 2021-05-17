package com.infamous.pirates_and_cowboys.goal;

import com.infamous.pirates_and_cowboys.RangedMobHelper;
import com.infamous.pirates_and_cowboys.entity.ITridentUser;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.RangedAttackGoal;

public class ThrowTridentGoal<T extends MobEntity & ITridentUser> extends RangedAttackGoal {
        private final T tridentUser;

        public ThrowTridentGoal(T tridentUser, double moveSpeed, int maxAttackTime, float maxAttackDistanceSq) {
            super(tridentUser, moveSpeed, maxAttackTime, maxAttackDistanceSq);
            this.tridentUser = tridentUser;
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return super.canUse() && RangedMobHelper.shouldUseTrident(this.tridentUser);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            super.start();
            this.tridentUser.setAggressive(true);
            this.tridentUser.startUsingItem(RangedMobHelper.getHandWithTrident(this.tridentUser));
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            super.stop();
            this.tridentUser.stopUsingItem();
            this.tridentUser.setAggressive(false);
        }
    }