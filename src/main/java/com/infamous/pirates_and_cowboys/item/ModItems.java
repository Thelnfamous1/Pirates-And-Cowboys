package com.infamous.pirates_and_cowboys.item;

import com.infamous.pirates_and_cowboys.PiratesAndCowboys;
import com.infamous.pirates_and_cowboys.entity.ModEntityTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PiratesAndCowboys.MODID);

    public static final RegistryObject<ModSpawnEggItem> PLUNDERER_SPAWN_EGG = ITEMS.register("plunderer_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityTypes.PLUNDERER,
                    9804699, 2580065,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));


    public static final RegistryObject<ModSpawnEggItem> WRANGLER_SPAWN_EGG = ITEMS.register("wrangler_spawn_egg",
            () -> new ModSpawnEggItem(ModEntityTypes.WRANGLER,
                    9804699, 2580065,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));


}
