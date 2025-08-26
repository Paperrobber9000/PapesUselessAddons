package com.pape.uselessaddons.utils;

import org.jetbrains.annotations.Nullable;

import com.pape.uselessaddons.settings.SettingsOfEpicness;
import com.pape.uselessaddons.mixin.meteor.accessor.BetterChatAccessor;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BetterChatHideRepeatsUtil {
	BetterChat bc = Modules.get().get(BetterChat.class);
    BetterChatAccessor bca = (BetterChatAccessor) bc;
    public @Nullable Setting<Boolean> antiSpam = null;
    public @Nullable Setting<Integer> antiSpamDepth = null;
    public final IntList lines = new IntArrayList();
    
    Setting<Boolean> hideRepeats;
    Setting<Integer> hideRepeatsDepth;

    int i = 0;
    
    public Text hideRepeats(Text text) {
        if (text == null) {
            return null; // nothing to modify, pass it through
        }
        
        hideRepeatsDepth = SettingsOfEpicness.hideRepeatsDepth;
        String textString = text.getString(); // safer than toString()
        Text returnText = text;

        // Get the last part safely
        Text lastPart = text;
        if (!text.getSiblings().isEmpty()) {
            lastPart = text.getSiblings().get(text.getSiblings().size() - 1);
        }

        Style style = lastPart.getStyle();

        if (style.getColor() != null && style.getColor().getRgb() == Formatting.GRAY.getColorValue() // check for gray at the end
                && textString.endsWith(")") && textString.contains("(")) {

            // split on '(' safely
            String[] split = textString.split("\\("); // escape the regex char!
            try {
                int repeatNumber = Integer.parseInt(split[split.length - 1].replace(")", ""));

                if (repeatNumber >= hideRepeatsDepth.get()) {
                    returnText = Text.literal("");
                }
            } catch (NumberFormatException ignored) {
                // just skip if parsing fails
            }
        }

        return returnText;
    }

}
