package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.RangedMobHelper;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public interface IBowUser extends IRangedAttackMob {

    default ProjectileEntity createBowProjectile(World worldIn, LivingEntity bowUser){
        ItemStack ammoStack = bowUser.getProjectile(bowUser.getItemInHand(RangedMobHelper.getHandWithBow(bowUser)));
        boolean hasFireworkAmmo = ammoStack.getItem() instanceof FireworkRocketItem;
        ProjectileEntity projectileentity;
        if (hasFireworkAmmo) {
            projectileentity = new FireworkRocketEntity(worldIn, ammoStack, bowUser, bowUser.getX(), bowUser.getEyeY() - (double)0.15F, bowUser.getZ(), true);
        } else {
            ArrowItem arrowitem = (ArrowItem)(ammoStack.getItem() instanceof ArrowItem ? ammoStack.getItem() : Items.ARROW);
            projectileentity = (AbstractArrowEntity)arrowitem.createArrow(worldIn, ammoStack, bowUser);
        }
        return projectileentity;
    }

    default void shootBow(LivingEntity bowUser, LivingEntity targetEntity, ProjectileEntity projectileEntity, float projectileVelocity) {
        double xDifference = targetEntity.getX() - bowUser.getX();
        double yDifference = targetEntity.getY(0.3333333333333333D) - projectileEntity.getY();
        double zDifference = targetEntity.getZ() - bowUser.getZ();
        double horizontalDistanceSq = (double)MathHelper.sqrt(xDifference * xDifference + zDifference * zDifference);
        projectileEntity.shoot(xDifference, yDifference + horizontalDistanceSq * (double)0.2F, zDifference, projectileVelocity, (float)(14 - bowUser.level.getDifficulty().getId() * 4));
        bowUser.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (bowUser.getRandom().nextFloat() * 0.4F + 0.8F));
        bowUser.level.addFreshEntity(projectileEntity);
    }
}
