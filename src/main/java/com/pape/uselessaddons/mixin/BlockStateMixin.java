package com.pape.uselessaddons.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.pape.uselessaddons.modules.RemoveBlockHitboxes;

import meteordevelopment.meteorclient.systems.modules.Modules;

import org.spongepowered.asm.mixin.injection.At;


@Mixin(AbstractBlock.AbstractBlockState.class)
public class BlockStateMixin {
	
    // Remove interaction blocking (clicks, etc.)
    @ModifyReturnValue(
    		method = "getRaycastShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;",
        at = @At("RETURN")
    )
    private VoxelShape removeBlockRaycastShape(VoxelShape original, BlockView world, BlockPos pos) {
    	RemoveBlockHitboxes rbh = Modules.get() != null ? Modules.get().get(RemoveBlockHitboxes.class) : null;

        if (rbh != null && rbh.isActive() && RemoveBlockHitboxes.shouldRemoveHitbox(((BlockState)(Object)this).getBlock())) {
            return VoxelShapes.empty();
        }
        return original;
    }

    // Remove visible outline when hovering
    @ModifyReturnValue(
        method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
        at = @At("RETURN")
    )
    private VoxelShape removeBlockOutlineShape(VoxelShape original, BlockView world, BlockPos pos, ShapeContext context) {
    	RemoveBlockHitboxes rbh = Modules.get() != null ? Modules.get().get(RemoveBlockHitboxes.class) : null;

        if (rbh != null && rbh.isActive() && RemoveBlockHitboxes.shouldRemoveHitbox(((BlockState)(Object)this).getBlock())) {
        	return net.minecraft.util.shape.VoxelShapes.cuboid(0, -1, 0, 1, -0.999, 1);
        }
        return original;
    }
    
    // Make sure the collision is the same with no phasing (This took several hours to figure out, screw whatever causes the phasing to happen)
    @ModifyReturnValue(
    	    method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
    	    at = @At("RETURN")
    	)
    	private VoxelShape keepEntityCollision(VoxelShape original, BlockView world, BlockPos pos, ShapeContext context) {
    	    RemoveBlockHitboxes rbh = Modules.get() != null ? Modules.get().get(RemoveBlockHitboxes.class) : null;

    	    if (rbh != null && rbh.isActive() && RemoveBlockHitboxes.shouldRemoveHitbox(((BlockState)(Object)this).getBlock())) {
    	    	
    	    	BlockState state = (BlockState)(Object)this;
	    		Block b = state.getBlock();
	    		BlockState stateIfItWasGood = b.getDefaultState();
	    		return stateIfItWasGood.getCollisionShape(world, pos);
    	    	
    	    }
    	    return original;
    	}
    
}