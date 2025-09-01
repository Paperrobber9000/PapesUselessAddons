package com.pape.uselessaddons.mixin.UnlockCamera;

import com.pape.uselessaddons.modules.UnlockElytraCam;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

	private float papes$oldYaw, papes$oldPitch;
	
    /* This inject runs at the start of tickMovement, before physics updates.
     * We force the player movement rotation to be right-side-up while keeping
     * the camera (yaw/pitch) as the module wants.
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void unlockElytraCam$preMovement(CallbackInfo ci) {
        UnlockElytraCam uec = Modules.get() != null ? Modules.get().get(UnlockElytraCam.class) : null;
        if (uec == null || !uec.isActive() || !uec.isElytraActivated()) {
        	
            papes$oldPitch = 444;
            /* Unreachable number for pitch, if it's this then 
             * it can only be the situation where we disregard 
             * the old values after landing. This way we dont 
             * have the same yaw/pitch from when we landed 
             * upon the next takeoff.
             */
            
            return;
        }

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Calculate normalized pitch
        float normalizedPitch = ((player.getPitch() + 180f) % 360f + 360f) % 360f - 180f;
        
        // Setting oldPitch to the normalized version so the number doesn't get extremely big, that messes with the speed you can go with elytra which gets super buggy
        papes$oldPitch = normalizedPitch;
        // We don't do this to oldYaw because it doesn't seem to have an effect on speed, but does cause items to snap out of place when the yaw crosses over +-180
        papes$oldYaw = player.getYaw();

        // Only apply for upside-down camera
        if (normalizedPitch > 90f || normalizedPitch < -90f) {
            // Compute fixedYaw/fixedPitch (right-side-up)
            float offsetYaw = ((player.getYaw() + 180f) % 360f + 360f) % 360f;
            float offsetPitch = ((player.getPitch() + 180f) % 360f + 360f) % 360f;
            float fixedYaw = (offsetYaw + 180f) % 360f - 180f;
            float fixedPitch = ((offsetPitch + 180f) % 360f - 180f) * -1f;

            // Apply the server-safe right-side-up rotation for movement
            player.setYaw(fixedYaw);
            player.setPitch(fixedPitch);
        }
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void unlockElytraCam$postMovement(CallbackInfo ci) {
        UnlockElytraCam uec = Modules.get() != null ? Modules.get().get(UnlockElytraCam.class) : null;
        if (uec == null || !uec.isActive() || !uec.isElytraActivated() 
        		|| papes$oldPitch == 444) // Only possible after landing, prevents old yaw/pitch from being used again when the player takes off
        	return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Restore the camera rotation
        player.setYaw(papes$oldYaw);
        player.setPitch(papes$oldPitch);
    }
}
