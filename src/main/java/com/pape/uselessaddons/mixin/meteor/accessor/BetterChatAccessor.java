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
    
    @Accessor("antiSpam")
    Setting<Boolean> antiSpam();
    
    @Accessor("antiSpamDepth")
    Setting<Integer> antiSpamDepth();
    
    @Accessor("sgFilter")
    SettingGroup sgFilter();
    
    @Accessor("filterRegex")
    Setting<Boolean> filterRegex();
    
    @Accessor("filterRegexList")
    List<Pattern> filterRegexList();
    
    @Invoker("appendAntiSpam")
    Text callAppendAntiSpam(Text text);
    
}
