package com.pape.uselessaddons;

import com.pape.uselessaddons.utils.BetterChatNotifyUtil;
import com.pape.uselessaddons.utils.MeteorStarscriptScoreUtil;
import com.pape.uselessaddons.utils.SystemErrFilterUtil;
import com.pape.uselessaddons.utils.TextHudPresetUtil;
import com.pape.uselessaddons.utils.XpOrbLabelUtil;
import com.mojang.logging.LogUtils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;

import com.pape.uselessaddons.commands.Score;
import com.pape.uselessaddons.modules.RemoveBlockHitboxes;

import org.slf4j.Logger;

public class PapesUselessAddons extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category PUA = new Category("PUA");

    @Override
    public void onInitialize() {
        LOG.info("PUA activad, epic stuff dropping...");
        
        SystemErrFilterUtil.install(); // hides spammy meteor error messages that don't matter
        
        Modules.get().add(new RemoveBlockHitboxes());
        
        Commands.add(new Score());
        
        MeteorClient.EVENT_BUS.subscribe(new XpOrbLabelUtil());
        MeteorClient.EVENT_BUS.subscribe(new BetterChatNotifyUtil());
        
        @SuppressWarnings("unused")	// bc it's just to let the thing know it exists, don't care if it's not used
		HudElementInfo<TextHud>.Preset scorePreset = TextHudPresetUtil.SCORE;
        
        MeteorStarscript.ss.set("score", MeteorStarscriptScoreUtil::score);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(PUA);
    }

    @Override
    public String getPackage() {
        return "com.pape.uselessaddons";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Paperrobber9000", "PapesUselessAddons");
    }
}
