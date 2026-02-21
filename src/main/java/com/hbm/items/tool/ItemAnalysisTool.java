package com.hbm.items.tool;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IAnalyzable;
import com.hbm.items.ItemBakedBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemAnalysisTool extends ItemBakedBase {

    public ItemAnalysisTool(String s) {
        super(s);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos targetPos = pos;
        Block block = world.getBlockState(targetPos).getBlock();

        if (block instanceof BlockDummyable) {
            int[] corePos = ((BlockDummyable) block).findCore(world, targetPos.getX(), targetPos.getY(), targetPos.getZ());

            if (corePos != null) {
                targetPos = new BlockPos(corePos[0], corePos[1], corePos[2]);
                block = world.getBlockState(targetPos).getBlock();
            }
        }

        if (block instanceof IAnalyzable) {
            List<String> debug = ((IAnalyzable) block).getDebugInfo(world, targetPos);

            if (debug != null && !world.isRemote) {
                for (String line : debug) {
                    TextComponentString component = new TextComponentString(line);
                    component.getStyle().setColor(TextFormatting.YELLOW);
                    player.sendMessage(component);
                }
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
}
