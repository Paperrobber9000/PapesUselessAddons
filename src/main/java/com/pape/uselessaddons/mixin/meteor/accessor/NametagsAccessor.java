package com.pape.uselessaddons.mixin.meteor.accessor;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.EntityType;

@Mixin(Nametags.class)
public interface NametagsAccessor {
    @Accessor(value = "scale", remap = false)
    Setting<Double> getScale();

    @Accessor(value = "nameColor", remap = false)
    Setting<SettingColor> getNameColor();

    @Accessor(value = "background", remap = false)
    Setting<SettingColor> getBackground();
    
    @Accessor(value = "entities", remap = false)
    Setting<Set<EntityType<?>>> getEntities();
}
