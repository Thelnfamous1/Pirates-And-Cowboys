package com.infamous.pirates_and_cowboys.pathfinding;

import com.infamous.pirates_and_cowboys.entity.IHorseUser;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;

public class HorsemanMoveController<T extends MobEntity & IHorseUser> extends MountingMoveController<T> {

    public HorsemanMoveController(T horseUser) {
        super(horseUser);
    }

    @Override
    public void setWantedPosition(double x, double y, double z, double speedIn) {
        super.setWantedPosition(x, y, z, speedIn);
        if(this.mob.getVehicle() instanceof AbstractHorseEntity){
            this.speedModifier *= 1.33D;
        }
    }

    public boolean isJumping(){
        return this.operation == Action.JUMPING;
    }

    public void setJumping(){
        this.operation = Action.JUMPING;
    }
}
