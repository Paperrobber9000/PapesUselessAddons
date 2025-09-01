package com.pape.uselessaddons.mixin.meteor;

import com.pape.uselessaddons.modules.RemoveBlockHitboxes;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.pape.uselessaddons.mixin.meteor.accessor.BlockESPAccessor;
import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ESPBlock.class)
public abstract class ESPBlockMixin {
    
    @Shadow private BlockState state;
    
    BlockESP besp = Modules.get().get(BlockESP.class);

    // Intercept ESPBlock.render for blocks affected by RemoveBlockHitboxes.
    @Inject(
    	    method = "render(Lmeteordevelopment/meteorclient/events/render/Render3DEvent;)V",
    	    at = @At("HEAD"),
    	    cancellable = true,
    	    remap = false
    	)
    private void onRender(Render3DEvent event, CallbackInfo ci) {
    	RemoveBlockHitboxes rb = Modules.get() != null ? Modules.get().get(RemoveBlockHitboxes.class) : null;
        if (rb == null || !rb.isActive() || !besp.isActive()) return;

        BlockESPAccessor bespa = (BlockESPAccessor) besp;
        ESPBlock self = (ESPBlock) (Object) this;

        Block block = state.getBlock();
        
        if (!rb.blocks.get().contains(block)) return; // only do affected blocks

        ci.cancel();

        // Try to use vanilla outline shape (spoiler alert this does nothing!!! Eeeeyooooooooo coding sucks)
        BlockPos pos = new BlockPos(self.x, self.y, self.z);
        BlockState vanillaState = mc.world.getBlockState(pos);
        
        VoxelShape shape = vanillaState.getSidesShape(mc.world, pos);
        if (shape.isEmpty()) 
        	shape = VoxelShapes.fullCube();	// VoxelShapes dont wanna show up? I've got just the solution for you! I'ts called... "Fuck You, Have a Whole-Ass Cube!"
                                            // Because trying to get the actual VoxelShape to work is way too much of a pain to implement. This works fine deal with it
        
        ESPBlockData blockData = bespa.blockData(state.getBlock());

        ShapeMode shapeMode = blockData.shapeMode;
        Color lineColor = blockData.lineColor;
        Color sideColor = blockData.sideColor;

        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            event.renderer.box(
                x + minX, y + minY, z + minZ,
                x + maxX, y + maxY, z + maxZ,
                sideColor, // sides
                lineColor, // lines
                shapeMode,
                0
            );
        });
    }
}
