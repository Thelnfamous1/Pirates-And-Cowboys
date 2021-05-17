package com.infamous.pirates_and_cowboys.client;

import com.infamous.pirates_and_cowboys.PiratesAndCowboys;
import com.infamous.pirates_and_cowboys.entity.ModEntityTypes;
import com.infamous.pirates_and_cowboys.item.ModSpawnEggItem;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PiratesAndCowboys.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PLUNDERER.get(), PlundererRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.WRANGLER.get(), WranglerRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.MOB_BOAT.get(), MobBoatRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterEntities(final RegistryEvent.Register<EntityType<?>> event) {
        ModSpawnEggItem.initSpawnEggs();
    }
}