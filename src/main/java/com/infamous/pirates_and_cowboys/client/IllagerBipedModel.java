package com.infamous.pirates_and_cowboys.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.infamous.pirates_and_cowboys.RangedMobHelper;
import net.minecraft.item.CrossbowItem;
import net.minecraft.util.Hand;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
// Borrowed from tallestred / seymourimadeit's IllagersWearArmor mod
public class IllagerBipedModel<T extends AbstractIllagerEntity> extends BipedModel<T> {
    public ModelRenderer arms;
    public ModelRenderer jacket;

    public IllagerBipedModel(float modelSize, float yOffset, int textureWidthIn, int textureHeightIn) {
        super(modelSize);
        this.head = (new ModelRenderer(this)).setTexSize(textureWidthIn, textureHeightIn);
        this.head.setPos(0.0F, 0.0F + yOffset, 0.0F);
        this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, modelSize);
        this.hat = (new ModelRenderer(this, 32, 0)).setTexSize(textureWidthIn, textureHeightIn);
        this.hat.addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, modelSize + 0.45F);
        this.hat.visible = false;
        ModelRenderer nose = (new ModelRenderer(this)).setTexSize(textureWidthIn, textureHeightIn);
        nose.setPos(0.0F, yOffset - 2.0F, 0.0F);
        nose.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, modelSize);
        this.head.addChild(nose);
        this.body = (new ModelRenderer(this)).setTexSize(textureWidthIn, textureHeightIn);
        this.body.setPos(0.0F, 0.0F + yOffset, 0.0F);
        this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, modelSize);

        this.jacket = (new ModelRenderer(this)).setTexSize(textureWidthIn, textureHeightIn);
        this.jacket.setPos(0.0F, 0.0F + yOffset, 0.0F);
        this.jacket.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, modelSize + 0.5F);

        this.arms = (new ModelRenderer(this)).setTexSize(textureWidthIn, textureHeightIn);
        this.arms.setPos(0.0F, 0.0F + yOffset + 2.0F, 0.0F);
        this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, modelSize);
        ModelRenderer upperArm = (new ModelRenderer(this, 44, 22)).setTexSize(textureWidthIn, textureHeightIn);
        upperArm.mirror = true;
        upperArm.addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, modelSize);
        this.arms.addChild(upperArm);
        this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, modelSize);

        this.rightLeg = (new ModelRenderer(this, 0, 22)).setTexSize(textureWidthIn, textureHeightIn);
        this.rightLeg.setPos(-2.0F, 12.0F + yOffset, 0.0F);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);

        this.leftLeg = (new ModelRenderer(this, 0, 22)).setTexSize(textureWidthIn, textureHeightIn);
        this.leftLeg.mirror = true;
        this.leftLeg.setPos(2.0F, 12.0F + yOffset, 0.0F);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);

        this.rightArm = (new ModelRenderer(this, 40, 46)).setTexSize(textureWidthIn, textureHeightIn);
        this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);
        this.rightArm.setPos(-5.0F, 2.0F + yOffset, 0.0F);

        this.leftArm = (new ModelRenderer(this, 40, 46)).setTexSize(textureWidthIn, textureHeightIn);
        this.leftArm.mirror = true;
        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);
        this.leftArm.setPos(5.0F, 2.0F + yOffset, 0.0F);
    }

    @Override
    protected Iterable<ModelRenderer> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.arms, this.jacket));
    }

    @Override
    public void prepareMobModel(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {
        BipedModel.ArmPose mainhandArmPose = getArmPoseForHand(entityIn, Hand.MAIN_HAND);
        BipedModel.ArmPose offhandArmPose = getArmPoseForHand(entityIn, Hand.OFF_HAND);
        // check if the mainhand arm pose is "two-handed" - for example, using a bow or a crossbow
        // if so, we want to make sure the offhand is not holding an item to allow it to make up the mainhand pose
        if (mainhandArmPose.isTwoHanded()) {
            offhandArmPose = entityIn.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
        }

        if (entityIn.getMainArm() == HandSide.RIGHT) {
            this.rightArmPose = mainhandArmPose;
            this.leftArmPose = offhandArmPose;
        } else {
            this.rightArmPose = offhandArmPose;
            this.leftArmPose = mainhandArmPose;
        }
        super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTick);
    }

    private BipedModel.ArmPose getArmPoseForHand(T entityIn, Hand hand) {
        ItemStack heldItem = entityIn.getItemInHand(hand);
        if (heldItem.isEmpty()) {
            return BipedModel.ArmPose.EMPTY;
        } else {
            if (entityIn.getUsedItemHand() == hand && entityIn.getTicksUsingItem() > 0) {
                UseAction useaction = heldItem.getUseAnimation();
                if (useaction == UseAction.BLOCK) {
                    return BipedModel.ArmPose.BLOCK;
                }

                if (useaction == UseAction.BOW) {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAction.SPEAR) {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAction.CROSSBOW && hand == entityIn.getUsedItemHand()) {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!entityIn.swinging && heldItem.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(heldItem)) {
                return BipedModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedModel.ArmPose.ITEM;
        }
    }

    @Override
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        AbstractIllagerEntity.ArmPose illagerArmPose = entityIn.getArmPose();
        this.arms.y = 3.0F;
        this.arms.z = -1.0F;
        this.arms.xRot = -0.75F;
        this.jacket.copyFrom(body);
        boolean isWearingChestplateOrLeggings = entityIn.getItemBySlot(EquipmentSlotType.CHEST).getItem() instanceof ArmorItem || entityIn.getItemBySlot(EquipmentSlotType.LEGS).getItem() instanceof ArmorItem;
        this.jacket.visible = !isWearingChestplateOrLeggings;
        boolean crossedArms = illagerArmPose == AbstractIllagerEntity.ArmPose.CROSSED;
        this.arms.visible = crossedArms;
        this.leftArm.visible = !crossedArms;
        this.rightArm.visible = !crossedArms;
        if (crossedArms) {
            this.leftArm.y = 3.0F;
            this.leftArm.z = -1.0F;
            this.leftArm.xRot = -0.75F;
            this.rightArm.y = 3.0F;
            this.rightArm.z = -1.0F;
            this.rightArm.xRot = -0.75F;
        }
        switch (illagerArmPose) {
            case ATTACKING:
                if(!entityIn.isUsingItem()){
                    if (entityIn.getMainHandItem().isEmpty()) {
                        // use "zombie arm pose" - both arms raised to shoulder level
                        // this is necessary because otherwise Illagers look like they're doing a Nazi salute when attacking with no weapon
                        ModelHelper.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
                    } else {
                        // use "attacking pose" - the mainhand is raised to strike while the offhand swings back and forth
                        ModelHelper.swingWeaponDown(this.rightArm, this.leftArm, entityIn, this.attackTime, ageInTicks);
                    }
                }

                break;
            case CELEBRATING:
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.rightArm.xRot = MathHelper.cos(ageInTicks * 0.6662F) * 0.05F;
                this.rightArm.zRot = 2.670354F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.leftArm.xRot = MathHelper.cos(ageInTicks * 0.6662F) * 0.05F;
                this.leftArm.zRot = -2.3561945F;
                this.leftArm.yRot = 0.0F;
                break;
            case SPELLCASTING:
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.rightArm.xRot = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
                this.leftArm.xRot = MathHelper.cos(ageInTicks * 0.6662F) * 0.25F;
                this.rightArm.zRot = 2.3561945F;
                this.leftArm.zRot = -2.3561945F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
                break;
            default:
                break;
        }
    }
}