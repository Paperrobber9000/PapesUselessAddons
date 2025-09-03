package com.pape.uselessaddons.modules;

import com.pape.uselessaddons.PapesUselessAddons;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class AmogusStampede extends Module {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	
    private static final class Sprite {
        double x, y;
        double dx, dy; // movement per tick
        int frame;
        int frameCounter;

        Sprite(double x, double y, double dx, double dy, int frame) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.frame = frame;
            this.frameCounter = 0;
        }
    }

    // Things like values that I want it to be, and you'll NEVER be able to change in a config!!! I rule this world with an iron fist
    private final int frameWidth  = 167;
    private final int frameHeight = 231;
    private final int frames      = 12;
    private final int frameTicks  = 1;
    private int spriteCount = 40;
    
    // ARGB values of the base template colors in amogus_spritesheet.png
    private static final int TEMPLATE_OUTLINE= 0xFF00002C; // navy blue = outline
    private static final int TEMPLATE_MAIN   = 0xFFFC1012; // red = main color
    private static final int TEMPLATE_SHADOW = 0xFF0100FC; // blue = shadow
    private static final int TEMPLATE_VISOR1  = 0xFFF8FDF8; // visor1
    private static final int TEMPLATE_VISOR2  = 0xFF03FC03; // visor2
    private static final int TEMPLATE_VISOR3  = 0xFF017B01; // visor3
    private static final int TEMPLATE_VISOR4  = 0xFF042502; // visor4

    private static final Identifier sheet = Identifier.of("papesuselessaddons", "textures/misc/amogus_spritesheet.png");
    private final static Identifier colorSheet = Identifier.of("papesuselessaddons", "textures/misc/amogus_colors.png");
    
    private static final List<int[]> crewmateColors = new ArrayList<>();
    
    private final Random rand = new Random();
    private static final Function<Identifier, RenderLayer> RENDER_LAYER_FACTORY =
    		id -> RenderLayer.getGuiTextured(id);

    private final List<Sprite> sprites = new ArrayList<>();

    // Event control
    private boolean active = false;
    private int ticksActive = 0;
    private int ticksUntilNextEvent = 0;

    private final int minDelayTicks = 5 * 60 * 20;  // 5 minutes in ticks
    private final int maxDelayTicks = 10 * 60 * 20; // 10 minutes in ticks
    private final int eventDurationTicks = 10 * 20; // 10 seconds
    
    private int guiWidth = 0;
    private int guiHeight = 0;
    
    private final double spriteScale = 1.5;

    public AmogusStampede() {
        super(PapesUselessAddons.PUA, "amogus-stampede", "An event will occasionally happen.", "render");
    }
    
    static {
        try {
            ResourceManager resourceManager = client.getResourceManager();
            Resource resource = resourceManager.getResource(colorSheet).get();
            
            // Get the InputStream from the resource
            try (InputStream is = resource.getInputStream()) {
	            NativeImage colorImage = NativeImage.read(is);
	            
	         // Example: pick specific pixels for each crewmate color
                int[][] pixelsToSample = {
                    {37, 27}, // outline color
                    {54, 28}, // main color
                    {18, 104}, // shadow color
                    {116, 40}, // visor1 color
                    {92, 46}, // visor2 color
                    {82, 62}, // visor3 color
                    {64, 54} // visor4 color
                };
                
	            int colorHeight = 184; // Height of each crewmate sprite in the colors asset
	            int colorCount = colorImage.getHeight() / colorHeight;
	
	            for (int i = 0; i < colorCount; i++) {
	                int[] color = new int[pixelsToSample.length];
	                
	            	for (int j = 0; j < pixelsToSample.length; j++) {
	            		int[] coords = pixelsToSample[j];
		                
	                    int x = coords[0];
	                    int y = coords[1] + i*colorHeight;
	                    
	                    color[j] = colorImage.getColorArgb(x, y);
	                    
	                }
	            	
	                crewmateColors.add(color);
	            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static final List<Identifier> recoloredSheets = new ArrayList<>();

    static {
        try {
            ResourceManager resourceManager = client.getResourceManager();
            Resource resource = resourceManager.getResource(sheet).get();
            
            try (InputStream is = resource.getInputStream()) {
                NativeImage baseImage = NativeImage.read(is);

                for (int i = 0; i < crewmateColors.size(); i++) {
                    int[] palette = crewmateColors.get(i);

                    // Copy template to new image
                    NativeImage recolored = new NativeImage(baseImage.getWidth(), baseImage.getHeight(), false);

                    for (int y = 0; y < baseImage.getHeight(); y++) {
                        for (int x = 0; x < baseImage.getWidth(); x++) {
                            int argb = baseImage.getColorArgb(x, y);

                            // Replace based on template color
                            if (argb == TEMPLATE_OUTLINE) {
                                recolored.setColorArgb(x, y, palette[0]); // outline color
                            } else if (argb == TEMPLATE_MAIN) {
                                recolored.setColorArgb(x, y, palette[1]); // main color
                            } else if (argb == TEMPLATE_SHADOW) {
                                recolored.setColorArgb(x, y, palette[2]); // shadow color
                            } else if (argb == TEMPLATE_VISOR1) {
                                recolored.setColorArgb(x, y, palette[3]); // visor1 color
                            } else if (argb == TEMPLATE_VISOR2) {
                                recolored.setColorArgb(x, y, palette[4]); // visor2 color
                            } else if (argb == TEMPLATE_VISOR3) {
                                recolored.setColorArgb(x, y, palette[5]); // visor3 color
                            } else if (argb == TEMPLATE_VISOR4) {
                                recolored.setColorArgb(x, y, palette[6]); // visor4 color
                            } else {
                            	recolored.setColorArgb(x, y, argb); // keep the transparent stuff
                            }
                        }
                    }

                    // Register dynamic texture
                    Identifier texId = Identifier.of("papesuselessaddons", "recolored/crewmate_" + i);
                    client.getTextureManager().registerTexture(texId, new net.minecraft.client.texture.NativeImageBackedTexture(recolored));
                    recoloredSheets.add(texId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] getColorForIndex(int index) {
        return crewmateColors.get(index % crewmateColors.size());
    }

    @Override
    public void onActivate() {
        sprites.clear();
        scheduleNextEvent();
    }

    private void scheduleNextEvent() {
        ticksUntilNextEvent = minDelayTicks + rand.nextInt(maxDelayTicks - minDelayTicks + 1);
        active = false;
        ticksActive = 0;
    }
    
    // Called by the command. If true, startStampede happens, but when returned false the command gives an error message in its respective class.
    public boolean callStampede() {
    	if (guiWidth > 0 && guiHeight > 0) {
    		startStampede(guiWidth, guiHeight, spriteCount);
    		return true;
    	} else {
    		return false;
    	}
    }

    public void startStampede(int guiWidth, int guiHeight, int spriteCount) {

        for (int i = 0; i < spriteCount; i++) {
            int startFrame = rand.nextInt(frames);

            double speed  = 12 + rand.nextDouble() * 8;
            double screenW = mc.getWindow().getScaledWidth();
            double screenH = mc.getWindow().getScaledHeight();
            double angleDegrees = rand.nextDouble() * Math.toDegrees(Math.atan(screenH/screenW)) / 1.5; // angle based on window scale (smar thinkign_)
            double angleRadians = Math.toRadians(angleDegrees);
            
            double startX = Math.min(-frameWidth, -rand.nextInt(guiWidth) - frameWidth - rand.nextDouble() * 200 + (guiWidth + 200)*(angleDegrees/Math.toDegrees(Math.atan(screenH/screenW))));
            double normalStartY = guiHeight - guiHeight*0.4 - frameHeight;
            double startY = rand.nextDouble() * Math.max(frameHeight*-0.5, normalStartY) - 0.75*normalStartY*((angleDegrees)/Math.toDegrees(Math.atan(screenH/screenW)));
            
            double dx = speed * Math.cos(angleRadians); // horizontal movement
            double dy = speed * Math.sin(angleRadians); // vertical movement
            
            sprites.add(new Sprite(startX, startY, dx, dy, startFrame));
        }

        if (spriteCount == this.spriteCount) {
            active = true;
            ticksActive = 0;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (active) {
            ticksActive++;
            
         // Update sprites
            sprites.removeIf(s -> {
                s.x += s.dx;
                s.y += s.dy;
                
                if (++s.frameCounter >= frameTicks) {
                    s.frameCounter = 0;
                    int nextFrame = (s.frame + 1) % frames;
                    
                    double screenW = mc.getWindow().getScaledWidth();
                    double screenH = mc.getWindow().getScaledHeight();
                    
                    boolean onScreen = 
                    	    s.x + frameWidth > 0 &&  // not off left
                    	    s.y + frameHeight > 0 && // not off top
                    	    s.x < screenW &&        // not off right
                    	    s.y  < screenH;         // not off bottom

                    if ((nextFrame == 5 || nextFrame == 11) && onScreen) {
                    	float volumeDecider = (float)(0.05 + rand.nextFloat()*0.75);
                    	float pitchDecider = (float)(1 - rand.nextFloat()*0.25);
                        client.player.playSound(
                        	SoundEvents.ENTITY_HORSE_STEP,  // epic stampeding noises to trample people to death to
                            volumeDecider,                  // volume
                            pitchDecider                    // pitch
                        );
                    }

                    s.frame = nextFrame;
                }
                // Remove if completely offscreen
                return s.x > guiWidth || s.y > guiHeight;
            });
            
            if (ticksActive < eventDurationTicks && sprites.size() < spriteCount && this.spriteCount > 1) {
            	if (guiWidth > 0 && guiHeight > 0) startStampede(guiWidth, guiHeight, spriteCount - sprites.size());
            }

            // After event duration, stop adding new sprites
            if (ticksActive > eventDurationTicks && sprites.isEmpty() && this.isActive()) {
                scheduleNextEvent();
            }
            
        } else {
        	if (spriteCount == 1) {
        		spriteCount = 40;
        	} else if (rand.nextInt(12) == 0) {
            	spriteCount = 1; // I was just thinking about how it would be really funny if there was only one of them once in a while
        	}
            if (--ticksUntilNextEvent <= 0) {
            	if (guiWidth > 0 && guiHeight > 0) startStampede(guiWidth, guiHeight, spriteCount);
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
    	guiWidth = event.screenWidth;
    	guiHeight = event.screenHeight;
    	
        if (!active || sprites.isEmpty()) return;
        
        int renderWidth = (int) Math.round(frameWidth * spriteScale);
        int renderHeight = (int) Math.round(frameHeight * spriteScale);

        final int sheetW = renderWidth * frames;
        for (Sprite s : sprites) {
        	// Each sprite chooses a color sheet (index decided when spawned)
            Identifier tex = recoloredSheets.get(s.hashCode() % recoloredSheets.size());
            
            float u = (float) (s.frame * renderWidth);
            float v = 0f;
            
            // Convert from GUI coordinates to framebuffer coordinates
            double scaleFactor = mc.getWindow().getScaleFactor();
            int xFb = (int) Math.round(s.x * scaleFactor);
            int yFb = (int) Math.round(s.y * scaleFactor);

            event.drawContext.drawTexture(
                RENDER_LAYER_FACTORY, tex,
                xFb, yFb,
                u, v,
                renderWidth, renderHeight,
                sheetW, renderHeight
            );
        }
    }
}

// And if you think this is too much work for a stupid amogus gag, you'd be right! This sucks