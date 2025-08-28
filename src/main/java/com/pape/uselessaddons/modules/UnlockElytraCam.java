package com.pape.uselessaddons.modules;

import com.pape.uselessaddons.PapesUselessAddons;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class UnlockElytraCam extends Module {
private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    public final Setting<Boolean> invertMouse = sgGeneral.add(new BoolSetting.Builder()
    		.name("invert-mouse")
    		.description("When looking upside down, will invert horizontal mouse movement. Is recommended, very intuitive")
            .defaultValue(true)
    		.build()
    		);
	
	public UnlockElytraCam() {
        super(PapesUselessAddons.PUA, "unlock-elytra-camera", "Allows the camera to not be limited in pitch while flying with an elytra.", "search");
    }
	
	public boolean isElytraActivated() {
		return mc.player != null && mc.player.isGliding();
	}
	
	public float truePitch() {
		float pitch = mc.gameRenderer.getCamera().getPitch();
        pitch %= 360;

        if (pitch < 0) pitch += 360;
        if (pitch > 180) pitch -= 360;
        
        return pitch;
	}

}
