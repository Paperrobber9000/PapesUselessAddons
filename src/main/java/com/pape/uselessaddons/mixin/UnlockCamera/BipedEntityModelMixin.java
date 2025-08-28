package com.pape.uselessaddons.mixin.UnlockCamera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.pape.uselessaddons.modules.UnlockElytraCam;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.joml.Math;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {
	
    // Prevent arms going crazy when punching
    @ModifyExpressionValue(method = "animateArms", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;pitch:F", opcode = Opcodes.GETFIELD))
    private float unlockedCamera$modifyArmPitch(float pitch) {
        final float PI = (float) Math.PI;
        UnlockElytraCam uec = Modules.get() != null ? Modules.get().get(UnlockElytraCam.class) : null;
        if (uec != null && uec.isActive() && uec.isElytraActivated()) {
            float normalizedPitch = ((pitch + PI) % (2 * PI) + (2 * PI)) % (2 * PI) - PI;
            if (normalizedPitch < -PI / 2) {
                return -PI - normalizedPitch;
            } else if (normalizedPitch > PI / 2) {
                return PI - normalizedPitch;
            } else {
                return normalizedPitch;
            }
        }

        return pitch;
    }
}
