package com.infamous.pirates_and_cowboys;

import com.infamous.pirates_and_cowboys.entity.ModEntityTypes;
import com.infamous.pirates_and_cowboys.entity.PlundererEntity;
import com.infamous.pirates_and_cowboys.entity.WranglerEntity;
import com.infamous.pirates_and_cowboys.item.ModSpawnEggItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PiratesAndCowboys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEventHandler {

    @SubscribeEvent
    public static void onRegisterEntities(final RegistryEvent.Register<EntityType<?>> event){
        ModSpawnEggItem.initSpawnEggs();
    }

    @SubscribeEvent
    public static void setupAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntityTypes.PLUNDERER.get(), PlundererEntity.registerAttributes().build());
        event.put(ModEntityTypes.WRANGLER.get(), WranglerEntity.registerAttributes().build());
        event.put(ModEntityTypes.MOB_BOAT.get(), MobEntity.createMobAttributes().build());
    }
}
