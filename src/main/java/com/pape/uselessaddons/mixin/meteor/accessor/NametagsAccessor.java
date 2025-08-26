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
    @Accessor("scale")
    Setting<Double> getScale();

    @Accessor("nameColor")
    Setting<SettingColor> getNameColor();

    @Accessor("background")
    Setting<SettingColor> getBackground();
    
    @Accessor("entities")
    Setting<Set<EntityType<?>>> getEntities();
}
