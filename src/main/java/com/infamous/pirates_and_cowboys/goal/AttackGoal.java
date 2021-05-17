package com.infamous.pirates_and_cowboys.goal;

import com.infamous.pirates_and_cowboys.RangedMobHelper;
import com.infamous.pirates_and_cowboys.entity.IMountUser;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;

public class AttackGoal<T extends CreatureEntity> extends net.minecraft.entity.ai.goal.MeleeAttackGoal {

        public AttackGoal(T creatureEntity, double speedIn, boolean useLongMemory) {
            super(creatureEntity, speedIn, useLongMemory);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !RangedMobHelper.isRanged(this.mob);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !RangedMobHelper.isRanged(this.mob);
        }

        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return IMountUser.getAttackReachSquared(this.mob, attackTarget);
        }
    }