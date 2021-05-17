package com.infamous.pirates_and_cowboys.goal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.infamous.pirates_and_cowboys.entity.IBoatUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class LookForBoatGoal<T extends MobEntity & IBoatUser> extends Goal {
    private static final String CAN_FIT_PASSENGER_METHOD_NAME = "func_184219_q";
    private final T boatUser;
    private double moveSpeedMultiplier;
    private BoatEntity targetBoat;
    private double maxDetectionRange;
    private Method canFitPassenger;

   public LookForBoatGoal(T boatUser, double maxDetectionRange, double moveSpeedMultiplier) {
      this.boatUser = boatUser;
      this.maxDetectionRange = maxDetectionRange;
      this.moveSpeedMultiplier = moveSpeedMultiplier;
   }

    private boolean shouldNotLookForBoat() {
        if (!this.boatUser.isInWater()) {
            return true;
        }
        return this.boatUser.getVehicle() != null;
    }

    private boolean shouldRideBoat(BoatEntity boatEntity) {
        if(!boatEntity.isInWater()){
            return false;
        }
        List<Entity> passengers = boatEntity.getPassengers();
        for(Entity passenger : passengers){
            if(!passenger.isAlliedTo(this.boatUser)){
                return false;
            }
        }

        return canFitOnBoat(boatEntity);

    }

    private boolean canFitOnBoat(BoatEntity boatEntity) {
        if(this.canFitPassenger == null){
            this.canFitPassenger = ObfuscationReflectionHelper.findMethod(BoatEntity.class, CAN_FIT_PASSENGER_METHOD_NAME, Entity.class);
        }
        boolean canFit = false;
        try {
            canFit = (boolean) this.canFitPassenger.invoke(boatEntity, this.boatUser);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return canFit;
    }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
       if (this.shouldNotLookForBoat()) return false;

       List<BoatEntity> nearbyBoats = this.boatUser.level.getEntitiesOfClass(
              BoatEntity.class,
               this.boatUser.getBoundingBox().inflate(this.maxDetectionRange),
               this.boatUser::canSee);

       for(BoatEntity boatEntity : nearbyBoats) {
          if (shouldRideBoat(boatEntity)) {
              this.targetBoat = boatEntity;
              break;
         }
      }

      return this.targetBoat != null;
   }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        if (this.boatUser.getBoundingBox().inflate(0.2D).intersects(this.targetBoat.getBoundingBox())) {
            boolean startedRidingBoat = this.boatUser.startRiding(this.targetBoat);
            if(startedRidingBoat)
                return;
        }
        this.boatUser.getNavigation().moveTo(
                this.targetBoat,
                this.moveSpeedMultiplier);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        if (this.shouldNotLookForBoat()) return false;

        return this.targetBoat != null && this.shouldRideBoat(this.targetBoat);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.targetBoat = null;
    }

    public boolean isInterruptable() {
      return true;
   }
}