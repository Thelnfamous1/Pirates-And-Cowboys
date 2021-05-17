package com.infamous.pirates_and_cowboys.client;

import com.infamous.pirates_and_cowboys.PiratesAndCowboys;
import com.infamous.pirates_and_cowboys.entity.PlundererEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class PlundererRenderer extends RogueIllagerRenderer<PlundererEntity> {
    private static final ResourceLocation PLUNDERER_TEXTURE = new ResourceLocation(PiratesAndCowboys.MODID, "textures/entity/illager/plunderer.png");

    public PlundererRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
        this.addLayer(new ParrotShoulderLayer<>(this));
    }

    @Override
    protected void scale(PlundererEntity pillagerEntity, MatrixStack matrixStack, float v) {
        float normalScaleFactor = 0.9375F;
        matrixStack.scale(normalScaleFactor, normalScaleFactor, normalScaleFactor);
        super.scale(pillagerEntity, matrixStack, v);
    }

    @Override
    public ResourceLocation getTextureLocation(PlundererEntity entity) {
        return PLUNDERER_TEXTURE;
    }
}
