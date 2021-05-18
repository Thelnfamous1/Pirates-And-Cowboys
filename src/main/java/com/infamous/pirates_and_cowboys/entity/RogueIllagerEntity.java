package com.infamous.pirates_and_cowboys.entity;

import com.google.common.collect.Maps;
import com.infamous.pirates_and_cowboys.PiratesAndCowboys;
import com.infamous.pirates_and_cowboys.RangedMobHelper;
import com.infamous.pirates_and_cowboys.goal.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.util.GroundPathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("EntityConstructor")
public abstract class RogueIllagerEntity extends AbstractIllagerEntity implements IModCrossbowUser, IBowUser, ITridentUser {
    private static final Predicate<Difficulty> BREAK_DOORS_PREDICATE = (difficulty) -> difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
    private static final DataParameter<Boolean> IS_CHARGING = EntityDataManager.defineId(RogueIllagerEntity.class, DataSerializers.BOOLEAN);
    protected static final double AI_RUN_SPEED_MULTIPLIER = 1.0D;
    protected static final double AI_WALK_SPEED_MULTIPLIER = 0.6D;
    private final ShootCrossbowGoal<RogueIllagerEntity> shootCrossbowGoal = new ShootCrossbowGoal<>(this, AI_RUN_SPEED_MULTIPLIER, 8.0F);
    private final ShootBowGoal<RogueIllagerEntity> shootBowGoal = new ShootBowGoal<>(this, AI_RUN_SPEED_MULTIPLIER, 20, 15.0F);
    private final ThrowTridentGoal<RogueIllagerEntity> throwTridentGoal = new ThrowTridentGoal<>(this, AI_RUN_SPEED_MULTIPLIER, 40, 10.0F);
    private final AttackGoal<RogueIllagerEntity> attackGoal = new AttackGoal<>(this, AI_RUN_SPEED_MULTIPLIER, false);

    protected RogueIllagerEntity(EntityType<? extends RogueIllagerEntity> type, World worldIn) {
        super(type, worldIn);
        this.setCombatTask();
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficulty) {
        ItemStack weaponStack = this.createWeaponStack();
        this.setItemSlot(EquipmentSlotType.MAINHAND, weaponStack);
    }

    protected abstract ItemStack createWeaponStack();

    // assuming this function just checks if the mob can use the given shootable item
    @Override
    public boolean canFireProjectileWeapon(ShootableItem shootableItem) {
        return true;
    }

    protected abstract void spawnCompanion(IServerWorld serverWorld);

    @Override
    public void setItemSlot(EquipmentSlotType slotIn, ItemStack stack) {
        super.setItemSlot(slotIn, stack);
        if(!this.level.isClientSide && slotIn.getType() == EquipmentSlotType.Group.HAND){
            this.setCombatTask();
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new RaiderBreakDoorGoal(this, 6, BREAK_DOORS_PREDICATE));
        this.goalSelector.addGoal(2, new AbstractIllagerEntity.RaidOpenDoorGoal(this));
        this.goalSelector.addGoal(3, new AbstractIllagerEntity.FindTargetGoal(this, 10.0F));

        this.goalSelector.addGoal(8, new RandomWalkingGoal(this, AI_WALK_SPEED_MULTIPLIER));
        this.goalSelector.addGoal(9, new BetterLookAtGoal(this, PlayerEntity.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new BetterLookAtGoal(this, MobEntity.class, 15.0F));

        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, AbstractRaiderEntity.class)).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING, false);
    }

    @SuppressWarnings("WeakerAccess")
    public void setCombatTask() {
        if (this.level != null && !this.level.isClientSide) {
            this.goalSelector.removeGoal(shootCrossbowGoal);
            this.goalSelector.removeGoal(shootBowGoal);
            this.goalSelector.removeGoal(throwTridentGoal);
            this.goalSelector.removeGoal(attackGoal);
            if(RangedMobHelper.shouldUseCrossbow(this)){
                this.goalSelector.addGoal(4, shootCrossbowGoal);
            }
            else if(RangedMobHelper.shouldUseBow(this)){
                this.goalSelector.addGoal(4, shootBowGoal);
            }
            else if(RangedMobHelper.shouldUseShootable(this)){
                PiratesAndCowboys.LOGGER.debug(this + " is using a modded ShootableItem!");
            }
            else if(RangedMobHelper.shouldUseTrident(this)){
                this.goalSelector.addGoal(4, throwTridentGoal);
            }
            else{
                this.goalSelector.addGoal(4, attackGoal);
            }
        }
    }

    @Nullable
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        if(this.getCurrentRaid() == null){
            this.populateDefaultEquipmentSlots(difficultyIn);
            this.populateDefaultEquipmentEnchantments(difficultyIn);
            this.setCombatTask();
        }
        if(GroundPathHelper.hasGroundPathNavigation(this)){
            ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(true);
        }
        this.spawnCompanion(worldIn);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!this.isNoAi()) {
            if(GroundPathHelper.hasGroundPathNavigation(this)){
                boolean isInRaid = ((ServerWorld)this.level).isRaided(this.blockPosition());
                ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(isInRaid);
            }
        }
    }

    @Override
    public void applyRaidBuffs(int wave, boolean makeLeader) {
        ItemStack weaponStack = createWeaponStack();
        boolean isCrossbowStack = RangedMobHelper.crossbowItemPredicate.test(weaponStack.getItem());
        boolean isBowStack = RangedMobHelper.bowItemPredicate.test(weaponStack.getItem());
        boolean isTridentStack = RangedMobHelper.tridentItemPredicate.test(weaponStack.getItem());

        Raid raid = this.getCurrentRaid();
        boolean applyWaveBonus = false;
        if (raid != null) {
            applyWaveBonus = this.random.nextFloat() <= raid.getEnchantOdds();
        }
        if (applyWaveBonus) {
            Map<Enchantment, Integer> weaponEnchantmentMap = Maps.newHashMap();

            if (wave > raid.getNumGroups(Difficulty.NORMAL)) {
                if(isCrossbowStack){
                    weaponEnchantmentMap.put(Enchantments.QUICK_CHARGE, 2);
                }
                else if(isBowStack){
                    weaponEnchantmentMap.put(Enchantments.POWER_ARROWS, 2);
                }
                else if(isTridentStack){
                    weaponEnchantmentMap.put(Enchantments.LOYALTY, 2);
                }
                else{
                    weaponEnchantmentMap.put(Enchantments.SHARPNESS, 2);
                }

            } else if (wave > raid.getNumGroups(Difficulty.EASY)) {
                if(isCrossbowStack){
                    weaponEnchantmentMap.put(Enchantments.QUICK_CHARGE, 1);
                }
                else if(isBowStack){
                    weaponEnchantmentMap.put(Enchantments.POWER_ARROWS, 1);
                }
                else if(isTridentStack){
                    weaponEnchantmentMap.put(Enchantments.LOYALTY, 1);
                }
                else{
                    weaponEnchantmentMap.put(Enchantments.SHARPNESS, 1);
                }
            }

            if(isCrossbowStack){
                weaponEnchantmentMap.put(Enchantments.MULTISHOT, 1);
            }

            EnchantmentHelper.setEnchantments(weaponEnchantmentMap, weaponStack);
        }
        this.setItemSlot(EquipmentSlotType.MAINHAND, weaponStack);
        this.setCombatTask();
    }

    @Override
    public boolean isCharging() {
        return this.entityData.get(IS_CHARGING);
    }

    @Override
    public void setChargingCrossbow(boolean isCharging) {
        this.entityData.set(IS_CHARGING, isCharging);
    }

    @OnlyIn(Dist.CLIENT)
    public AbstractIllagerEntity.ArmPose getArmPose() {
        if (this.isCharging()) {
            return AbstractIllagerEntity.ArmPose.CROSSBOW_CHARGE;
        } else if (RangedMobHelper.shouldUseCrossbow(this)) {
            return AbstractIllagerEntity.ArmPose.CROSSBOW_HOLD;
        } else if (RangedMobHelper.shouldUseBow(this)) {
            return AbstractIllagerEntity.ArmPose.BOW_AND_ARROW;
        } else {
            return this.isAggressive() ? AbstractIllagerEntity.ArmPose.ATTACKING : AbstractIllagerEntity.ArmPose.NEUTRAL;
        }
    }

    @Override
    public boolean isAlliedTo(Entity entityIn) {
        if (super.isAlliedTo(entityIn)) {
            return true;
        } else if (entityIn instanceof LivingEntity && ((LivingEntity)entityIn).getMobType() == CreatureAttribute.ILLAGER) {
            return this.getTeam() == null && entityIn.getTeam() == null;
        } else {
            return false;
        }
    }

    @Override
    // fireCrossbowProjectile
    public void shootCrossbowProjectile(LivingEntity attackTarget, ItemStack crossbow, ProjectileEntity projectile, float projectileAngle) {
        this.shootCrossbowProjectile(this, attackTarget, projectile, projectileAngle, 1.6F);
    }

    @Override
    // fireCrossbowProjectileAfter
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if(RangedMobHelper.shouldUseCrossbow(this)){
            this.performCrossbowAttack(this, 1.6F);
        }
        else if(RangedMobHelper.shouldUseBow(this)){
            ProjectileEntity projectileEntity = this.createBowProjectile(this.level, this);
            this.shootBow(this, target, projectileEntity, 1.6F);
        }
        else if(RangedMobHelper.shouldUseTrident(this)){
            this.throwTrident(this, target, 1.6F);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.setCombatTask();
        this.setCanPickUpLoot(true);
    }
}
