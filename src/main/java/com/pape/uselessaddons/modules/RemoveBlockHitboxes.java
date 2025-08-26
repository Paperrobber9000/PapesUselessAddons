package com.pape.uselessaddons.modules;

import java.util.List;
import com.pape.uselessaddons.PapesUselessAddons;

import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class RemoveBlockHitboxes extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    public final Setting<Boolean> allBlocks = sgGeneral.add(new BoolSetting.Builder()
    		.name("all-blocks")
    		.description("Whether to have this work on all blocks, regardless of block list.")
            .defaultValue(false)
    		.build()
    		);
    
    public final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .description("Blocks to remove the hitboxes of.")
            .defaultValue(Blocks.NETHER_PORTAL,Blocks.END_PORTAL,Blocks.END_GATEWAY)
            .onChanged(blocks1 -> {
                if (isActive() && Utils.canUpdate()) onActivate();
            })
            .build()
        );
    
    public RemoveBlockHitboxes() {
        super(PapesUselessAddons.PUA, "remove-block-hitboxes", "Removes the hitboxes from particular blocks, allowing the player to interact through them.", "search");
    }
    
    public static boolean shouldRemoveHitbox(Block block) {
    	RemoveBlockHitboxes module = Modules.get().get(RemoveBlockHitboxes.class);
    	if (module.allBlocks.get()) return true;
        return module.isActive() && module.blocks.get().contains(block);
    }
}
