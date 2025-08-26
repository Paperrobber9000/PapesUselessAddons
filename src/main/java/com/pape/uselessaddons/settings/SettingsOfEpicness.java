package com.pape.uselessaddons.settings;

import java.util.List;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.sound.SoundEvent;

public class SettingsOfEpicness {
	
	// This is where we keep settings for stuff! What a handy class :)

	// Nametags
    public static Setting<Boolean> showXpOrbValues;
    
    // Better Chat
    public static Setting<Boolean> hideRepeats;
    public static Setting<Integer> hideRepeatsDepth;
    
    public static Setting<Boolean> notify;
    public static Setting<List<String>> notifyRegex;
    public static Setting<List<SoundEvent>> notifySound;
    public static Setting<Double> notifySoundPitch;
    public static Setting<Double> notifySoundVolume;
    public static Setting<Boolean> notifyHighlight;
    public static Setting<SettingColor> notifyHighlightColor;
    
}
