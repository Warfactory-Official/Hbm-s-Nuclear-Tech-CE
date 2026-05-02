package com.hbm.blocks.machine.rbmk;

import com.hbm.api.block.IToolable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class RBMKKeyPad extends RBMKMiniPanelBase implements IToolable {

	public RBMKKeyPad(String s) {
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRBMKKeyPad();
	}

	@Override
	public boolean onScrew(World world,EntityPlayer player,int x,int y,int z,EnumFacing side,float fX,float fY,float fZ,EnumHand hand,ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float hitX,float hitY,float hitZ) {
		if(world.isRemote) return true;
		if(player.isSneaking()) return false;

		if(hitX != 0 && hitX != 1 && hitZ != 0 && hitZ != 1 && side != EnumFacing.DOWN && side != EnumFacing.UP) {

			TileEntityRBMKKeyPad tile = (TileEntityRBMKKeyPad) world.getTileEntity(pos);
			int meta = world.getBlockState(pos).getValue(FACING).getIndex();

			int indexHit = 0;

			if(meta == 2 && hitX < 0.5) indexHit = 1;
			if(meta == 3 && hitX > 0.5) indexHit = 1;
			if(meta == 4 && hitZ > 0.5) indexHit = 1;
			if(meta == 5 && hitZ < 0.5) indexHit = 1;

			if(hitY < 0.5) indexHit += 2;

			if(!tile.keys[indexHit].active) return false;
			tile.keys[indexHit].click();
		}

		return true;
	}

	/*@SideOnly(Side.CLIENT)
	public void renderInventoryBlock(Block block, int meta, int modelId) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -0.5, 0);
		GlStateManager.rotate(-90, 0, 1, 0);

		for(int i = 0; i < 4; i++) {

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.25, (i / 2) * -0.5 + 0.25, (i % 2) * -0.5 + 0.25);

			GlStateManager.color(1F, 1F, 1F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.rbmk_keypad_tex);
			ResourceManager.rbmk_button.renderPart("Socket");
			GlStateManager.color(0.65F, 0F, 0F);
			ResourceManager.rbmk_button.renderPart("Button");
			GlStateManager.popMatrix();
		}

		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.popMatrix();
	}*/
}
