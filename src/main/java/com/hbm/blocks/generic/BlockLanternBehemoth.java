package com.hbm.blocks.generic;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.capability.HbmCapability;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.deco.TileEntityLanternBehemoth;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockLanternBehemoth extends BlockDummyable implements IToolable, ILookOverlay {
    public BlockLanternBehemoth(String s) {
        super(Material.IRON, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return new TileEntityLanternBehemoth();
        return null;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {4, 0, 0, 0, 0, 0};
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if (tool != ToolType.TORCH) return false;
        boolean didRepair = IRepairable.tryRepairMultiblock(world, x, y, z, this, player);

        if(didRepair) {
            HbmCapability.IHBMData data = HbmCapability.getData(player);
            if(data.getReputation() < 25) data.setReputation(data.getReputation() + 1);
        }

        return didRepair;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        IRepairable.addGenericOverlay(event, world, pos.getX(), pos.getY(), pos.getZ(), this);
    }
}
