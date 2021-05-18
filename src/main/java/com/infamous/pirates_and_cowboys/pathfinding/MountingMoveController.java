package com.infamous.pirates_and_cowboys.pathfinding;

import com.infamous.pirates_and_cowboys.entity.IMountUser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;

public class MountingMoveController<T extends MobEntity & IMountUser> extends MovementController {

    public MountingMoveController(T mountUser) {
        super(mountUser);
    }

    @Override
    public void tick() {
        if (this.operation == MovementController.Action.STRAFE) {
            this.handleStrafe();
        } else if (this.operation == MovementController.Action.MOVE_TO) {
            this.handleMoveTo();
        } else if (this.operation == MovementController.Action.JUMPING) {
            this.handleJumping();
        } else {
            this.handleWait();
        }
    }

    protected void handleWait() {
        this.getNavigatingMob().setZza(0.0F);
        //this.getActualMovingMob().setZza(0.0F);
    }

    protected void handleJumping() {
        this.getNavigatingMob().setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        //this.getActualMovingMob().setSpeed((float)(this.speedModifier * this.getActualMovingMob().getAttributeValue(Attributes.MOVEMENT_SPEED)));
        if (this.getActualMovingMob().isOnGround()) {
            this.operation = Action.WAIT;
        }
    }

    protected void handleMoveTo() {
        this.operation = Action.WAIT;
        double xDifference = this.wantedX - this.getActualMovingMob().getX();
        double zDifference = this.wantedZ - this.getActualMovingMob().getZ();
        double yDifference = this.wantedY - this.getActualMovingMob().getY();
        double positionDifferenceSq = xDifference * xDifference + yDifference * yDifference + zDifference * zDifference;
        if (positionDifferenceSq < (double)2.5000003E-7F) {
            this.getNavigatingMob().setZza(0.0F);
            //this.getActualMovingMob().setZza(0.0F);
            return;
        }

        float targetAngle = (float)(MathHelper.atan2(zDifference, xDifference) * (double)(180F / (float)Math.PI)) - 90.0F;
        this.getNavigatingMob().yRot = this.rotlerp(this.getNavigatingMob().yRot, targetAngle, 90.0F);
        this.getNavigatingMob().setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        //this.getActualMovingMob().setSpeed((float)(this.speedModifier * this.getActualMovingMob().getAttributeValue(Attributes.MOVEMENT_SPEED)));

        this.handleMoveToJump(xDifference, zDifference, yDifference);
    }

    protected void handleMoveToJump(double xDifference, double zDifference, double yDifference) {
        BlockPos mobPos = this.getActualMovingMob().blockPosition();
        BlockState blockStateAtPos = this.getNavigatingMob().level.getBlockState(mobPos);
        Block blockAtPos = blockStateAtPos.getBlock();
        VoxelShape voxelShapeAtPos = blockStateAtPos.getCollisionShape(this.getNavigatingMob().level, mobPos);

        boolean cannotStepOver = yDifference > (double) this.getActualMovingMob().maxUpStep;
        boolean horizontalPosDiffLessThanMobWidth = xDifference * xDifference + zDifference * zDifference < (double) Math.max(1.0F, this.getActualMovingMob().getBbWidth());
        boolean mobPosYLessThanBlockTopPosY = this.getActualMovingMob().getY() < voxelShapeAtPos.max(Direction.Axis.Y) + (double) mobPos.getY();
        if (cannotStepOver
                && horizontalPosDiffLessThanMobWidth || !voxelShapeAtPos.isEmpty()
                && mobPosYLessThanBlockTopPosY
                && !blockAtPos.is(BlockTags.DOORS)
                && !blockAtPos.is(BlockTags.FENCES)) {
            this.getActualMovingMob().getJumpControl().jump();
            this.operation = Action.JUMPING;
        }
    }

    protected void handleStrafe() {
        float movementSpeed = (float)this.getNavigatingMob().getAttributeValue(Attributes.MOVEMENT_SPEED);
        //float movementSpeed = (float)this.getActualMovingMob().getAttributeValue(Attributes.MOVEMENT_SPEED);
        float adjustedMovementSpeed = (float)this.speedModifier * movementSpeed;
        float strafeForwards = this.strafeForwards;
        float strafeRight = this.strafeRight;
        float horizontalMovementSq = MathHelper.sqrt(strafeForwards * strafeForwards + strafeRight * strafeRight);
        if (horizontalMovementSq < 1.0F) {
            horizontalMovementSq = 1.0F;
        }

        horizontalMovementSq = adjustedMovementSpeed / horizontalMovementSq;
        strafeForwards = strafeForwards * horizontalMovementSq;
        strafeRight = strafeRight * horizontalMovementSq;
        float sinAdjustedYaw = MathHelper.sin(this.getNavigatingMob().yRot * ((float)Math.PI / 180F));
        float cosAdjustedYaw = MathHelper.cos(this.getNavigatingMob().yRot * ((float)Math.PI / 180F));
        float xAddition = strafeForwards * cosAdjustedYaw - strafeRight * sinAdjustedYaw;
        float zAddition = strafeRight * cosAdjustedYaw + strafeForwards * sinAdjustedYaw;
        if (!this.canStrafeWalk(xAddition, zAddition)) {
            this.strafeForwards = 1.0F;
            this.strafeRight = 0.0F;
        }

        this.getNavigatingMob().setSpeed(adjustedMovementSpeed);
        //this.getActualMovingMob().setSpeed(adjustedMovementSpeed);
        this.getNavigatingMob().setZza(this.strafeForwards);
        //this.getActualMovingMob().setZza(this.strafeForwards);
        this.getNavigatingMob().setXxa(this.strafeRight);
        //this.getActualMovingMob().setXxa(this.strafeRight);
        this.operation = Action.WAIT;
    }

    protected boolean canStrafeWalk(float xAddition, float zAddition) {
        PathNavigator pathNavigator = this.mob.getNavigation();
        if (pathNavigator != null) {
            NodeProcessor nodeprocessor = pathNavigator.getNodeEvaluator();
            if (nodeprocessor != null && nodeprocessor.getBlockPathType(this.mob.level, MathHelper.floor(this.getActualMovingMob().getX() + (double)xAddition), MathHelper.floor(this.getActualMovingMob().getY()), MathHelper.floor(this.getActualMovingMob().getZ() + (double)zAddition)) != PathNodeType.WALKABLE) {
                return false;
            }
        }

        return true;
    }

    protected MobEntity getNavigatingMob(){
        return this.mob;
    }

    protected MobEntity getActualMovingMob(){
        if(this.mob.getVehicle() instanceof MobEntity){
            return (MobEntity) this.mob.getVehicle();
        }
        else return this.mob;
    }
}
