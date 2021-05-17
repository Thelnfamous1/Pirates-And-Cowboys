package com.infamous.pirates_and_cowboys.entity;

import com.infamous.pirates_and_cowboys.PiratesAndCowboys;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, PiratesAndCowboys.MODID);

    public static final RegistryObject<EntityType<PlundererEntity>> PLUNDERER = ENTITY_TYPES.register("plunderer", () ->
            EntityType.Builder.<PlundererEntity>of(PlundererEntity::new, EntityClassification.MONSTER)
                    .sized(0.6F, 1.95F)
                    .setTrackingRange(8)
                    .setCustomClientFactory((spawnEntity,world) -> new PlundererEntity(world))
                    .build(new ResourceLocation(PiratesAndCowboys.MODID, "plunderer").toString())
    );

    public static final RegistryObject<EntityType<WranglerEntity>> WRANGLER = ENTITY_TYPES.register("wrangler", () ->
            EntityType.Builder.<WranglerEntity>of(WranglerEntity::new, EntityClassification.MONSTER)
                    .sized(0.6F, 1.95F)
                    .setTrackingRange(8)
                    .setCustomClientFactory((spawnEntity,world) -> new WranglerEntity(world))
                    .build(new ResourceLocation(PiratesAndCowboys.MODID, "wrangler").toString())
    );

    public static final RegistryObject<EntityType<MobBoatEntity>> MOB_BOAT = ENTITY_TYPES.register("mob_boat", () ->
            EntityType.Builder.<MobBoatEntity>of(MobBoatEntity::new, EntityClassification.MISC)
                    .sized(1.375F, 0.5625F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(PiratesAndCowboys.MODID, "mob_boat").toString())
    );
}
