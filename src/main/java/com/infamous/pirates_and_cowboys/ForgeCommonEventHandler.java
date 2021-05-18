package com.infamous.pirates_and_cowboys;

import com.infamous.pirates_and_cowboys.goal.FollowMobOwnerGoal;
import com.infamous.pirates_and_cowboys.goal.RideMobShoulderGoal;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PiratesAndCowboys.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(event.getEntity() instanceof ShoulderRidingEntity){
            ShoulderRidingEntity shoulderRidingEntity = (ShoulderRidingEntity) event.getEntity();
            shoulderRidingEntity.goalSelector.addGoal(2, new FollowMobOwnerGoal(shoulderRidingEntity, 1.0D, 5.0F, 1.0F, true));
            shoulderRidingEntity.goalSelector.addGoal(3, new RideMobShoulderGoal<>(shoulderRidingEntity));
        }
    }
}
