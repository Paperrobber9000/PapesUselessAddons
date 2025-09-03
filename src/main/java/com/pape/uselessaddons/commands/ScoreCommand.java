package com.pape.uselessaddons.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ScoreCommand extends Command {
	
    public ScoreCommand() {
        super("score", "Tells you your current score.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
        	int score = mc.player.getScore();
        	Text message = Text.literal("")
        	        .append(Text.literal("Current score: ").formatted(Formatting.GRAY))
        	        .append(Text.literal(String.valueOf(score)));
        	ChatUtils.sendMsg(message);
            return SINGLE_SUCCESS;
        });
    }
}
