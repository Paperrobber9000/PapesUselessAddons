package com.pape.uselessaddons.mixin.meteor;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.pape.uselessaddons.settings.SettingsOfEpicness;
import com.pape.uselessaddons.PapesUselessAddons;
import com.pape.uselessaddons.mixin.meteor.accessor.BetterChatAccessor;
import com.pape.uselessaddons.utils.BetterChatNotifyUtil;
import com.pape.uselessaddons.utils.BetterChatHideRepeatsUtil;

import it.unimi.dsi.fastutil.ints.IntList;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.SoundEventListSetting;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(value = BetterChat.class)
public class BetterChatMixin extends Module {
	
	@Unique
	BetterChatHideRepeatsUtil hru = new BetterChatHideRepeatsUtil();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
	
	@Unique
    private SettingGroup sgNotify;
	
//	@Shadow @Final
//	private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public BetterChatMixin(Category category, String name, String description) {
        super(category, name, description);
    }
    
    @Unique @Nullable Setting<Boolean> hideRepeats = null;
    @Unique @Nullable Setting<Integer> hideRepeatsDepth = null;
    
    @Unique @Nullable Setting<Boolean> notify = null;    
    @Unique @Nullable Setting<Boolean> notifyUsername = null;
    @Unique @Nullable Setting<List<String>> notifyRegex = null;
    @Unique @Nullable Setting<List<SoundEvent>> notifySound = null;
    @Unique @Nullable Setting<Double> notifySoundPitch = null;
    @Unique @Nullable Setting<Double> notifySoundVolume = null;
    @Unique @Nullable Setting<Boolean> notifyHighlight = null;
    @Unique @Nullable Setting<SettingColor> notifyHighlightColor = null;
    
    @Unique Logger LOG = PapesUselessAddons.LOG;
    
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lmeteordevelopment/meteorclient/systems/modules/misc/BetterChat;antiSpam:Lmeteordevelopment/meteorclient/settings/Setting;", shift = At.Shift.AFTER)
    , remap = false)
    private void addHideRepeatsSettings(CallbackInfo ci) {
    	
    	BetterChatAccessor bca = (BetterChatAccessor) this;
    	
    	if (SettingsOfEpicness.hideRepeats == null) {
        	hideRepeats = bca.sgFilter().add(
                    new BoolSetting.Builder()
                    .name("hide-repeats")
                    .description("If a certain number of messages are identical, they won't show up in chat.")
                    .defaultValue(false)
                    .visible(bca.antiSpam()::get)
                    .build()
                );
    	}
    	
    	if (SettingsOfEpicness.hideRepeatsDepth == null) {
        	hideRepeatsDepth = bca.sgFilter().add(
                    new IntSetting.Builder()
                    .name("max-repeats-shown")
                    .description("How many message repeats are allowed before new ones get hidden.")
                    .defaultValue(5)
                    .min(1)
                    .sliderMin(1)
                    .visible(hideRepeats::get)
                    .build()
                );
    	}
    	
    	SettingsOfEpicness.hideRepeats = hideRepeats;
    	SettingsOfEpicness.hideRepeatsDepth = hideRepeatsDepth;
    }
    
    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lmeteordevelopment/meteorclient/systems/modules/misc/BetterChat;sgFilter:Lmeteordevelopment/meteorclient/settings/SettingGroup;", shift = At.Shift.AFTER)
    , remap = false)
    private void addNotifySettings(CallbackInfo ci) {
    	
    	if (sgNotify == null) sgNotify = settings.createGroup("Notify");
    	
    	if (SettingsOfEpicness.notify == null) {
    		notify = sgNotify.add(
                    new BoolSetting.Builder()
                    .name("notify")
                    .description("Plays a sound if a chat message matches the regex.")
                    .defaultValue(false)
                    .build()
                );
    	}
    	
    	if (SettingsOfEpicness.notifyUsername == null) {
    		notifyUsername = sgNotify.add(
                    new BoolSetting.Builder()
                    .name("username")
                    .description("Whether to trigger a notification when your username is seen in chat.")
    		        .visible(notify::get)
                    .defaultValue(true)
                    .build()
                );
    	}
    	
    	if (SettingsOfEpicness.notifyRegex == null) {
    		notifyRegex = sgNotify.add(
    				new StringListSetting.Builder()
    		        .name("notification-regex")
    		        .description("Regex used for which chat messages trigger the notification sound.")
    		        .visible(notify::get)
    		        .build()
    		    );
    	}
    	
    	if (SettingsOfEpicness.notifySound == null) {
    		notifySound = sgNotify.add(
    				new SoundEventListSetting.Builder()
    		        .name("sound-to-play")
    		        .description("Sound that will be played when a chat message matches the notification regex.")
    		        .visible(notify::get)
    		        .defaultValue(Collections.singletonList(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value()))
    		        .build()
    		    );
    	}
    	
    	if (SettingsOfEpicness.notifySoundPitch == null) {
    		notifySoundPitch = sgNotify.add(
    				new DoubleSetting.Builder()
    		        .name("sound-pitch")
    		        .description("Playing sound pitch")
    		        .visible(notify::get)
    		        .defaultValue(1.0)
    		        .min(0)
    		        .sliderRange(0, 8)
    		        .build()
    		    );
    	}
    	
    	if (SettingsOfEpicness.notifySoundVolume == null) {
    		notifySoundVolume = sgNotify.add(
    				new DoubleSetting.Builder()
    		        .name("sound-volume")
    		        .description("Playing sound volume")
    		        .visible(notify::get)
    		        .defaultValue(1.0)
    		        .min(0)
    		        .sliderRange(0, 1)
    		        .build()
    		    );
    	}
    	
    	if (SettingsOfEpicness.notifyHighlight == null) {
    		notifyHighlight = sgNotify.add(
                    new BoolSetting.Builder()
                    .name("highlight-notifications")
                    .description("The notifier of a message that matched with the regex will be highlighted in a special color.")
    		        .visible(notify::get)
                    .defaultValue(false)
                    .build()
                );
    	}
    	
    	if (SettingsOfEpicness.notifyHighlightColor == null) {
    		notifyHighlightColor = sgNotify.add(
    				new ColorSetting.Builder()
    		        .name("highlight-color")
    		        .description("The color used to highlight notifications.")
    		        .visible(notifyHighlight::get)
    		        .defaultValue(new SettingColor(147, 167, 255))
    		        .build()
    		    );
    	}
    	
    	SettingsOfEpicness.notify = notify;
    	SettingsOfEpicness.notifyUsername = notifyUsername;
    	SettingsOfEpicness.notifyRegex = notifyRegex;
    	SettingsOfEpicness.notifySound = notifySound;
    	SettingsOfEpicness.notifySoundPitch = notifySoundPitch;
    	SettingsOfEpicness.notifySoundVolume = notifySoundVolume;
    	SettingsOfEpicness.notifyHighlight = notifyHighlight;
    	SettingsOfEpicness.notifyHighlightColor = notifyHighlightColor;
    	
    }
    
    // The two redirects below me cancel the removal of previous messages. It's used to hide repeats properly without immediately resetting itself.
    // It has to modify three different values with two whole mixins to do this, because meteor is a stubborn and hateful beast.
    
    @Redirect(
    	    method = "appendAntiSpam",
    	    at = @At(
    	        value = "INVOKE",
    	        target = "Ljava/util/List;remove(I)Ljava/lang/Object;"
    	    )
    	)
    	private Object skipRemoveCall(List<?> list, int index) {
    	    return null; // do nothing
    	}
    
    @Redirect(
    	    method = "appendAntiSpam",
    	    at = @At(
    	        value = "INVOKE",
    	        target = "Lit/unimi/dsi/fastutil/ints/IntList;removeInt(I)I"
    	    )
    	    , remap = false
    	)
    	private int skipRemoveInt(IntList list, int index) {
    	    // do nothing, return a dummy value
    	    return 0;
    	}
    
    @ModifyReturnValue(method = "appendAntiSpam", at = @At("RETURN"))
    private Text modifyReturnText(Text returnText) {
        Setting<Boolean> hr = SettingsOfEpicness.hideRepeats;

        if (hr != null && hr.get()) {
            Text newText = hru.hideRepeats(returnText);
            return newText;
        }
        return returnText;
    }

    @Inject(
            method = "onMessageReceive",
            at = @At(
                value = "INVOKE",
                target = "Lmeteordevelopment/meteorclient/events/game/ReceiveMessageEvent;setMessage(Lnet/minecraft/text/Text;)V",
                shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
        )
        private void papes$cancelIfBlank(ReceiveMessageEvent event, CallbackInfo ci, @Local LocalRef<Text> message) {
    	Text t = Text.literal("<" + dateFormat.format(new Date()) + "> ").formatted(Formatting.GRAY);
            if (message == null || message.get().getString().replace(t.getString(), "").isBlank() || message.get().equals(t)) {
                event.cancel();
                ci.cancel();
            }
        }
    
}
