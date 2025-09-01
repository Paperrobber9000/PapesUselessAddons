package com.pape.uselessaddons.mixin.meteor;

import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.pape.uselessaddons.settings.SettingsOfEpicness;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.EntityType;

@Mixin(value = Nametags.class)
public abstract class NametagsMixin extends Module {
	
    @Final
    private SettingGroup sgMisc;

    public NametagsMixin(Category category, String name, String description) {
        super(category, name, description);
    }
    
    @Unique
    @Nullable Setting<Boolean> showXpOrbValues = null;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lmeteordevelopment/meteorclient/systems/modules/render/Nametags;sgItems:Lmeteordevelopment/meteorclient/settings/SettingGroup;", shift = At.Shift.AFTER)
    , remap = false)
    private void addXpLabelSettings(CallbackInfo ci) {
    	
    	if (sgMisc == null) {
            sgMisc = settings.createGroup("Miscellaneous");
        }
    	
    	if (showXpOrbValues == null) {
        	showXpOrbValues = sgMisc.add(
                    new BoolSetting.Builder()
                        .name("xp-orb-labels")
                        .description("Displays XP value above experience orbs.")
                        .defaultValue(false)
                        .build()
                );
    	}
    	
    	SettingsOfEpicness.showXpOrbValues = showXpOrbValues;
    }

    @Unique
    public Setting<Boolean> getShowXpOrbValuesInstance() {
        return showXpOrbValues;
    }
    
    @Shadow(remap = false)
    public Setting<Set<EntityType<?>>> entities;

	@Shadow(remap = false)
	public Setting<Double> scale;

	@Shadow(remap = false)
	public Setting<SettingColor> nameColor;

	@Shadow(remap = false)
	public Setting<SettingColor> background;
    
	
    
}
