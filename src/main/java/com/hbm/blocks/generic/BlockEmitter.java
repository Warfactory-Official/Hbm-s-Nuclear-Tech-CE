package com.hbm.blocks.generic;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;

public class BlockEmitter extends BlockContainer implements IToolable, ITooltipProvider {

	public static final PropertyDirection FACING = BlockDirectional.FACING;

	public BlockEmitter(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityEmitter();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(@NotNull IBlockState state) {
		return false;
	}

	@Override
	public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess world, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote)
			return true;

		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof TileEntityEmitter)) return false;
		TileEntityEmitter emitter = (TileEntityEmitter) te;

		ItemStack held = player.getHeldItem(hand);

		if(!held.isEmpty() && held.getItem() instanceof ItemDye) {
			emitter.color = ItemDye.DYE_COLORS[held.getItemDamage() % ItemDye.DYE_COLORS.length];
			emitter.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			held.shrink(1);
			return true;
		}

		return false;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(!(te instanceof TileEntityEmitter)) return false;
		TileEntityEmitter emitter = (TileEntityEmitter) te;

		if(tool == ToolType.SCREWDRIVER) {
			emitter.girth += 0.125F;
			emitter.markDirty();
			return true;
		}

		if(tool == ToolType.DEFUSER) {
			emitter.girth -= 0.125F;
			if(emitter.girth < 0.125F) emitter.girth = 0.125F;
			emitter.markDirty();
			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			emitter.effect = (emitter.effect + 1) % TileEntityEmitter.effectCount;
			emitter.markDirty();
			return true;
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.GOLD + "Use screwdriver to widen beam");
		list.add(TextFormatting.GOLD + "Use defuser to narrow beam");
		list.add(TextFormatting.GOLD + "Use hand drill to cycle special effects");
		list.add(TextFormatting.GOLD + "Use dye to change color");
	}

	@AutoRegister(name = "tileentity_ntm_emitter")
	public static class TileEntityEmitter extends TileEntityLoadedBase implements ITickable {

		public static final int range = 100;
		public int color;
		public int beam;
		public float girth = 0.5F;
		public int effect = 0;
		public static final int effectCount = 5;

		@Override
		public void update() {

			if(!world.isRemote) {

				EnumFacing dir = world.getBlockState(pos).getValue(FACING);

				if(world.getTotalWorldTime() % 20 == 0) {
					for(int i = 1; i <= range; i++) {

						beam = i;
						BlockPos target = pos.offset(dir, i);

						IBlockState state = world.getBlockState(target);
						if(state.getBlockFaceShape(world, target, dir.getOpposite()) == BlockFaceShape.SOLID) {
							break;
						}
					}
				}

				if(effect == 4 && beam > 0) {

					if(world.getTotalWorldTime() % 5 == 0) {
						long step = (world.getTotalWorldTime() / 5L) % beam;
						double x = pos.getX() + dir.getXOffset() * step + 0.5;
						double y = pos.getY() + dir.getYOffset() * step + 0.5;
						double z = pos.getZ() + dir.getZOffset() * step + 0.5;

						int prevColor = color;
						if(color == 0) {
							color = Color.HSBtoRGB(world.getTotalWorldTime() / 50.0F, 0.5F, 0.25F) & 16777215;
						}

						NBTTagCompound data = new NBTTagCompound();
						data.setString("type", "plasmablast");
						data.setFloat("r", ((float) ((color & 0xff0000) >> 16)) / 256F);
						data.setFloat("g", ((float) ((color & 0x00ff00) >> 8)) / 256F);
						data.setFloat("b", ((float) ((color & 0x0000ff))) / 256F);
						data.setFloat("scale", girth * 5);

						int meta = dir.getIndex();
						if(meta == 2) data.setFloat("pitch", 90);
						if(meta == 3) data.setFloat("pitch", -90);
						if(meta == 4) { data.setFloat("pitch", 90); data.setFloat("yaw", 90); }
						if(meta == 5) { data.setFloat("pitch", -90); data.setFloat("yaw", 90); }

						PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z),
								new TargetPoint(world.provider.getDimension(), x, y, z, 100));

						color = prevColor;
					}
				}

				networkPackNT(150);
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.color = nbt.getInteger("color");
			this.girth = nbt.getFloat("girth");
			this.effect = nbt.getInteger("effect");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("color", this.color);
			nbt.setFloat("girth", this.girth);
			nbt.setInteger("effect", this.effect);
			return super.writeToNBT(nbt);
		}

		@Override
		public AxisAlignedBB getRenderBoundingBox() {
			return TileEntity.INFINITE_EXTENT_AABB;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(this.beam);
			buf.writeInt(this.color);
			buf.writeFloat(this.girth);
			buf.writeInt(this.effect);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			this.beam = buf.readInt();
			this.color = buf.readInt();
			this.girth = buf.readFloat();
			this.effect = buf.readInt();
		}
	}
}
