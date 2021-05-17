package com.infamous.pirates_and_cowboys.client;

import com.infamous.pirates_and_cowboys.entity.IShoulderRidable;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotShoulderLayer<T extends LivingEntity & IShoulderRidable, M extends BipedModel<T>> extends LayerRenderer<T, M> {
    private final ParrotModel parrotModel = new ParrotModel();

    public ParrotShoulderLayer(IEntityRenderer<T, M> rendererIn) {
        super(rendererIn);
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderParrot(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true);
        this.renderParrot(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false);
    }

    private void renderParrot(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T shoulderRidable, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, boolean leftShoulderIn) {
        CompoundNBT compoundnbt = leftShoulderIn ? shoulderRidable.getLeftShoulderEntity() : shoulderRidable.getRightShoulderEntity();
        EntityType.byString(compoundnbt.getString("id"))
                .filter((entityType) -> entityType == EntityType.PARROT)
                .ifPresent((p_229137_11_) -> {
            matrixStackIn.pushPose();
            matrixStackIn.translate(leftShoulderIn ? 0.4000000059604645D : -0.4000000059604645D, shoulderRidable.isCrouching() ? -1.2999999523162842D : -1.5D, 0.0D);
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.parrotModel.renderType(ParrotRenderer.PARROT_LOCATIONS[compoundnbt.getInt("Variant")]));
            this.parrotModel.renderOnShoulder(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch, shoulderRidable.tickCount);
            matrixStackIn.popPose();
        });
    }
}