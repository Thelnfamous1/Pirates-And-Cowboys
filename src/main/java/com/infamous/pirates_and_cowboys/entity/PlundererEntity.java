package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.goal.LookForBoatGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public class PlundererEntity extends RogueIllagerEntity implements IShoulderRidable, IBoatUser {
    private long timeEntitySatOnShoulder = 0;
    private static final DataParameter<CompoundNBT> LEFT_SHOULDER_ENTITY = EntityDataManager.defineId(PlundererEntity.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<CompoundNBT> RIGHT_SHOULDER_ENTITY = EntityDataManager.defineId(PlundererEntity.class, DataSerializers.COMPOUND_TAG);

    public PlundererEntity(World world){
        super(ModEntityTypes.PLUNDERER.get(), world);
    }

    public PlundererEntity(EntityType<? extends PlundererEntity> entityType, World world){
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookForBoatGoal<>(this, 16.0D, 1.0D));
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, (double)0.35F)
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }


    @Override
    protected ItemStack createWeaponStack() {
        return this.random.nextFloat() < 0.25D ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.IRON_SWORD);
    }

    @Override
    protected void spawnCompanion(IServerWorld serverWorld) {
        ParrotEntity parrotEntity = EntityType.PARROT.create(this.level);
        if(parrotEntity != null){
            parrotEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
            parrotEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(this.blockPosition()), SpawnReason.MOB_SUMMONED, null, null);
            parrotEntity.setTame(true);
            parrotEntity.setOwnerUUID(this.getUUID()); // need this so the Parrot's RideShoulderGoal will work
            serverWorld.addFreshEntity(parrotEntity);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LEFT_SHOULDER_ENTITY, new CompoundNBT());
        this.entityData.define(RIGHT_SHOULDER_ENTITY, new CompoundNBT());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.handleShoulderEntityOnLivingTick();
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.readShoulderRiderNBT(compound);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        this.writeShoulderRiderNBT(compound);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        else if (this.isDeadOrDying()) {
            return false;
        } else {
            this.spawnShoulderEntities();
            return super.hurt(source, amount);
        }
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        this.spawnShoulderEntities();
    }

    @Override
    public CompoundNBT getLeftShoulderEntity() {
        return this.entityData.get(LEFT_SHOULDER_ENTITY);
    }

    @Override
    public void setLeftShoulderEntity(CompoundNBT tag) {
        this.entityData.set(LEFT_SHOULDER_ENTITY, tag);
    }

    @Override
    public CompoundNBT getRightShoulderEntity() {
        return this.entityData.get(RIGHT_SHOULDER_ENTITY);
    }

    @Override
    public void setRightShoulderEntity(CompoundNBT tag) {
        this.entityData.set(RIGHT_SHOULDER_ENTITY, tag);
    }

    @Override
    public void playShoulderEntityAmbientSound(@Nullable CompoundNBT shoulderEntityNBT) {
        if (shoulderEntityNBT != null && (!shoulderEntityNBT.contains(IShoulderRidable.SILENT_NBT_KEY) || !shoulderEntityNBT.getBoolean(IShoulderRidable.SILENT_NBT_KEY)) && this.level.random.nextInt(200) == 0) {
            String entityID = shoulderEntityNBT.getString(IShoulderRidable.ID_NBT_KEY);
            EntityType.byString(entityID).filter((entityType) -> entityType == EntityType.PARROT).ifPresent((entityType) -> {
                if (!ParrotEntity.imitateNearbyMobs(this.level, this)) {
                    this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), ParrotEntity.getAmbient(this.level, this.level.random), this.getSoundSource(), 1.0F, ParrotEntity.getPitch(this.level.random));
                }

            });
        }

    }

    @Override
    public boolean addShoulderEntity(CompoundNBT shoulderRidingNBT) {
        if (!this.isPassenger() && this.onGround && !this.isInWater()) {
            if (this.getLeftShoulderEntity().isEmpty()) {
                this.setLeftShoulderEntity(shoulderRidingNBT);
                this.timeEntitySatOnShoulder = this.level.getGameTime();
                return true;
            } else if (this.getRightShoulderEntity().isEmpty()) {
                this.setRightShoulderEntity(shoulderRidingNBT);
                this.timeEntitySatOnShoulder = this.level.getGameTime();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void spawnShoulderEntities() {
        if (this.timeEntitySatOnShoulder + 20L < this.level.getGameTime()) {
            this.spawnShoulderEntity(this.getLeftShoulderEntity());
            this.setLeftShoulderEntity(new CompoundNBT());
            this.spawnShoulderEntity(this.getRightShoulderEntity());
            this.setRightShoulderEntity(new CompoundNBT());
        }

    }

    private void spawnShoulderEntity(CompoundNBT compoundNBT) {
        if (!this.level.isClientSide && !compoundNBT.isEmpty()) {
            EntityType.create(compoundNBT, this.level).ifPresent((entity) -> {
                if (entity instanceof TameableEntity) {
                    ((TameableEntity)entity).setOwnerUUID(this.uuid);
                }

                entity.setPos(this.getX(), this.getY() + (double)0.7F, this.getZ());
                ((ServerWorld)this.level).addWithUUID(entity);
            });
        }

    }

    private void handleShoulderEntityOnLivingTick() {
        this.playShoulderEntityAmbientSound(this.getLeftShoulderEntity());
        this.playShoulderEntityAmbientSound(this.getRightShoulderEntity());
        if (!this.level.isClientSide && (this.fallDistance > 0.5F || this.isInWater()) || this.isSleeping()) {
            this.spawnShoulderEntities();
        }
    }

    @Override
    public void push(Entity entityIn) {
        super.push(entityIn);
        this.pickUpParrot(this, entityIn);
    }

    @Override
    public boolean startRiding(Entity entityIn, boolean force) {
        return super.startRiding(entityIn, force);
    }

    @Override
    public void rideTick() {
        super.rideTick();
    }
}
