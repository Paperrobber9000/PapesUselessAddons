package com.pape.uselessaddons.utils;

import com.pape.uselessaddons.mixin.meteor.accessor.MeteorTextHudAccessor;

import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class TextHudPresetUtil {
	
	private TextHudPresetUtil() {}

	public static final HudElementInfo<TextHud>.Preset SCORE;
	
	static {
		SCORE = addPreset("[PUA] Score", "Score: #1{score}");
	}
	
	private static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
        return MeteorTextHudAccessor.addPreset(title, text);
    }
	
}
