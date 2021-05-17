package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.pathfinding.HorsemanGroundPathNavigator;
import com.infamous.pirates_and_cowboys.pathfinding.HorsemanMoveController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public interface IHorseUser extends IMountUser {
    int MAX_HORSE_JUMP_COOLDOWN = 60;
    int PERFECT_JUMP_COUNTER = 9;

    default boolean isCollidingWithAFence(Entity ridingEntity){
        boolean isCollidingHorizontally = ridingEntity.horizontalCollision;
        if(!isCollidingHorizontally) return false;

        Direction mountFacing = ridingEntity.getMotionDirection();
        BlockPos mountPosition = ridingEntity.blockPosition();
        BlockState blockStateInFrontOf = ridingEntity.level.getBlockState(mountPosition.relative(mountFacing));
        Block blockInFrontOf = blockStateInFrontOf.getBlock();

        return blockInFrontOf.is(BlockTags.FENCES)
                || blockInFrontOf.is(BlockTags.WALLS)
                || blockInFrontOf instanceof FenceGateBlock && !blockStateInFrontOf.getValue(FenceGateBlock.OPEN);
    }

    default void updateHorseJumpAI(LivingEntity horseUser) {
        boolean readyToJump = this.getHorseJumpCooldown() <= 0;
        Entity ridingEntity = horseUser.getVehicle();
        if(ridingEntity instanceof IServerJumpingMount && readyToJump){
            IServerJumpingMount jumpingMount = (IServerJumpingMount) ridingEntity;
            boolean isJumping = this.getMountingMoveController().isJumping();
            boolean collidedWithAFence = this.isCollidingWithAFence(ridingEntity);
            boolean mountIsOnGround = ridingEntity.isOnGround();

            // Check if the horse has collided with a fence,
            // the rider's move controller is not in the jump phase
            // and the horse is on the ground
            if(collidedWithAFence && !isJumping && mountIsOnGround && jumpingMount.canJump()){
                //PiratesAndCowboys.LOGGER.info("Horseman {} is attempting to hop over a fence!", horseUser);

                // Tell the rider's move controller to enter the jump phase
                this.getMountingMoveController().setJumping();

                // Generate horse jump power
                // on EASY (0): horse jump power counter range is 6-12 (9 +/- 3)
                // on NORMAL (1): horse jump power counter range is 7-11 (9 +/- 2)
                // on HARD (2): horse jump power counter range is 8-10 (9 +/- 1)
                // on HARDCORE (3): horse jump power counter range is 9 (9 +/- 0)
                int difficultyID = horseUser.level.getDifficulty().getId();

                // N > 9 yields the same horse jump power as 9 - (N-9)
                // Example: 11 will yield the same horse jump power as 9 - (11 - 9) = 9-2 = 7
                int horseJumpPowerCounter =
                        // The target is always a perfect jump counter of 9
                        PERFECT_JUMP_COUNTER +
                                // The multiplier for the range
                                (3 - difficultyID)
                                        // A randomly generated int in [-1, 0, +1]
                                        * (horseUser.getRandom().nextInt(3) - 1);
                float decimalHorseJumpPower;
                if (horseJumpPowerCounter <= PERFECT_JUMP_COUNTER) {
                    decimalHorseJumpPower =  (float) horseJumpPowerCounter * 0.1F;
                } else {
                    decimalHorseJumpPower = 0.8F + 2.0F / (float)(horseJumpPowerCounter - PERFECT_JUMP_COUNTER) * 0.1F;
                }

                int horseJumpPower = MathHelper.floor(decimalHorseJumpPower * 100.0F);

                // Initiate horse jump
                if (horseJumpPower > 0) {
                    //PiratesAndCowboys.LOGGER.info("Horseman {} is making their horse jump!", horseUser);
                    jumpingMount.setServerSideJumpPower(horseJumpPower);
                    jumpingMount.handleStartJump(horseJumpPower);
                    this.setHorseJumpCooldown(MAX_HORSE_JUMP_COOLDOWN);
                }
            }
        }
    }

    int getHorseJumpCooldown();

    void setHorseJumpCooldown(int horseJumpCooldown);

    HorsemanGroundPathNavigator getMountingNavigator();

    HorsemanMoveController getMountingMoveController();
}
