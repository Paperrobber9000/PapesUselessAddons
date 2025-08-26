package com.pape.uselessaddons.mixin.meteor.accessor;

import java.util.List;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import net.minecraft.text.Text;

@Mixin(BetterChat.class)
public interface BetterChatAccessor {
    
    @Accessor(value = "antiSpam", remap = false) 
    Setting<Boolean> antiSpam();
    
    @Accessor(value = "antiSpamDepth", remap = false)
    Setting<Integer> antiSpamDepth();
    
    @Accessor(value = "sgFilter", remap = false)
    SettingGroup sgFilter();
    
    @Accessor(value = "filterRegex", remap = false)
    Setting<Boolean> filterRegex();
    
    @Accessor(value = "filterRegexList", remap = false)
    List<Pattern> filterRegexList();
    
    @Invoker("appendAntiSpam")
    Text callAppendAntiSpam(Text text);
    
}
