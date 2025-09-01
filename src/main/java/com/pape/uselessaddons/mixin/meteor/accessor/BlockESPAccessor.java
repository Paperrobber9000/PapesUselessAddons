package com.pape.uselessaddons.mixin.meteor.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import net.minecraft.block.Block;

@Mixin(BlockESP.class)
public interface BlockESPAccessor {
	
	@Invoker("getBlockData")
	ESPBlockData blockData(Block block);
	
}
