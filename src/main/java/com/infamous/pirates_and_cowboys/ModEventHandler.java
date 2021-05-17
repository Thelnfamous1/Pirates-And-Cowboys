package com.infamous.pirates_and_cowboys;

import com.infamous.pirates_and_cowboys.entity.ModEntityTypes;
import com.infamous.pirates_and_cowboys.entity.PlundererEntity;
import com.infamous.pirates_and_cowboys.entity.WranglerEntity;
import com.infamous.pirates_and_cowboys.goal.RideMobShoulderGoal;
import com.infamous.pirates_and_cowboys.goal.FollowMobOwnerGoal;
import com.infamous.pirates_and_cowboys.item.ModSpawnEggItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PiratesAndCowboys.MODID)
public class ModEventHandler {

    @SubscribeEvent
    public static void onRegisterEntities(final RegistryEvent.Register<EntityType<?>> event){
        ModSpawnEggItem.initSpawnEggs();
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event){
        if(event.getEntity() instanceof ShoulderRidingEntity){
            ShoulderRidingEntity shoulderRidingEntity = (ShoulderRidingEntity) event.getEntity();
            shoulderRidingEntity.goalSelector.addGoal(2, new FollowMobOwnerGoal(shoulderRidingEntity, 1.0D, 5.0F, 1.0F, true));
            shoulderRidingEntity.goalSelector.addGoal(3, new RideMobShoulderGoal<>(shoulderRidingEntity));
        }
    }

    @SubscribeEvent
    public static void setupAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntityTypes.PLUNDERER.get(), PlundererEntity.registerAttributes().build());
        event.put(ModEntityTypes.WRANGLER.get(), WranglerEntity.registerAttributes().build());
    }
}
