package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.pathfinding.MountingGroundPathNavigator;
import com.infamous.pirates_and_cowboys.pathfinding.MountingMoveController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.monster.RavagerEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.pathfinding.PathNavigator;

public interface IMountUser {

    static double getAttackReachSquared(LivingEntity attacker, LivingEntity attackTarget){
        float attackTargetWidth = attackTarget.getBbWidth();
        if(attacker.getVehicle() != null){
            Entity ridingEntity = attacker.getVehicle();
            float ridingEntityWidth = ridingEntity.getBbWidth();
            if(ridingEntity instanceof RavagerEntity){
                ridingEntityWidth -= 0.1F;
                float riderExtendedWidth = ridingEntityWidth * 2.0F * ridingEntityWidth * 2.0F;
                return (riderExtendedWidth + attackTargetWidth);
            }
            else if(ridingEntity instanceof SpiderEntity
                || ridingEntity instanceof PolarBearEntity
                || ridingEntity instanceof RabbitEntity){
                // NOTE:
                // Spiders are 1.4F wide,
                // Cave Spiders are 0.7F wide,
                // Polar bears are 1.4F wide,
                // Rabbits are 0.4F wide
                ridingEntityWidth = 2.0F;
                float riderExtendedWidth = ridingEntityWidth * 2.0F * ridingEntityWidth * 2.0F;
                return riderExtendedWidth + attackTargetWidth;
            }
            else{
                float riderExtendedWidth = ridingEntityWidth * 2.0F * ridingEntityWidth * 2.0F;
                return (riderExtendedWidth + attackTargetWidth);
            }
        }
        else{
            return (attacker.getBbWidth() * 2.0F * attacker.getBbWidth() * 2.0F + attackTargetWidth);
        }
    }

    MountingMoveController getMountingMoveController();

    MountingGroundPathNavigator getMountingNavigator();
}
