/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.data.entities.animals.tfcleopardseal;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.model.entity.HierarchicalAnimatedModel;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFCLeopardSealModel extends HierarchicalAnimatedModel<TFCLeopardSeal> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "seal"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole_body_1 = partdefinition.addOrReplaceChild("whole_body_1", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition whole_body = whole_body_1.addOrReplaceChild("whole_body", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition body = whole_body.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(0, 26).addBox(-5.0F, 0.0F, -4.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 8.0F, -2.0F));

        PartDefinition whole_tail = whole_body.addOrReplaceChild("whole_tail", CubeListBuilder.create().texOffs(38, 26).addBox(-4.0F, -1.0F, -3.0F, 8.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, -2.0F));

        PartDefinition tail_1 = whole_tail.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(0, 44).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition tail = tail_1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(44, 21).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F, 3.0F));

        PartDefinition fin_3 = tail_1.addOrReplaceChild("fin_3", CubeListBuilder.create().texOffs(0, 55).addBox(0.0F, 0.0F, -3.0F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 4.0F, 1.0F));

        PartDefinition fin_4 = tail_1.addOrReplaceChild("fin_4", CubeListBuilder.create().texOffs(22, 51).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 4.0F, 1.0F));

        PartDefinition fin_1 = whole_body.addOrReplaceChild("fin_1", CubeListBuilder.create().texOffs(44, 7).addBox(-10.0F, -2.0F, -1.0F, 10.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-7.0F, -5.0F, -3.0F, 1.5708F, 0.0F, -1.5708F));

        PartDefinition fin_2 = whole_body.addOrReplaceChild("fin_2", CubeListBuilder.create().texOffs(44, 0).addBox(0.0F, -2.0F, -1.0F, 10.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, -5.0F, -3.0F, 1.5708F, 0.0F, 1.5708F));

        PartDefinition muzzle = whole_body.addOrReplaceChild("muzzle", CubeListBuilder.create().texOffs(38, 43).addBox(-4.0F, -7.0F, -3.0F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -7.0F, 1.0F));

        PartDefinition jaw = muzzle.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(44, 14).addBox(-3.0F, -3.0F, 1.0F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.01F)),
                PartPose.offset(0.0F, -8.0F, -1.0F));

        PartDefinition muzzle_r1 = jaw.addOrReplaceChild("muzzle_r1", CubeListBuilder.create().texOffs(34, 58).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition jaw_2 = jaw.addOrReplaceChild("jaw_2", CubeListBuilder.create().texOffs(22, 44).addBox(-3.0F, -5.0F, -2.0F, 6.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 2.0F, 1.0F));

        PartDefinition muzzle_r2 = jaw_2.addOrReplaceChild("muzzle_r2", CubeListBuilder.create().texOffs(46, 58).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)),
                PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static final AnimationDefinition FLOP = AnimationDefinition.Builder.withLength(1.1261F).looping()
            .addAnimation("whole_body_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(4.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.degreeVec(4.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("whole_body_1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.posVec(0.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.posVec(0.0F, 0.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("whole_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("whole_tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 2.5F, 90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(-90.0F, -17.5F, 90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(-90.0F, 22.5F, 90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.degreeVec(-90.0F, 2.5F, 90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(-90.0F, -17.5F, 90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.degreeVec(-90.0F, 22.5F, 90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(-90.0F, 2.5F, 90.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.posVec(1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, -2.5F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(-90.0F, 17.5F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(-90.0F, -22.5F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.degreeVec(-90.0F, -2.5F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(-90.0F, 17.5F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.degreeVec(-90.0F, -22.5F, -90.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(-90.0F, -2.5F, -90.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.posVec(-1.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("muzzle", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.563F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9384F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("muzzle", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.posVec(0.0F, 1.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5317F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7507F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("jaw", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("jaw_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1877F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3754F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6882F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.1261F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition SWIM = AnimationDefinition.Builder.withLength(0.7836F).looping()
            .addAnimation("fin_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1149F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 47.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3135F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -22.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4702F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -27.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6478F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -22.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 22.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1149F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3135F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 7.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4702F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -42.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6478F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("muzzle", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.397F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("muzzle", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("whole_body_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1254F, KeyframeAnimations.degreeVec(0.0F, -12.33F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3448F, KeyframeAnimations.degreeVec(0.0F, 5.03F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4806F, KeyframeAnimations.degreeVec(0.0F, 9.62F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, -5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("whole_tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3761F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -22.03F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 17.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2717F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -15.2F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4284F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -18.05F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0836F, KeyframeAnimations.degreeVec(-0.3384F, 0.4031F, 2.6265F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3761F, KeyframeAnimations.degreeVec(-1.9436F, 1.3529F, 31.1514F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("fin_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-2.1654F, -1.2497F, -29.9764F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1567F, KeyframeAnimations.degreeVec(-0.185F, -0.5079F, -22.9681F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.397F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(-2.1654F, -1.2497F, -29.9764F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("jaw_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7523F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4284F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.7836F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    private final ModelPart whole_body_1;
    private final ModelPart whole_body;
    private final ModelPart body;
    private final ModelPart body_2;
    private final ModelPart whole_tail;
    private final ModelPart tail_1;
    private final ModelPart tail;
    private final ModelPart fin_3;
    private final ModelPart fin_4;
    private final ModelPart fin_1;
    private final ModelPart fin_2;
    private final ModelPart muzzle;
    private final ModelPart jaw;
    private final ModelPart jaw_2;

    public TFCLeopardSealModel(ModelPart root) {
        super(root);
        this.whole_body_1 = root.getChild("whole_body_1");
        this.whole_body = this.whole_body_1.getChild("whole_body");
        this.body = this.whole_body.getChild("body");
        this.body_2 = this.body.getChild("body_2");
        this.whole_tail = this.whole_body.getChild("whole_tail");
        this.tail_1 = this.whole_tail.getChild("tail_1");
        this.tail = this.tail_1.getChild("tail");
        this.fin_3 = this.tail_1.getChild("fin_3");
        this.fin_4 = this.tail_1.getChild("fin_4");
        this.fin_1 = this.whole_body.getChild("fin_1");
        this.fin_2 = this.whole_body.getChild("fin_2");
        this.muzzle = this.whole_body.getChild("muzzle");
        this.jaw = this.muzzle.getChild("jaw");
        this.jaw_2 = this.jaw.getChild("jaw_2");
    }

    @Override
    public void setupAnim(@NotNull TFCLeopardSeal animal, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(animal, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
        if (!animal.isInWater() && animal.onGround()) {
            this.animateWalk(FLOP, limbSwing, limbSwingAmount, 3, 3);
        } else {
            this.animateWalk(SWIM, limbSwing, limbSwingAmount, 1, 1);
        }
    }

}
