package com.infamous.pirates_and_cowboys.client;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
// Borrowed from tallestred / seymourimadeit's IllagersWearArmor mod
public class IllagerArmorModel extends BipedModel<AbstractIllagerEntity> {
    public IllagerArmorModel(float modelSizeIn) {
        super(modelSizeIn);
        this.head = new ModelRenderer(this, 0, 0);
        this.head.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, modelSizeIn);
        this.head.setPos(0.0F, 0.0F, 0.0F);
    }
}