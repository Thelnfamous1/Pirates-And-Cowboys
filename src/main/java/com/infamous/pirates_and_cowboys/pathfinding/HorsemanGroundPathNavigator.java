package com.infamous.pirates_and_cowboys.pathfinding;

import com.infamous.pirates_and_cowboys.entity.IHorseUser;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.World;

public class HorsemanGroundPathNavigator<T extends MobEntity & IHorseUser> extends MountingGroundPathNavigator<T>{

    public HorsemanGroundPathNavigator(T horseUser, World worldIn) {
        super(horseUser, worldIn);
    }

    @Override
    protected PathFinder createPathFinder(int followRangeDistanceSq) {
        this.nodeEvaluator = new HorsemanWalkNodeProcessor();
        return new PathFinder(this.nodeEvaluator, followRangeDistanceSq);
    }
}
