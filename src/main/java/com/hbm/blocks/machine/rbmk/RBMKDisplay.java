package com.hbm.blocks.machine.rbmk;

import com.hbm.api.block.IToolable;
import com.hbm.render.model.RBMKMiniPanelBakedModel;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RBMKDisplay extends RBMKMiniPanelBase implements IToolable {


    public RBMKDisplay(String s) {
        super(s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new TileEntityRBMKDisplay();
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) return false;

        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if (tile instanceof TileEntityRBMKDisplay) {
                ((TileEntityRBMKDisplay) tile).rotate();
            }
        }
        return true;
    }
}
