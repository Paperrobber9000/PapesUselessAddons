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
    	
    	// Don't even bother with anything if notify is off.
    	if (!SettingsOfEpicness.notify.get()) return;
    	
        Text message = event.getMessage();
        
        // If the message would have been filtered by filter regex, return early so there isn't a ghost ping
        if (bca.filterRegex().get()) {
            String messageString = message.getString();
            for (Pattern pattern : bca.filterRegexList()) {
                if (pattern.matcher(messageString).find()) return;	
            }
        }
        
        // If the message would have been hidden by hide repeats, return early so there isn't a ghost ping
        Setting<Boolean> hr = SettingsOfEpicness.hideRepeats;
        if (hr != null && hr.get()) {
            Text antiSpammed = bca.callAppendAntiSpam(message);
            if (antiSpammed != null && antiSpammed.getString().isBlank()) return;
        }
        
        
        String messageString = message.getString();

    	SettingColor highlightColor = SettingsOfEpicness.notifyHighlightColor.get();
    	TextColor textColor = TextColor.fromRgb(highlightColor.getPacked());
	    Text highlightMsg = message;
	    
	    // Booleans that we change to trigger events. Prevents things from happening twice if there's a username AND a regex match in a single message.
	    boolean playNotifSound = false;
	    boolean hasNotifSound = true;
        
        compileFilterRegexList();	// Compile here, because trying to do it from the mixin doesn't work. This guarantees the list is updated.
        if (!notifyRegexList.isEmpty()) {
        	for (Pattern pattern : notifyRegexList) {
                if (pattern.matcher(messageString).find()) {
                	
                	if (!SettingsOfEpicness.notifySound.get().isEmpty()) 
                		playNotifSound = true;
                	else 
                		hasNotifSound = false;
                	
                	if (SettingsOfEpicness.notifyHighlight.get()) 
                	    highlightMsg = highlightMatches(highlightMsg, pattern, textColor);
                }
            }
        }
        
        if (SettingsOfEpicness.notifyUsername.get()) {
            String username = MinecraftClient.getInstance().getSession().getUsername().toLowerCase();
            String[] splitMessage = messageString.split(">");
            String userlessMessage = "";
            
            for (int i = 0; i < splitMessage.length; i++) {
            	if (!(i == 0 && splitMessage[i].toLowerCase().contains(username))) 
            		userlessMessage += splitMessage[i];
            }
            
            if (userlessMessage.toLowerCase().contains(username)) {
            	if (!SettingsOfEpicness.notifySound.get().isEmpty()) 
            		playNotifSound = true;
            	else 
            		hasNotifSound = false;
            	
            	if (SettingsOfEpicness.notifyHighlight.get())
                	highlightMsg = highlightUsername(highlightMsg, username, textColor);
            }
        }
        
        if (playNotifSound) playNotificationSound();
        if (!highlightMsg.equals(message)) event.setMessage(highlightMsg);
        if (!hasNotifSound) ChatUtils.sendMsg(Text.literal("No notification sound selected! Please add a sound to play when notified in BetterChat settings."));
        
    }
    
    private Text highlightMatches(Text message, Pattern pattern, TextColor highlightColor) {
    	String fullStr = message.getString(); // flat text for searching
        Matcher matcher = pattern.matcher(fullStr);
        MutableText result = Text.literal("");
        
        int lastEnd = 0;

        while (matcher.find()) {
            // Add text before the match (normal color)
            if (matcher.start() > lastEnd) {
            	result.append(message.copy().getSiblings().isEmpty()
                        ? Text.literal(fullStr.substring(lastEnd, matcher.start()))
                        : Text.literal(fullStr.substring(lastEnd, matcher.start()))
                              .setStyle(message.getStyle()));
            }

            // Add the matched part (highlight color)
            result.append(Text.literal(matcher.group())
                               .setStyle(Style.EMPTY.withColor(highlightColor)));

            lastEnd = matcher.end();
        }

        // Add the rest of the message after the last match
        if (lastEnd < fullStr.length()) {
            result.append(Text.literal(fullStr.substring(lastEnd))
                    .setStyle(message.getStyle()));
        }

        return result;
    }
    
    private Text highlightUsername(Text message, String username, TextColor highlightColor) {
    	String fullStr = message.getString(); // flat text for searching
    	String fullStrLwr = fullStr.toLowerCase();
        MutableText result = Text.literal("");
        
        String[] splitMessage = fullStrLwr.split(">");
        String userlessMessage = "";
        
        for (int i = 0; i < splitMessage.length; i++) {
        	if (!(i == 0 && splitMessage[i].toLowerCase().contains(username))) 
        		userlessMessage += splitMessage[i];
        	else
        		for (int j = 0; j <= splitMessage[i].length(); j++) {
        			userlessMessage += ".";	// make it the same length as the original message, just without the username
        		}
        }

        int lastEnd = 0;
        int index;

        while ((index = userlessMessage.indexOf(username, lastEnd)) != -1) {
            // Add text before the username (normal color)
            if (index > lastEnd) {
                result.append(message.copy().getSiblings().isEmpty()
                        ? Text.literal(fullStr.substring(lastEnd, index))
                        : Text.literal(fullStr.substring(lastEnd, index))
                              .setStyle(message.getStyle()));
            }

            // Add the username with highlight
            result.append(Text.literal(fullStr.substring(index, index + username.length()))
                    .setStyle(Style.EMPTY.withColor(highlightColor)));

            // Move past this match
            lastEnd = index + username.length();
        }

        // Add the rest of the message
        if (lastEnd < fullStr.length()) {
            result.append(Text.literal(fullStr.substring(lastEnd))
                    .setStyle(message.getStyle()));
        }

        return result;
    }
    
    private void playNotificationSound() {
		for (int i = 0; i < SettingsOfEpicness.notifySound.get().size(); i++) {
    		mc.getSoundManager().play(PositionedSoundInstance.master(SettingsOfEpicness.notifySound.get().get(i),
        			SettingsOfEpicness.notifySoundPitch.get().floatValue(), SettingsOfEpicness.notifySoundVolume.get().floatValue()));
		}
    }
    
}
