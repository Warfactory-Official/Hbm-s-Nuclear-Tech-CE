package com.hbm.blocks.generic;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ParticleUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PartEmitter extends BlockContainer implements IToolable, ITooltipProvider {

	public PartEmitter(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityPartEmitter();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		if(tool == ToolType.HAND_DRILL) {
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if(!(te instanceof TileEntityPartEmitter)) return false;
			TileEntityPartEmitter emitter = (TileEntityPartEmitter) te;
			emitter.effect = (emitter.effect + 1) % TileEntityPartEmitter.effectCount;
			emitter.markDirty();
			return true;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.GOLD + "Use hand drill to cycle special effects");
	}

	@AutoRegister(name = "tileentity_partemitter")
	public static class TileEntityPartEmitter extends TileEntity implements ITickable {

		public static final int range = 150;
		public int effect = 0;
		public static final int effectCount = 4;

		@Override
		public void update() {

			if(!world.isRemote) {

				double x = pos.getX() + 0.5;
				double y = pos.getY() + 0.5;
				double z = pos.getZ() + 0.5;
				NBTTagCompound data = new NBTTagCompound();

				if(effect == 1) {
					ParticleUtil.spawnGasFlame(world, pos.getX() + world.rand.nextDouble(), pos.getY() + 4.5 + world.rand.nextDouble(), pos.getZ() + world.rand.nextDouble(), world.rand.nextGaussian() * 0.2, 0.1, world.rand.nextGaussian() * 0.2);
				}

				if(effect == 2) {
					data.setString("type", "tower");
					data.setFloat("lift", 5F);
					data.setFloat("base", 0.25F);
					data.setFloat("max", 5F);
					data.setInteger("life", 560 + world.rand.nextInt(20));
					data.setInteger("color", 0x404040);
				}

				if(effect == 3) {
					data.setString("type", "tower");
					data.setFloat("lift", 0.5F);
					data.setFloat("base", 1F);
					data.setFloat("max", 10F);
					data.setInteger("life", 750 + world.rand.nextInt(250));

					x = pos.getX() + 0.5 + world.rand.nextDouble() * 3 - 1.5;
					y = pos.getY() + 1;
					z = pos.getZ() + 0.5 + world.rand.nextDouble() * 3 - 1.5;
				}

				if(data.hasKey("type")) {
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
				}
			}
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			return new SPacketUpdateTileEntity(pos, 0, this.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.effect = nbt.getInteger("effect");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("effect", this.effect);
			return super.writeToNBT(nbt);
		}
	}
}
