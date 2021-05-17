package com.infamous.pirates_and_cowboys.client;

import com.infamous.pirates_and_cowboys.entity.RogueIllagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RogueIllagerRenderer<T extends RogueIllagerEntity> extends MobRenderer<T, IllagerBipedModel<T>> {
    private static final ResourceLocation PILLAGER_TEXTURE = new ResourceLocation("textures/entity/illager/pillager.png");

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RogueIllagerRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new IllagerBipedModel<>(0.0F, 0.0F, 64, 64), 0.5f);
        this.addLayer(new BipedArmorLayer(this, new IllagerArmorModel(0.5F), new IllagerArmorModel(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return PILLAGER_TEXTURE;
    }
}
