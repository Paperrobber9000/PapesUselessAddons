package com.pape.uselessaddons.mixin.meteor.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

@Mixin(value = MeteorTextHud.class, remap = false)
public interface MeteorTextHudAccessor {

	@Invoker("addPreset")
	static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
		throw new AssertionError();
	}
	
}
