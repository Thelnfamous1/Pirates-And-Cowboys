package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.goal.LookForHorseGoal;
import com.infamous.pirates_and_cowboys.pathfinding.HorsemanGroundPathNavigator;
import com.infamous.pirates_and_cowboys.pathfinding.HorsemanMoveController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class WranglerEntity extends RogueIllagerEntity implements IHorseUser {
    private int horseJumpCooldown;

    public WranglerEntity(World world){
        super(ModEntityTypes.WRANGLER.get(), world);
    }

    public WranglerEntity(EntityType<? extends WranglerEntity> entityType, World world){
        super(entityType, world);
        this.moveControl = new HorsemanMoveController<>(this);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookForHorseGoal<>(this, 16.0D, 1.0D));
    }

    @Override
    protected PathNavigator createNavigation(World worldIn) {
        return new HorsemanGroundPathNavigator<>(this, worldIn);
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, (double)0.35F)
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected ItemStack createWeaponStack() {
        return this.random.nextFloat() < 0.75D ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.IRON_SWORD);
    }

    @Override
    protected void spawnCompanion(IServerWorld serverWorld) {
        HorseEntity horseEntity = EntityType.HORSE.create(this.level);
        if(horseEntity != null){
            horseEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
            horseEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(this.blockPosition()), SpawnReason.JOCKEY, null, null);
            horseEntity.setTamed(true);
            horseEntity.setOwnerUUID(this.getUUID());
            horseEntity.setAge(0);

            Inventory horseChest = ObfuscationReflectionHelper.getPrivateValue(AbstractHorseEntity.class, horseEntity, "field_110296_bG");
            if (horseChest != null) {
                horseChest.setItem(0, new ItemStack(Items.SADDLE));
            }
            this.startRiding(horseEntity);
            serverWorld.addFreshEntity(horseEntity);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if(this.horseJumpCooldown > 0){
            this.horseJumpCooldown--;
        }
        else if(this.horseJumpCooldown < 0){
            this.horseJumpCooldown = 0;
        }
        this.updateHorseJumpAI(this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VINDICATOR_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.VINDICATOR_HURT;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
    }

    // Inherited methods from parent class that now utilize interface methods

    @Override
    public MovementController getMoveControl() {
        return this.getMountingMoveController();
    }

    @Override
    public PathNavigator getNavigation() {
        return this.getMountingNavigator();
    }

    // IMOUNTUSER METHODS

    @Override
    public HorsemanMoveController getMountingMoveController() {
        return (HorsemanMoveController) this.moveControl;
    }

    @Override
    public HorsemanGroundPathNavigator getMountingNavigator() {
        return (HorsemanGroundPathNavigator) this.navigation;
    }

    @Override
    public int getHorseJumpCooldown() {
        return this.horseJumpCooldown;
    }

    @Override
    public void setHorseJumpCooldown(int horseJumpCooldown) {
        this.horseJumpCooldown = horseJumpCooldown;
    }
}
