package com.infamous.pirates_and_cowboys.entity;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

public interface ITridentUser extends IRangedAttackMob {

    default void throwTrident(LivingEntity tridentUser, LivingEntity attackTarget, float projectileVelocity){
        TridentEntity tridententity = new TridentEntity(tridentUser.level, tridentUser, new ItemStack(Items.TRIDENT));
        double xDifference = attackTarget.getX() - tridentUser.getX();
        double yDifference = attackTarget.getY(0.3333333333333333D) - tridententity.getY();
        double zDifference = attackTarget.getZ() - tridentUser.getZ();
        double horizontalDistanceSq = (double) MathHelper.sqrt(xDifference * xDifference + zDifference * zDifference);
        tridententity.shoot(xDifference, yDifference + horizontalDistanceSq * (double)0.2F, zDifference, projectileVelocity, (float)(14 - tridentUser.level.getDifficulty().getId() * 4));
        tridentUser.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (tridentUser.getRandom().nextFloat() * 0.4F + 0.8F));
        tridentUser.level.addFreshEntity(tridententity);
    }

}
