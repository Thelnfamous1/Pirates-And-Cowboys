package com.infamous.pirates_and_cowboys.client;

import com.infamous.pirates_and_cowboys.PiratesAndCowboys;
import com.infamous.pirates_and_cowboys.entity.WranglerEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class WranglerRenderer extends RogueIllagerRenderer<WranglerEntity> {
    private static final ResourceLocation WRANGLER_TEXTURE = new ResourceLocation(PiratesAndCowboys.MODID, "textures/entity/illager/wrangler.png");

    public WranglerRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
        // wranglers wear a mask that is mapped onto the illager headwear UV
        this.model.hat.visible = true;
    }

    @Override
    protected void scale(WranglerEntity pillagerEntity, MatrixStack matrixStack, float v) {
        float normalScaleFactor = 0.9375F;
        matrixStack.scale(normalScaleFactor, normalScaleFactor, normalScaleFactor);
        super.scale(pillagerEntity, matrixStack, v);
    }

    @Override
    public ResourceLocation getTextureLocation(WranglerEntity entity) {
        return WRANGLER_TEXTURE;
    }
}
