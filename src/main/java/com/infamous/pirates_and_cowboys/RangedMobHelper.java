package com.infamous.pirates_and_cowboys;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;

import java.util.function.Predicate;

public class RangedMobHelper {

    public static final Predicate<Item> crossbowItemPredicate = (item -> item instanceof CrossbowItem);
    public static final Predicate<Item> bowItemPredicate = (item -> item instanceof BowItem);
    public static final Predicate<Item> tridentItemPredicate = (item -> item instanceof TridentItem);
    public static final Predicate<Item> shootableItemPredicate = (item -> item instanceof ShootableItem);
    public static final Predicate<Item> rangedItemPredicate = (item) ->
            shootableItemPredicate.test(item) || tridentItemPredicate.test(item);

    @SuppressWarnings("WeakerAccess")
    public static Hand getHandWithItem(LivingEntity livingEntity, Predicate<Item> itemPredicate) {
        return itemPredicate.test(livingEntity.getMainHandItem().getItem()) ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public static Hand getHandWithBow(LivingEntity livingEntity) {
       return getHandWithItem(livingEntity, bowItemPredicate);
    }

    public static Hand getHandWithShootable(LivingEntity livingEntity){
        return getHandWithItem(livingEntity, shootableItemPredicate);
    }

    public static boolean isHoldingShootable(LivingEntity livingEntity){
        return livingEntity.isHolding(shootableItemPredicate);
    }

    public static boolean shouldUseShootable(LivingEntity livingEntity){
        return isHoldingShootable(livingEntity)
                && (getHandWithShootable(livingEntity) == Hand.MAIN_HAND || !rangedItemPredicate.test(livingEntity.getMainHandItem().getItem()));
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isUsingShootable(LivingEntity livingEntity){
        return livingEntity.isUsingItem() && isHoldingShootable(livingEntity) && livingEntity.getUsedItemHand() == getHandWithShootable(livingEntity);
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isHoldingBow(LivingEntity livingEntity){
        return livingEntity.isHolding(bowItemPredicate);
    }

    public static boolean shouldUseBow(LivingEntity livingEntity){
        return isHoldingBow(livingEntity)
                && (getHandWithBow(livingEntity) == Hand.MAIN_HAND || !rangedItemPredicate.test(livingEntity.getMainHandItem().getItem()));
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isUsingBow(LivingEntity livingEntity){
        return livingEntity.isUsingItem() && isHoldingBow(livingEntity) && livingEntity.getUsedItemHand() == getHandWithBow(livingEntity);
    }

    public static Hand getHandWithCrossbow(LivingEntity livingEntity) {
        return getHandWithItem(livingEntity, crossbowItemPredicate);
    }

    public static boolean isHoldingCrossbow(LivingEntity livingEntity){
        return livingEntity.isHolding(crossbowItemPredicate);
    }

    public static boolean shouldUseCrossbow(LivingEntity livingEntity){
        return isHoldingCrossbow(livingEntity)
                && (getHandWithCrossbow(livingEntity) == Hand.MAIN_HAND || !rangedItemPredicate.test(livingEntity.getMainHandItem().getItem()));
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isUsingCrossbow(LivingEntity livingEntity){
        return livingEntity.isUsingItem() && isHoldingCrossbow(livingEntity) && livingEntity.getUsedItemHand() == getHandWithCrossbow(livingEntity);
    }

    public static Hand getHandWithTrident(LivingEntity livingEntity) {
        return getHandWithItem(livingEntity, tridentItemPredicate);
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isHoldingTrident(LivingEntity livingEntity){
        return livingEntity.isHolding(tridentItemPredicate);
    }

    public static boolean shouldUseTrident(MobEntity mobEntity){
        return isHoldingTrident(mobEntity)
                && (getHandWithTrident(mobEntity) == Hand.MAIN_HAND || !rangedItemPredicate.test(mobEntity.getMainHandItem().getItem()));
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean isUsingTrident(LivingEntity livingEntity){
        return livingEntity.isUsingItem() && isHoldingTrident(livingEntity) && livingEntity.getUsedItemHand() == getHandWithTrident(livingEntity);
    }

    public static boolean isUsingRangedWeapon(LivingEntity livingEntity){
        return isUsingShootable(livingEntity) || isUsingTrident(livingEntity);
    }

    public static boolean isRanged(MobEntity mobEntity) {
        return shouldUseShootable(mobEntity) || shouldUseTrident(mobEntity);
    }
}
