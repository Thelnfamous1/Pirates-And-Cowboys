package com.infamous.pirates_and_cowboys.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.world.Difficulty;

import java.util.EnumSet;
import java.util.function.Predicate;

public class RaiderBreakDoorGoal extends net.minecraft.entity.ai.goal.BreakDoorGoal {
    private final AbstractRaiderEntity raiderEntity;

    public RaiderBreakDoorGoal(AbstractRaiderEntity raiderEntity, int timeToBreak, Predicate<Difficulty> difficultyPredicate) {
        super(raiderEntity, timeToBreak, difficultyPredicate);
        this.raiderEntity = raiderEntity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.raiderEntity.hasActiveRaid() && super.canContinueToUse();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        return this.raiderEntity.hasActiveRaid() && this.raiderEntity.getRandom().nextInt(10) == 0 && super.canUse();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        super.start();
        this.mob.setNoActionTime(0);
    }
}
