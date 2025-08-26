package com.pape.uselessaddons.utils;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.pape.uselessaddons.settings.SettingsOfEpicness;
import com.pape.uselessaddons.mixin.meteor.accessor.NametagsAccessor;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;

public class XpOrbLabelUtil {
	private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Vector3d pos = new Vector3d();
    public static final Logger log = LogUtils.getLogger();
    public @Nullable Setting<Double> nametagsScale = null;
    public @Nullable Setting<SettingColor> nametagsNameColor = null;
    public @Nullable Setting<SettingColor> nametagsBackground = null;
    Nametags nt = Modules.get().get(Nametags.class);
    NametagsAccessor na = (NametagsAccessor) nt;

    @EventHandler
    private void onRender2D(Render2DEvent event) {
    	
    	Setting<Boolean> s = SettingsOfEpicness.showXpOrbValues;

        if (mc.world == null || mc.player == null || s == null || !s.get() || !nt.isActive()) return;

        nametagsScale = na.getScale();
        nametagsNameColor = na.getNameColor();
        nametagsBackground = na.getBackground();
        boolean showXpOrbValues = s.get(); // add @Accessor for showXpOrbValues in the mixin

        if (!showXpOrbValues) return;
        
        for (ExperienceOrbEntity orb : mc.world.getEntitiesByClass(
                ExperienceOrbEntity.class,
                mc.player.getBoundingBox().expand(64),
                e -> true
            )) {
                String xpValue = String.valueOf(orb.getExperienceAmount());
                
	        boolean shadow = Config.get().customFont.get();
	
	            Utils.set(pos, orb, event.tickDelta);
	            if (pos.x == 0 && pos.z == 0) return;
	            pos.add(0, getHeight(orb), 0);
	            
	
	            if (NametagUtils.to2D(pos, nametagsScale.get())) renderXpAmount(orb, shadow, xpValue);
        }
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());

        height += 0.5;

        return height;
    }

    private void drawBg(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x - 1, y - 1, width + 2, height + 2, nametagsBackground.get());
        Renderer2D.COLOR.render(null);
    }
    
    private void renderXpAmount(Entity entity, boolean shadow, String xpValue) {
               
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(pos);
        String nametag = xpValue + " xp";

        double valueWidth = text.getWidth(nametag, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = valueWidth / 2;
        double heightOffset = heightDown;
        
        
        boolean normalNametagIsThere = na.getEntities().get().contains(EntityType.EXPERIENCE_ORB) // checks if xp orb is in the entities list of nametags, for rendering placement
        	    && entity.isAlive();
        
        if (normalNametagIsThere) heightOffset = heightDown*-0.106f; // This specific number works if theyre the same scale, could not tell you why

        drawBg(-widthHalf, -heightOffset, valueWidth, heightDown);

        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightOffset;

        text.render(nametag, hX, hY, nametagsNameColor.get(), shadow);
        text.end();

        NametagUtils.end();
    }
}
