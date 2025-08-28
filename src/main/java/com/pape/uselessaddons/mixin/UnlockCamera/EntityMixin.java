package com.pape.uselessaddons.mixin.UnlockCamera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.pape.uselessaddons.modules.UnlockElytraCam;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	
    @Shadow
    public abstract float getPitch();

    @Shadow
    public abstract float getYaw();

    @Shadow
    public abstract void setYaw(float yaw);

    @Shadow
    public abstract void setPitch(float pitch);

    @Shadow
    public float prevYaw;

    @Shadow
    public float prevPitch;

    @Shadow
    @Nullable
    public abstract Entity getVehicle();

    // Prevent pitch from clamping internally
    @WrapOperation(method = "setAngles", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"))
    private float unlockedCameras$normalizePitch(float value, float min, float max, Operation<Float> original, @Local(argsOnly = true, ordinal = 1) float pitch) {
        UnlockElytraCam uec = Modules.get() != null ? Modules.get().get(UnlockElytraCam.class) : null;
        if (uec != null && uec.isActive() && uec.isElytraActivated()) {
            return ((pitch + 180) % 360 + 360) % 360 - 180;
        } else {
            return value;
        }
    }

    // Invert horizontal mouse movement if upside down, and set angle to be the corresponding rightside-up angle when landing upside-down
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void unlockedCamera$changeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        UnlockElytraCam uec = Modules.get() != null ? Modules.get().get(UnlockElytraCam.class) : null;
        if (uec != null && uec.isActive() && uec.isElytraActivated()) {
            ci.cancel();
            float f = (float) cursorDeltaY * 0.15F;
            float g = (float) cursorDeltaX * 0.15F;

            float normalizedPitch = ((this.getPitch() + 180) % 360 + 360) % 360 - 180;
            if ((normalizedPitch > 90 || normalizedPitch < -90) && uec.invertMouse.get()) {
                this.setYaw(this.getYaw() - g);
                this.prevYaw -= g;
            } else {
                this.setYaw(this.getYaw() + g);
                this.prevYaw += g;
            }

            this.setPitch(this.getPitch() + f);
            this.prevPitch += f;
        } else if (uec != null) { // Either module is off or elytra isn't activated. Either way, check if yaw & pitch need to be fixed
            float offsetYaw = ((this.getYaw() + 180) % 360 + 360) % 360;
            float offsetPitch = ((this.getPitch() + 180) % 360 + 360) % 360;
            
            // Normalize pitch
            float normalizedPitch = ((this.getPitch() + 180) % 360 + 360) % 360 - 180;

            if (normalizedPitch > 90 || normalizedPitch < -90) {
            	float fixedYaw = (offsetYaw + 180) % 360 - 180;
            	float fixedPitch = ((offsetPitch + 180) % 360 - 180)*-1; // This has *-1 to fix it looking at the ground when the sky was being looked at and vice versa. Changes values like -45 into 45, etc.
            	
                // Flip both pitch and yaw to what they would be if cam was locked (both 180deg flips)
                this.setYaw(fixedYaw);
                this.setPitch(fixedPitch);
            }
        }
    }

    // Prevent pitch from clamping
    @WrapOperation(method = "setPitch", at = @At(value = "INVOKE", target = "Ljava/lang/Math;clamp(FFF)F"))
    private float unlockedCamera$unlockPitch(float value, float min, float max, Operation<Float> original, @Local(argsOnly = true) float pitch) {
        UnlockElytraCam uec = Modules.get() != null ? Modules.get().get(UnlockElytraCam.class) : null;
        float normalized = ((pitch + 180) % 360 + 360) % 360 - 180;
        if (uec != null && uec.isActive() && !uec.isElytraActivated()) { // Used for landing on the ground
            return Math.max(min, Math.min(max, normalized));
        } else { // Used for disabling the module
        	return pitch;
        }
    }
    
}