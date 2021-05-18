package com.infamous.pirates_and_cowboys.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public interface IBoatUser extends IMountUser{

    default Optional<MobBoatEntity> convertBoatToMobBoat(World world, BoatEntity boat){
        if(boat.getType() == EntityType.BOAT && !world.isClientSide){
            MobBoatEntity mobBoat = new MobBoatEntity(world, boat);
            mobBoat.yRot = boat.yRot;
            world.addFreshEntity(mobBoat);
            boat.remove();
            return Optional.of(mobBoat);
        }
        else return Optional.empty();
    }

    default Optional<BoatEntity> convertMobBoatToBoat(World world, MobBoatEntity mobBoat){
        CompoundNBT compoundNBT = mobBoat.getVanillaBoat();
        if (!world.isClientSide && !compoundNBT.isEmpty()) {
            Optional<Entity> optionalBoat = EntityType.create(compoundNBT, world);
            if(optionalBoat.isPresent()){
                optionalBoat.get().setPos(mobBoat.getX(), mobBoat.getY(), mobBoat.getZ());
                optionalBoat.get().yRot = mobBoat.yRot;
                ((ServerWorld)world).addWithUUID(optionalBoat.get());
                mobBoat.setVanillaBoat(new CompoundNBT());
                mobBoat.remove();
                return Optional.of((BoatEntity)optionalBoat.get());
            }
            else return Optional.empty();
        }
        else return Optional.empty();
    }
}
