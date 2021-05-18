package com.infamous.pirates_and_cowboys.goal;

import com.infamous.pirates_and_cowboys.entity.IHorseUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.horse.HorseEntity;

import java.lang.reflect.Method;
import java.util.List;

public class LookForHorseGoal<T extends MobEntity & IHorseUser> extends Goal {
    private static final String CAN_FIT_PASSENGER_METHOD_NAME = "func_184219_q";
    private final T horseUser;
    private double moveSpeedMultiplier;
    private HorseEntity targetHorse;
    private double maxDetectionRange;
    private Method canFitPassenger;

   public LookForHorseGoal(T horseUser, double maxDetectionRange, double moveSpeedMultiplier) {
      this.horseUser = horseUser;
      this.maxDetectionRange = maxDetectionRange;
      this.moveSpeedMultiplier = moveSpeedMultiplier;
   }

    private boolean shouldNotLookForHorse() {
       if (this.horseUser.isInWater()) {
            return true;
        }
        return this.horseUser.getVehicle() != null;
    }

    private boolean shouldRideHorse(HorseEntity horseEntity) {
        if(horseEntity.isInWater()){
            return false;
        }
       List<Entity> passengers = horseEntity.getPassengers();
        for(Entity passenger : passengers){
            if(!passenger.isAlliedTo(this.horseUser)){
                return false;
            }
        }

        return this.canFitOnHorse(horseEntity);

    }

    private boolean canFitOnHorse(HorseEntity horseEntity) {
        return horseEntity.getPassengers().size() < 1;
    }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
       if (this.shouldNotLookForHorse()) return false;

       List<HorseEntity> nearbyHorses = this.horseUser.level.getEntitiesOfClass(
               HorseEntity.class,
               this.horseUser.getBoundingBox().inflate(this.maxDetectionRange),
               this.horseUser::canSee);

       for(HorseEntity nearbyHorse : nearbyHorses) {
          if (shouldRideHorse(nearbyHorse)) {
              this.targetHorse = nearbyHorse;
              break;
         }
      }

      return this.targetHorse != null;
   }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        if (this.horseUser.getBoundingBox().inflate(0.2D).intersects(this.targetHorse.getBoundingBox())) {
            boolean startedRidingHorse = this.horseUser.startRiding(this.targetHorse);
            if(startedRidingHorse)
                return;
        }
        this.horseUser.getNavigation().moveTo(
                this.targetHorse,
                this.moveSpeedMultiplier);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        if (this.shouldNotLookForHorse()) return false;

        return this.targetHorse != null && this.shouldRideHorse(this.targetHorse);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.targetHorse = null;
    }

    public boolean isInterruptable() {
      return true;
   }
}