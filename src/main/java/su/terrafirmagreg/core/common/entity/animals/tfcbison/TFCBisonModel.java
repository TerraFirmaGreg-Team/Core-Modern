/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.entity.animals.tfcbison;

import org.jetbrains.annotations.NotNull;

import com.mojang.math.Constants;

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

public class TFCBisonModel extends HierarchicalAnimatedModel<TFCBison> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "bison"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole_body = partdefinition.addOrReplaceChild("whole_body", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.0F));

        PartDefinition body = whole_body.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 42).addBox(-9.5F, -12.0F, -19.0F, 19.0F, 24.0F, 18.0F, new CubeDeformation(0.5F))
                .texOffs(0, 0).addBox(-8.5F, -8.0F, -18.0F, 17.0F, 17.0F, 25.0F, new CubeDeformation(0.5F))
                .texOffs(66, 0).addBox(-8.5F, -8.0F, 8.0F, 17.0F, 2.0F, 5.0F, new CubeDeformation(0.5F))
                .texOffs(56, 90).addBox(-8.5F, -8.0F, 0.0F, 17.0F, 17.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 17.0F));

        PartDefinition piece_r1 = tail.addOrReplaceChild("piece_r1", CubeListBuilder.create().texOffs(104, 68).addBox(-2.5F, 0.1889F, -1.3354F, 6.0F, 17.0F, 1.0F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 84).addBox(-5.5F, -7.5F, -10.5F, 11.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(84, 31).addBox(-3.5F, 0.5F, -11.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(76, 44).addBox(-6.5F, -10.5F, -11.5F, 13.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.5F, -17.4F));

        PartDefinition horn_1 = head.addOrReplaceChild("horn_1", CubeListBuilder.create().texOffs(100, 31).addBox(-3.5F, -6.0F, -0.9F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(90, 36).addBox(-1.5F, -2.0F, -0.9F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -3.5F, -6.6F));

        PartDefinition horn_2 = head.addOrReplaceChild("horn_2", CubeListBuilder.create().texOffs(42, 104).addBox(1.5F, -6.0F, -0.9F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(84, 36).addBox(0.5F, -2.0F, -0.9F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -3.5F, -6.6F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(76, 71).addBox(-2.0F, -2.0F, -4.0F, 3.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.4F, 4.5F, -6.5F));

        PartDefinition leg_1 = whole_body.addOrReplaceChild("leg_1", CubeListBuilder.create().texOffs(42, 84).addBox(-1.9F, -2.0F, -3.0F, 7.0F, 13.0F, 7.0F, new CubeDeformation(0.5F))
                .texOffs(84, 7).addBox(-1.5F, -2.0F, -3.0F, 6.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.5F, 6.0F, -11.0F));

        PartDefinition leg_2 = whole_body.addOrReplaceChild("leg_2", CubeListBuilder.create().texOffs(84, 7).addBox(-2.5F, -2.0F, -3.0F, 6.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.5F, 6.0F, 13.0F));

        PartDefinition leg_3 = whole_body.addOrReplaceChild("leg_3", CubeListBuilder.create().texOffs(42, 84).addBox(-3.1F, -3.0F, -4.0F, 7.0F, 13.0F, 7.0F, new CubeDeformation(0.5F))
                .texOffs(84, 7).addBox(-2.5F, -3.0F, -4.0F, 6.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 7.0F, -10.0F));

        PartDefinition leg_4 = whole_body.addOrReplaceChild("leg_4", CubeListBuilder.create().texOffs(84, 7).addBox(-3.5F, -2.0F, -3.0F, 6.0F, 18.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.5F, 6.0F, 13.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static final AnimationDefinition BISON_RUN = AnimationDefinition.Builder.withLength(0.5165F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(1.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1377F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.degreeVec(3.5F, 0.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4132F, KeyframeAnimations.degreeVec(-1.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(1.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1033F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4132F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1033F, KeyframeAnimations.degreeVec(17.0459F, 2.9589F, -9.5565F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2066F, KeyframeAnimations.degreeVec(26.88F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3443F, KeyframeAnimations.degreeVec(6.9724F, -1.5416F, 12.4061F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.241F, KeyframeAnimations.degreeVec(32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(47.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.241F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1722F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1033F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4477F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_3", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4477F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_4", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1033F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2755F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5165F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition BISON_WALK = AnimationDefinition.Builder.withLength(1.2083F).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.75F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(1.7841F, -0.0779F, 0.9988F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 1.75F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 32.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(17.5F, 0.0F, -0.3067F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -37.8F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("tail", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.625F, KeyframeAnimations.degreeVec(10.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 12.5F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 15.08F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.625F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.19F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -19.27F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -7.8F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_1", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_1", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_2", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_2", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_3", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(-22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_3", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_4", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.degreeVec(22.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("leg_4", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)))
            .build();

    private final ModelPart bison;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart horn_1;
    private final ModelPart horn_2;
    private final ModelPart left_ear;
    private final ModelPart leg_1;
    private final ModelPart leg_2;
    private final ModelPart leg_3;
    private final ModelPart leg_4;

    public TFCBisonModel(ModelPart root) {
        super(root);
        this.bison = root.getChild("whole_body");
        this.body = this.bison.getChild("body");
        this.tail = this.body.getChild("tail");
        this.head = this.body.getChild("head");
        this.horn_1 = this.head.getChild("horn_1");
        this.horn_2 = this.head.getChild("horn_2");
        this.left_ear = this.head.getChild("left_ear");
        this.leg_1 = this.bison.getChild("leg_1");
        this.leg_2 = this.bison.getChild("leg_2");
        this.leg_3 = this.bison.getChild("leg_3");
        this.leg_4 = this.bison.getChild("leg_4");
    }

    @Override
    public void setupAnim(@NotNull TFCBison entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);

        final float speed = getAdjustedLandSpeed(entity);
        if (speed > 1f) {
            this.animateWalk(BISON_RUN, limbSwing, limbSwingAmount, 1f, 2.5f);
        } else {
            this.animateWalk(BISON_WALK, limbSwing, limbSwingAmount, 1f, 5f);
        }

        if (entity.isTelegraphingAttack()) {
            //Note for re-use: telegraph animations should be 1 second long, or the float here should be multiplied by their length
            //animate(entity.telegraphAnimation, BOAR_PREPARE_CHARGE, entity.getTelegraphAnimationProgress());
            this.head.xRot = entity.getTelegraphAttackTick() * Constants.DEG_TO_RAD * -1;
        } else {
            this.head.xRot = headPitch * Constants.DEG_TO_RAD;
            this.head.yRot = headYaw * Constants.DEG_TO_RAD;
        }
    }

}
