package com.pape.uselessaddons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import com.pape.uselessaddons.settings.SettingsOfEpicness;
import com.pape.uselessaddons.PapesUselessAddons;
import com.pape.uselessaddons.mixin.meteor.accessor.BetterChatAccessor;

public class BetterChatNotifyUtil {
	
	Module m = null;
	BetterChat bc = Modules.get().get(BetterChat.class);
    BetterChatAccessor bca = (BetterChatAccessor) bc;
	BetterChatHideRepeatsUtil hru = new BetterChatHideRepeatsUtil();
    private final MinecraftClient mc = MinecraftClient.getInstance();
    Logger LOG = PapesUselessAddons.LOG;
	
	private final List<Pattern> notifyRegexList = new ArrayList<>();

    public void compileFilterRegexList() {
    	if (SettingsOfEpicness.notifyRegex == null) return;
        notifyRegexList.clear();

        for (int i = 0; i < SettingsOfEpicness.notifyRegex.get().size(); i++) {
            try {
                notifyRegexList.add(Pattern.compile(SettingsOfEpicness.notifyRegex.get().get(i)));
            } catch (PatternSyntaxException e) {
                String removed = SettingsOfEpicness.notifyRegex.get().remove(i);
                m.error("Removing Invalid regex: %s", removed);
            }
        }
    }
    
    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        Text message = event.getMessage();
        
        if (bca.filterRegex().get()) {
            String messageString = message.getString();
            for (Pattern pattern : bca.filterRegexList()) {
                if (pattern.matcher(messageString).find()) {
                    return;	// If the message would have been filtered by filter regex, return early so there isn't a ghost ping
                }
            }
        }
        
        Setting<Boolean> hr = SettingsOfEpicness.hideRepeats;
        
        if (hr != null && hr.get()) {
            Text antiSpammed = bca.callAppendAntiSpam(message);
            if (antiSpammed != null && antiSpammed.getString().isBlank()) return;
        }

        if (SettingsOfEpicness.notify.get()) {
            String messageString = message.getString();
            compileFilterRegexList();	// Compile here, because trying to do it from the mixin doesn't work. This guarantees the list is updated.
            for (Pattern pattern : notifyRegexList) {
                if (pattern.matcher(messageString).find()) {
                	if (!SettingsOfEpicness.notifySound.get().isEmpty()) {
                		for (int i = 0; i < SettingsOfEpicness.notifySound.get().size(); i++) {
                    		mc.getSoundManager().play(PositionedSoundInstance.master(SettingsOfEpicness.notifySound.get().get(i),
                        			SettingsOfEpicness.notifySoundPitch.get().floatValue(), SettingsOfEpicness.notifySoundVolume.get().floatValue()));
                		}
                	} else {
                        ChatUtils.sendMsg(Text.literal("No notification sound selected! Please add a sound to play when notified in BetterChat settings."));
                	}
                	
                	if (SettingsOfEpicness.notifyHighlight.get()) {
                    	SettingColor highlightColor = SettingsOfEpicness.notifyHighlightColor.get();
                    	TextColor textColor = TextColor.fromRgb(highlightColor.getPacked());
                    	
                	    Text highlighted = highlightMatches(messageString, pattern, textColor);
                	    
                	    event.setMessage(highlighted);
                	}
                }
            }
        }
    }
    
    private Text highlightMatches(String message, Pattern pattern, TextColor highlightColor) {
        Matcher matcher = pattern.matcher(message);
        int lastEnd = 0;
        MutableText result = Text.literal("");

        while (matcher.find()) {
            // Add text before the match (normal color)
            if (matcher.start() > lastEnd) {
                result.append(Text.literal(message.substring(lastEnd, matcher.start())));
            }

            // Add the matched part (highlight color)
            result.append(Text.literal(matcher.group())
                               .setStyle(Style.EMPTY.withColor(highlightColor)));

            lastEnd = matcher.end();
        }

        // Add the rest of the message after the last match
        if (lastEnd < message.length()) {
            result.append(Text.literal(message.substring(lastEnd)));
        }

        return result;
    }

    
    // TODO: The rest of the chat notify code (steal from betterchat and villager roller code rofl)
    
}
