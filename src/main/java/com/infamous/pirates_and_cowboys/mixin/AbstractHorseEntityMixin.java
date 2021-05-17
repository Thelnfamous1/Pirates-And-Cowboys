package com.infamous.pirates_and_cowboys.mixin;

import com.infamous.pirates_and_cowboys.entity.IServerJumpingMount;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin extends AnimalEntity implements IServerJumpingMount {

    @Shadow
    protected float playerJumpPendingScale;
    @Shadow
    private boolean allowStandSliding;

    protected AbstractHorseEntityMixin(EntityType<? extends AnimalEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public void setServerSideJumpPower(int jumpPowerIn) {
        if (this.isSaddled() && !this.level.isClientSide) {
            if (jumpPowerIn < 0) {
                jumpPowerIn = 0;
            } else {
                this.allowStandSliding = true;
                this.stand();
            }

            if (jumpPowerIn >= 90) {
                this.playerJumpPendingScale = 1.0F;
            } else {
                this.playerJumpPendingScale = 0.4F + 0.4F * (float)jumpPowerIn / 90.0F;
            }
        }
    }

    @Shadow
    private void stand() {

    }

    @Shadow
    public abstract boolean isSaddled();
}
