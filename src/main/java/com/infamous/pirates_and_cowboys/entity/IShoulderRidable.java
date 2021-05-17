package com.infamous.pirates_and_cowboys.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public interface IShoulderRidable<T extends LivingEntity> {

    String ID_NBT_KEY = "id";
    String SILENT_NBT_KEY = "Silent";
    String SHOULDER_ENTITY_LEFT_NBT_KEY = "ShoulderEntityLeft";
    String SHOULDER_ENTITY_RIGHT_NBT_KEY = "ShoulderEntityRight";

    default void writeShoulderRiderNBT(CompoundNBT writeAdditionalNBT) {
        if (!this.getLeftShoulderEntity().isEmpty()) {
            writeAdditionalNBT.put(SHOULDER_ENTITY_LEFT_NBT_KEY, this.getLeftShoulderEntity());
        }

        if (!this.getRightShoulderEntity().isEmpty()) {
            writeAdditionalNBT.put(SHOULDER_ENTITY_RIGHT_NBT_KEY, this.getRightShoulderEntity());
        }
    }

    default void readShoulderRiderNBT(CompoundNBT readAdditionalNBT) {
        if (readAdditionalNBT.contains(SHOULDER_ENTITY_LEFT_NBT_KEY, 10)) {
            this.setLeftShoulderEntity(readAdditionalNBT.getCompound(SHOULDER_ENTITY_LEFT_NBT_KEY));
        }

        if (readAdditionalNBT.contains(SHOULDER_ENTITY_RIGHT_NBT_KEY, 10)) {
            this.setRightShoulderEntity(readAdditionalNBT.getCompound(SHOULDER_ENTITY_RIGHT_NBT_KEY));
        }
    }

    default boolean addShoulderEntity(ShoulderRidingEntity shoulderRidingEntity) {
        CompoundNBT shoulderRidingNBT = new CompoundNBT();
        String entityString = shoulderRidingEntity.getEncodeId();
        if (entityString != null) {
            shoulderRidingNBT.putString(ID_NBT_KEY, entityString);
            shoulderRidingEntity.saveWithoutId(shoulderRidingNBT);
            if (this.addShoulderEntity(shoulderRidingNBT)) {
                shoulderRidingEntity.remove();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    void playShoulderEntityAmbientSound(@Nullable CompoundNBT compoundNBT);

    boolean addShoulderEntity(CompoundNBT shoulderRidingNBT);

    void spawnShoulderEntities();

    CompoundNBT getLeftShoulderEntity();

    void setLeftShoulderEntity(CompoundNBT tag);

    CompoundNBT getRightShoulderEntity();

    void setRightShoulderEntity(CompoundNBT tag);

    default void pickUpParrot(LivingEntity shoulderRidable, Entity entityIn) {
        if(entityIn instanceof ShoulderRidingEntity
                && ((ShoulderRidingEntity) entityIn).getOwnerUUID() == shoulderRidable.getUUID()){
            this.addShoulderEntity((ShoulderRidingEntity) entityIn);
        }
    }
}
