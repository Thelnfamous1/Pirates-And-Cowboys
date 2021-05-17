package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.RangedMobHelper;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface IModCrossbowUser extends ICrossbowUser {

    @Override
    default void performCrossbowAttack(LivingEntity crossbowUser, float projectileVelocity) {
        Hand handWithCrossbow = RangedMobHelper.getHandWithCrossbow(crossbowUser);
        ItemStack crossbowStack = crossbowUser.getItemInHand(handWithCrossbow);
        if (RangedMobHelper.isHoldingCrossbow(crossbowUser)) {
            CrossbowItem.performShooting(crossbowUser.level, crossbowUser, handWithCrossbow, crossbowStack, projectileVelocity, (float)(14 - crossbowUser.level.getDifficulty().getId() * 4));
        }

        this.onCrossbowAttackPerformed();
    }

    boolean isCharging();
}
