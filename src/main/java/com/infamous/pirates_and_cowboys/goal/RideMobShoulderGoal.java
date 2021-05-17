package com.infamous.pirates_and_cowboys.goal;

import com.infamous.pirates_and_cowboys.entity.IShoulderRidable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

public class RideMobShoulderGoal<T extends ShoulderRidingEntity> extends Goal {
   private final T shoulderRidingEntity;
   private MobEntity mobOwner;
   private IShoulderRidable ownerAsShoulderRidable;
   private boolean isSittingOnShoulder;

   public RideMobShoulderGoal(T entityIn) {
      this.shoulderRidingEntity = entityIn;
   }

   @Nullable
   private MobEntity getMobOwner(){
      UUID ownerId = this.shoulderRidingEntity.getOwnerUUID();
      if(this.shoulderRidingEntity.level instanceof ServerWorld && ownerId != null){
         Entity ownerEntity = ((ServerWorld) this.shoulderRidingEntity.level).getEntity(ownerId);
         if(ownerEntity instanceof MobEntity){
            return (MobEntity) ownerEntity;
         }
         else return null;
      }
      return null;
   }

   /**
    * Returns whether the EntityAIBase should begin execution.
    */
   public boolean canUse() {
      MobEntity owner = this.getMobOwner();
      if(owner instanceof IShoulderRidable){
         boolean canOwnerBeRidden = !owner.isSpectator() && !owner.isInWater();
         boolean isSitting = this.shoulderRidingEntity.isInSittingPose();
         if(isSitting){
            this.shoulderRidingEntity.setOrderedToSit(false);
         }
         return canOwnerBeRidden && this.shoulderRidingEntity.canSitOnShoulder();
      }
      return false;
   }

   public boolean isInterruptable() {
      return !this.isSittingOnShoulder;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      MobEntity owner = this.getMobOwner();
      this.mobOwner = owner;
      this.ownerAsShoulderRidable = (IShoulderRidable) owner;
      this.isSittingOnShoulder = false;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      boolean isSitting = this.shoulderRidingEntity.isInSittingPose();
      if(isSitting){
         this.shoulderRidingEntity.setOrderedToSit(false);
      }
      if (!this.isSittingOnShoulder && !this.shoulderRidingEntity.isLeashed()) {
         if (this.shoulderRidingEntity.getBoundingBox().intersects(this.mobOwner.getBoundingBox())) {
            this.isSittingOnShoulder = this.ownerAsShoulderRidable.addShoulderEntity(this.shoulderRidingEntity);
         }
      }
   }
}