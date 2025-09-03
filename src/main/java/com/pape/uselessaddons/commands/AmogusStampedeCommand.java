package com.pape.uselessaddons.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pape.uselessaddons.modules.AmogusStampede;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class AmogusStampedeCommand extends Command {
	
    public AmogusStampedeCommand() {
        super("amogusstampede", "Triggers the event.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
        	AmogusStampede as = Modules.get().get(AmogusStampede.class);
        	if (!as.isActive()) {
        		Text message = Text.literal("Stampede cannot happen. Module is disabled.");
        		ChatUtils.sendMsg(message);
                return SINGLE_SUCCESS;
        	}
        	if(!as.callStampede()) {
        		Text message = Text.literal("Stampede cannot happen. Screen is undefined.");
        		ChatUtils.sendMsg(message);
        	}
            return SINGLE_SUCCESS;
        });
    }
}
