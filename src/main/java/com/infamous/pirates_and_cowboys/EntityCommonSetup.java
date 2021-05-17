package com.infamous.pirates_and_cowboys;

import com.infamous.pirates_and_cowboys.entity.ModEntityTypes;
import com.infamous.pirates_and_cowboys.entity.PlundererEntity;
import com.infamous.pirates_and_cowboys.entity.WranglerEntity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.gen.Heightmap;

public class EntityCommonSetup {

    public static void registerSpawnPlacements(){
        EntitySpawnPlacementRegistry.register(
                ModEntityTypes.PLUNDERER.get(),
                EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                MonsterEntity::checkAnyLightMonsterSpawnRules);

        EntitySpawnPlacementRegistry.register(
                ModEntityTypes.WRANGLER.get(),
                EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                MonsterEntity::checkAnyLightMonsterSpawnRules);
    }
}
