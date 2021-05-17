package com.infamous.pirates_and_cowboys.goal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public class BetterLookAtGoal extends LookAtGoal {
    protected final EntityPredicate lookAtPredicate;

    public BetterLookAtGoal(MobEntity entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance, float chance) {
        super(entityIn, watchTargetClass, maxDistance, chance);
        this.lookAtPredicate = (new EntityPredicate())
                .range((double)maxDistance)
                .allowSameTeam()
                .allowInvulnerable()
                .allowNonAttackable()
                .selector((target) -> notRiddenBy(entityIn).test(target));
    }

    public BetterLookAtGoal(MobEntity entityIn, Class<? extends LivingEntity> watchTargetClass, float maxDistance) {
        super(entityIn, watchTargetClass, maxDistance);
        this.lookAtPredicate = (new EntityPredicate())
                .range((double)maxDistance)
                .allowSameTeam()
                .allowInvulnerable()
                .allowNonAttackable()
                .selector((target) -> notRiddenBy(entityIn).test(target));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            if (this.mob.getTarget() != null) {
                this.lookAt = this.mob.getTarget();
            }

            if (this.lookAtType == PlayerEntity.class) {
                this.lookAt = this.mob.level.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            } else {
                this.lookAt = this.mob.level.getNearestLoadedEntity(this.lookAtType, this.lookAtPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0D, (double)this.lookDistance));
            }

            return this.lookAt != null;
        }
    }

    public static Predicate<Entity> notRiddenBy(Entity entityIn) {
        return (target) -> {
            while(true) {
                if (target != null && target.isVehicle()) {
                    target = target.getControllingPassenger();
                    if (target != entityIn) {
                        continue;
                    }

                    return false;
                }

                return true;
            }
        };
    }
}
