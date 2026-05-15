/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.entity.animals.tfcmongoose;

import com.mojang.math.Constants;

import net.dries007.tfc.client.model.entity.HierarchicalAnimatedModel;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFCMongooseModel extends HierarchicalAnimatedModel<TFCMongoose> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "mongoose"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition upperpart = body.addOrReplaceChild("upperpart", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 5.0F));

        PartDefinition leg_q = upperpart.addOrReplaceChild("leg_q", CubeListBuilder.create().texOffs(22, 26).addBox(-1.1F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 0.0F, -9.0F));

        PartDefinition leg_w = upperpart.addOrReplaceChild("leg_w", CubeListBuilder.create().texOffs(22, 26).addBox(-0.9F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, 0.0F, -9.0F));

        PartDefinition tail_q = upperpart.addOrReplaceChild("tail_q", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.0F, 2.0F, 0.5061F, 0.0F, 0.0F));

        PartDefinition tail_w = tail_q.addOrReplaceChild("tail_w", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.095F, -1.234F, -0.48F, 0.0F, 0.0F));

        PartDefinition tail_r1 = tail_w.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -8.0F, -6.0F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 9.0F, 2.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition tail_e = tail_w.addOrReplaceChild("tail_e", CubeListBuilder.create(), PartPose.offset(0.0F, 3.875F, 8.35F));

        PartDefinition tail_r2 = tail_e.addOrReplaceChild("tail_r2", CubeListBuilder.create().texOffs(0, 29).addBox(-0.5F, -0.8695F, -0.9914F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.125F, -0.35F, 1.4399F, 0.0F, 0.0F));

        PartDefinition head = upperpart.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -11.0F));

        PartDefinition head_q = head.addOrReplaceChild("head_q", CubeListBuilder.create().texOffs(22, 18).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head_r1 = head_q.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(4, 29).addBox(-1.0F, 7.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -12.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition right_ear = head_q.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-1.0F, -1.0F, -1.0F));

        PartDefinition right_ear_r1 = right_ear.addOrReplaceChild("right_ear_r1",
                CubeListBuilder.create().texOffs(12, 29).addBox(-1.2065F, -0.7767F, -0.3508F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -1.0F, 1.4F, -0.0568F, -0.3827F, 0.1165F));

        PartDefinition left_ear = head_q.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(1.0F, -2.0F, -1.0F));

        PartDefinition left_ear_r1 = left_ear.addOrReplaceChild("left_ear_r1",
                CubeListBuilder.create().texOffs(16, 29).addBox(-0.7935F, -0.7767F, -0.3508F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.5F, 0.0F, 1.4F, -0.0568F, 0.3827F, -0.1165F));

        PartDefinition belly = upperpart.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -6.0F, 4.0F, 5.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.0F, -5.0F));

        PartDefinition leg_e = body.addOrReplaceChild("leg_e", CubeListBuilder.create().texOffs(22, 26).addBox(-0.9F, -1.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, 3.0F, 4.0F));

        PartDefinition leg_r = body.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(22, 26).addBox(-1.1F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 2.0F, 5.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static final AnimationDefinition MONGOOSE_ATTACK = AnimationDefinition.Builder.withLength(0.625F)
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-9.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-0.36F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-1.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 2.13F, -4.43F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 1.0F, -9.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, -0.5F, -7.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, -0.75F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.625F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2083F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-21.8848F, 23.5508F, 9.2914F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-58.9136F, 17.6631F, 6.9685F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, -20.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-65.625F, 0.0F, -15.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(57.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-9.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F, KeyframeAnimations.degreeVec(-6.07F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.625F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.625F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.scaleVec(1.0F, 1.1F, 1.1F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.625F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_SLEEP = AnimationDefinition.Builder.withLength(2.2083F).looping()
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(4.8821F, -1.0809F, 84.9539F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(4.8821F, -1.0809F, 84.9539F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(3.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.posVec(3.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(110.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-30.1726F, 2.5424F, -1.3782F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-104.018F, -14.9031F, 2.64F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-105.2494F, -13.6391F, 7.6318F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-104.018F, -14.9031F, 2.64F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-17.779F, 1.8471F, -3.4383F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-17.8404F, 1.0836F, -5.82F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-17.78F, 1.85F, -3.44F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.posVec(0.0F, -1.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.4106F, 2.3064F, 4.4375F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-32.4709F, 0.9652F, 2.3268F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-32.4106F, 2.3064F, 4.4375F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w", new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.4539F, 1.0809F, 7.6179F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-14.8774F, -1.936F, -22.2472F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 2.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-7.5383F, -7.4904F, -52.061F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(-10.0392F, -7.4902F, -52.0605F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(-7.54F, -7.49F, -52.06F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_WALK = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0.4385F, 7.053F, -7.1071F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0.47F, -6.4586F, 6.5135F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(0.0F, -6.32F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, -18.45F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-1.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_RUN = AnimationDefinition.Builder.withLength(0.5F).looping()
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(4.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2083F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(38.93F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-2.19F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F, KeyframeAnimations.degreeVec(25.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(25.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F, KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125F, KeyframeAnimations.degreeVec(-9.37F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(1.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.375F, KeyframeAnimations.degreeVec(-4.37F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_DRAG = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0.4385F, 7.053F, -7.1071F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0.47F, -6.4586F, 6.5135F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 25.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-22.5F, -1.1F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, -18.45F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(5.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-26.9409F, -5.7358F, -11.125F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-31.9409F, -5.7358F, -11.125F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-26.9409F, -5.7358F, -11.125F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(-1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.posVec(-1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-24.2477F, 6.2797F, 13.6497F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-31.7477F, 6.2797F, 13.6497F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-24.2477F, 6.2797F, 13.6497F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.posVec(1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-2.03F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(57.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(37.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(57.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_SNIFF = AnimationDefinition.Builder.withLength(2.0F)
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(-47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0833F, KeyframeAnimations.degreeVec(-51.2622F, -6.2964F, 7.7846F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F, KeyframeAnimations.degreeVec(-52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5833F, KeyframeAnimations.degreeVec(-51.273F, 7.256F, -6.8996F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7917F, KeyframeAnimations.degreeVec(-34.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.7917F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0833F, KeyframeAnimations.degreeVec(58.835F, -21.717F, -12.696F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1667F, KeyframeAnimations.degreeVec(57.3733F, -17.6861F, -10.9746F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2917F, KeyframeAnimations.degreeVec(54.676F, 1.6994F, -1.5563F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5417F, KeyframeAnimations.degreeVec(52.2957F, 19.1246F, 4.9364F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625F, KeyframeAnimations.degreeVec(47.8429F, 22.701F, 6.7398F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7917F, KeyframeAnimations.degreeVec(36.2708F, 8.693F, 2.2438F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.125F, KeyframeAnimations.degreeVec(-13.0862F, -17.0723F, 3.904F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F, KeyframeAnimations.degreeVec(-0.0115F, -2.4407F, 0.5414F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625F, KeyframeAnimations.degreeVec(-0.0118F, 12.5593F, 0.5384F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.875F, KeyframeAnimations.degreeVec(-0.0116F, -4.9407F, 0.5419F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(57.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0F, KeyframeAnimations.degreeVec(-23.5097F, -24.5948F, 4.6293F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.125F, KeyframeAnimations.degreeVec(-49.7264F, -22.521F, 11.1484F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2083F, KeyframeAnimations.degreeVec(-21.446F, -18.0503F, 8.7761F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.25F, KeyframeAnimations.degreeVec(-26.0557F, -15.815F, 7.59F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.4167F, KeyframeAnimations.degreeVec(-25.4456F, 33.4855F, -17.9672F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5F, KeyframeAnimations.degreeVec(-40.4456F, 33.4855F, -17.9672F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5417F, KeyframeAnimations.degreeVec(-22.9456F, 33.4855F, -17.9672F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5833F, KeyframeAnimations.degreeVec(-35.4456F, 33.4855F, -17.9672F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625F, KeyframeAnimations.degreeVec(-32.9456F, 33.4855F, -17.9672F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(52.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.875F, KeyframeAnimations.degreeVec(42.1504F, 6.7484F, -7.3968F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F, KeyframeAnimations.degreeVec(11.87F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7083F, KeyframeAnimations.degreeVec(30.37F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.875F, KeyframeAnimations.degreeVec(36.1231F, -7.446F, 10.0687F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F, KeyframeAnimations.degreeVec(13.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625F, KeyframeAnimations.degreeVec(23.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5417F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -10.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -11.67F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0833F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 10.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.375F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_SEARCH = AnimationDefinition.Builder.withLength(4.3333F)
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.125F, KeyframeAnimations.degreeVec(-77.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7083F, KeyframeAnimations.degreeVec(-60.15F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.125F, KeyframeAnimations.degreeVec(-62.65F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.625F, KeyframeAnimations.degreeVec(-55.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0F, KeyframeAnimations.degreeVec(-53.49F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.2083F, KeyframeAnimations.degreeVec(-55.99F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.6667F, KeyframeAnimations.degreeVec(-105.13F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(105.28F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(65.28F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.625F, KeyframeAnimations.degreeVec(88.67F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(117.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2917F, KeyframeAnimations.degreeVec(94.71F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.4583F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(83.12F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.375F, KeyframeAnimations.degreeVec(82.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.625F, KeyframeAnimations.degreeVec(115.6444F, -0.7684F, -22.4876F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.7917F, KeyframeAnimations.degreeVec(115.8187F, 0.4505F, 17.9948F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0F, KeyframeAnimations.degreeVec(116.142F, -0.3638F, -15.4571F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.125F, KeyframeAnimations.degreeVec(115.9433F, -0.7451F, -27.7982F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.375F, KeyframeAnimations.degreeVec(115.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.6667F, KeyframeAnimations.degreeVec(134.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(117.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.0833F, KeyframeAnimations.degreeVec(59.09F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-10.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.875F, KeyframeAnimations.degreeVec(6.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.4167F, KeyframeAnimations.degreeVec(-23.38F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(-21.69F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625F, KeyframeAnimations.degreeVec(90.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(100.3424F, -32.0762F, -5.5354F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2083F, KeyframeAnimations.degreeVec(98.1415F, -34.5344F, -6.0801F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.625F, KeyframeAnimations.degreeVec(112.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0F, KeyframeAnimations.degreeVec(73.25F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.3333F, KeyframeAnimations.degreeVec(104.63F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -1.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.625F, KeyframeAnimations.posVec(0.0F, -2.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.posVec(0.0F, -1.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(30.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(30.56F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.125F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7083F, KeyframeAnimations.degreeVec(55.45F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.125F, KeyframeAnimations.degreeVec(10.36F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.625F, KeyframeAnimations.degreeVec(70.61F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0417F, KeyframeAnimations.degreeVec(17.7F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(55.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(33.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(33.33F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.125F, KeyframeAnimations.degreeVec(67.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.7083F, KeyframeAnimations.degreeVec(5.91F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0F, KeyframeAnimations.degreeVec(5.91F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.3333F, KeyframeAnimations.degreeVec(56.31F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.625F, KeyframeAnimations.degreeVec(77.36F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0417F, KeyframeAnimations.degreeVec(3.29F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_e",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.posVec(1.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.posVec(1.0F, -2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r",
                    new AnimationChannel(AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.75F, KeyframeAnimations.posVec(-1.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.875F, KeyframeAnimations.posVec(-1.0F, -4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.3333F, KeyframeAnimations.posVec(0.0F, -3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition MONGOOSE_EAT = AnimationDefinition.Builder.withLength(1.635F)
            .addAnimation("upperpart",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.211F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3692F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2658F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.211F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.7384F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1603F, KeyframeAnimations.degreeVec(15.59F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_e",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.211F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.3692F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.7384F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1603F, KeyframeAnimations.degreeVec(-34.26F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.211F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4219F, KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.7911F, KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9494F, KeyframeAnimations.degreeVec(75.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0549F, KeyframeAnimations.degreeVec(62.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1603F, KeyframeAnimations.degreeVec(75.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.3186F, KeyframeAnimations.degreeVec(27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_w",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4219F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1603F, KeyframeAnimations.degreeVec(-27.4777F, 1.1541F, 2.2178F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_q",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.4219F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.1603F, KeyframeAnimations.degreeVec(-27.4777F, -1.1541F, -2.2178F), AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_r", new AnimationChannel(AnimationChannel.Targets.ROTATION, new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.635F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    private final ModelPart body;
    private final ModelPart upperpart;
    private final ModelPart leg_q;
    private final ModelPart leg_w;
    private final ModelPart tail_q;
    private final ModelPart tail_w;
    private final ModelPart tail_e;
    private final ModelPart head;
    private final ModelPart head_q;
    private final ModelPart right_ear;
    private final ModelPart left_ear;
    private final ModelPart belly;
    private final ModelPart leg_e;
    private final ModelPart leg_r;

    public TFCMongooseModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.upperpart = this.body.getChild("upperpart");
        this.leg_q = this.upperpart.getChild("leg_q");
        this.leg_w = this.upperpart.getChild("leg_w");
        this.tail_q = this.upperpart.getChild("tail_q");
        this.tail_w = this.tail_q.getChild("tail_w");
        this.tail_e = this.tail_w.getChild("tail_e");
        this.head = this.upperpart.getChild("head");
        this.head_q = this.head.getChild("head_q");
        this.right_ear = this.head_q.getChild("right_ear");
        this.left_ear = this.head_q.getChild("left_ear");
        this.belly = this.upperpart.getChild("belly");
        this.leg_e = this.body.getChild("leg_e");
        this.leg_r = this.body.getChild("leg_r");
    }

    @Override
    public void setupAnim(TFCMongoose entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        this.animate(entity.walkingAnimation, MONGOOSE_RUN, ageInTicks, getAdjustedLandSpeed(entity));
        this.animate(entity.eatingAnimation, MONGOOSE_EAT, ageInTicks);
        this.animate(entity.searchingAnimation, MONGOOSE_SEARCH, ageInTicks);
        this.animate(entity.sniffingAnimation, MONGOOSE_SNIFF, ageInTicks);
        this.animate(entity.draggingAnimation, MONGOOSE_DRAG, ageInTicks);

        if (!entity.searchingAnimation.isStarted() && !entity.sniffingAnimation.isStarted() && !entity.draggingAnimation.isStarted() && !entity.eatingAnimation.isStarted()) {
            this.head.xRot = headPitch * Constants.DEG_TO_RAD;
            this.head.yRot = netHeadYaw * Constants.DEG_TO_RAD;
        }
    }
}
