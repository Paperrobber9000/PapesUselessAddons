package com.pape.uselessaddons.utils;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.starscript.value.Value;

public class MeteorStarscriptScoreUtil {

	public static Value score() {
        if (mc.getNetworkHandler() == null || mc.player == null) return Value.number(0);

        int score = mc.player.getScore();
        return Value.number(score);
    }
	
}
