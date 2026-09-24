package com.hbm.tileentity.machine.pile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.MachinePWRController;
import com.hbm.blocks.machine.pile.BlockPile;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.machine.ItemPileRodMK2;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.FlameCreator;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.tileentity.TileEntityTickingBase;
import com.hbm.util.EnumUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

@AutoRegister
public class TileEntityPileCore extends TileEntityTickingBase {

	public PileOrientation orientation = PileOrientation.NEITHER;

	public int height;
	public int width;
	public int depth;

	public List<PileChannel> fuelChannels = new ArrayList<>();
	public List<PileChannel> ventilationChannels = new ArrayList<>();
	public List<PileChannel> controlChannels = new ArrayList<>();

	public int left;
	public int right;
	public int up;

	public PileSegment[] segments = new PileSegment[0];

	public double highestHeat;
	public static final int MAX_HEAT = 800;
	public static boolean meltingDown = false;

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		height = nbt.getInteger("height");
		width = nbt.getInteger("width");
		depth = nbt.getInteger("depth");
		left = nbt.getInteger("left");
		right = nbt.getInteger("right");
		up = nbt.getInteger("up");

		orientation = EnumUtil.grabEnumSafely(PileOrientation.VALUES, nbt.getInteger("orientation"));

		segments = new PileSegment[width];

		fuelChannels.clear();
		ventilationChannels.clear();
		controlChannels.clear();

		int fuelCount = nbt.getByte("fc");
		int ventCount = nbt.getByte("vc");
		int contCount = nbt.getByte("cc");

		for(int i = 0; i < fuelCount; i++) fuelChannels.add(readChannelFromNBT(nbt, "f" + i));
		for(int i = 0; i < ventCount; i++) ventilationChannels.add(readChannelFromNBT(nbt, "v" + i));
		for(int i = 0; i < contCount; i++) controlChannels.add(readChannelFromNBT(nbt, "c" + i));

		this.recalculateSegments();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("height", height);
		nbt.setInteger("width", width);
		nbt.setInteger("depth", depth);
		nbt.setInteger("left", left);
		nbt.setInteger("right", right);
		nbt.setInteger("up", up);

		nbt.setInteger("orientation", orientation.ordinal());

		int fuelCount = fuelChannels.size();
		int ventCount = ventilationChannels.size();
		int contCount = controlChannels.size();

		nbt.setByte("fc", (byte) fuelCount);
		nbt.setByte("vc", (byte) ventCount);
		nbt.setByte("cc", (byte) contCount);

		for(int i = 0; i < fuelCount; i++) fuelChannels.get(i).writeChannelToNBT(nbt, "f" + i);
		for(int i = 0; i < ventCount; i++) ventilationChannels.get(i).writeChannelToNBT(nbt, "v" + i);
		for(int i = 0; i < contCount; i++) controlChannels.get(i).writeChannelToNBT(nbt, "c" + i);
		return nbt;
	}

	public PileChannel getFuelChannel(int x, int y, int z) { return getChannel(x, y, z, fuelChannels); }
	public PileChannel getVentilationChannel(int x, int y, int z) { return getChannel(x, y, z, ventilationChannels); }
	public PileChannel getControlChannel(int x, int y, int z) { return getChannel(x, y, z, controlChannels); }

	public PileChannel getChannel(int x, int y, int z, List<PileChannel> list) {
		for(PileChannel channel : list) if(channel.entry.compare(x, y, z)) return channel;
		return null;
	}

	public int getFuelChannelNum(PileChannel chan) { return getChannelNum(chan, fuelChannels); }
	public int getVentilationChannelNum(PileChannel chan) { return getChannelNum(chan, ventilationChannels); }
	public int getControlChannelNum(PileChannel chan) { return getChannelNum(chan, controlChannels); }

	public int getChannelNum(PileChannel chan, List<PileChannel> list) {
		return list.indexOf(chan);
	}

	public TileEntityPileCore setupSize(int up, int down, int l, int r, int d) {
		this.height = up + 1 + down;
		this.width = l + 1 + r;
		this.depth = d;
		this.left = l;
		this.right = r;
		this.up = up;
		this.segments = new PileSegment[width];
		return this;
	}

	public List<PileChannel> getChannelList(PileChannelType type) {
		if(type == PileChannelType.FUEL) return this.fuelChannels;
		if(type == PileChannelType.VENTILATION) return this.ventilationChannels;
		return this.controlChannels;
	}

	protected int getMeta(int x, int y, int z) {
		return world.getBlockState(new BlockPos(x, y, z)).getValue(BlockMeta.META);
	}

	protected void setMeta(int x, int y, int z, int meta) {
		BlockPos target = new BlockPos(x, y, z);
		world.setBlockState(target, world.getBlockState(target).withProperty(BlockMeta.META, meta), 3);
	}

	public boolean drillChannel(int x, int y, int z, ForgeDirection dir, EntityPlayer player) {
		int startMeta = getMeta(x, y, z);
		PileChannelType type = PileChannelType.getChannelType(dir, orientation);

		int size =
				type == PileChannelType.CONTROL ? height :
				type == PileChannelType.FUEL ? depth : width;

		List<PileChannel> list = getChannelList(type);

		if(startMeta == BlockPile.META_FUEL_IN || startMeta == BlockPile.META_AIR_IN || startMeta == BlockPile.META_CONTROL) {
			for(int i = 0; i < list.size(); i++) {
				PileChannel chan = list.get(i);
				if(chan.entry.compare(x, y, z) && chan.entry.getDir() == dir) {
					if(chan.type == PileChannelType.FUEL) chan.ejectAll();
					list.remove(i);
					for(int j = 0; j < size; j++) {
						setMeta(x + dir.offsetX * j, y + dir.offsetY * j, z + dir.offsetZ * j, BlockPile.META_DUMMY);
					}
					world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1F, 0.75F);
					recalculateSegments();
					return true;
				}
			}
		}

		boolean error = false;
		for(int i = 0; i < size; i++) {
			int iX = x + dir.offsetX * i;
			int iY = y + dir.offsetY * i;
			int iZ = z + dir.offsetZ * i;
			BlockPos iPos = new BlockPos(iX, iY, iZ);
			if(world.getBlockState(iPos).getBlock() != ModBlocks.pile_block) { MachinePWRController.sendError(world, iPos, "Foreign block in reactor", player); error = true; continue; }
			int meta = getMeta(iX, iY, iZ);
			if(meta == BlockPile.META_EDGE) { MachinePWRController.sendError(world, iPos, "Cannot drill along edge", player); error = true; }
			else if(meta == BlockPile.META_CORE) { MachinePWRController.sendError(world, iPos, "Cannot intersect core", player); error = true; }
			else if(meta == BlockPile.META_CHANNEL) { MachinePWRController.sendError(world, iPos, "Cannot intersect channel", player); error = true; }
			else if(meta != BlockPile.META_DUMMY) { MachinePWRController.sendError(world, iPos, "Cannot intersect channel IO", player); error = true; }
		}

		if(error) return false;

		for(int i = 0; i < size; i++) {
			int iX = x + dir.offsetX * i;
			int iY = y + dir.offsetY * i;
			int iZ = z + dir.offsetZ * i;
			if(i == 0) {
				if(type == PileChannelType.FUEL) setMeta(iX, iY, iZ, BlockPile.META_FUEL_IN);
				if(type == PileChannelType.VENTILATION) setMeta(iX, iY, iZ, BlockPile.META_AIR_IN);
				if(type == PileChannelType.CONTROL) setMeta(iX, iY, iZ, BlockPile.META_CONTROL);
			} else if(i == size - 1) {
				if(type == PileChannelType.FUEL) setMeta(iX, iY, iZ, BlockPile.META_FUEL_OUT);
				if(type == PileChannelType.VENTILATION) setMeta(iX, iY, iZ, BlockPile.META_AIR_OUT);
				if(type == PileChannelType.CONTROL) setMeta(iX, iY, iZ, BlockPile.META_CONTROL);
			} else {
				setMeta(iX, iY, iZ, BlockPile.META_CHANNEL);
			}
		}

		list.add(new PileChannel(x, y, z, dir, size, type));

		world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1F, 1.25F);
		this.markChanged();

		recalculateSegments();

		return true;
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			this.runSimulation();
			this.handleVentilation();
			this.handleMeltdown();

			this.networkPackNT(25);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for(PileChannel chan : this.fuelChannels) chan.ejectAll();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeDouble(this.highestHeat);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.highestHeat = buf.readDouble();
	}

	protected void runSimulation() {

		for(PileChannel chan : this.fuelChannels) {
			if(chan.length <= 0) continue;
			double producedNeutrons = 0;

			for(int i = 0; i < chan.rods.length; i++) {
				ItemStack stack = chan.rods[i];
				if(!stack.isEmpty() && stack.getItem() instanceof ItemPileRodMK2) {
					double neut = ItemPileRodMK2.getReactivity(stack, chan.incomingNeutrons / chan.length);
					producedNeutrons += neut;
					chan.heat += neut * ItemPileRodMK2.getHeatPerNeutron(stack);
					chan.rods[i] = ItemPileRodMK2.react(stack, neut);
				}
			}
			chan.outgoingNeutrons = producedNeutrons;
			chan.incomingNeutrons = 0;
		}

		for(PileSegment seg : this.segments) {
			if(seg == null || seg.segType != PileChannelType.FUEL) continue;
			double outgoing = 0D;
			for(PileChannel chan : seg.channels) outgoing += chan.outgoingNeutrons;
			for(PileChannel chan : seg.channels) chan.incomingNeutrons += outgoing;
		}

		for(int i = 1; i < this.segments.length - 1; i++) {
			PileSegment seg = this.segments[i];
			if(seg == null || seg.segType != PileChannelType.FUEL) continue;
			double outgoing = 0D;
			for(PileChannel chan : seg.channels) outgoing += chan.outgoingNeutrons;

			double mult = 1D;
			for(int j = i - 1; j >= 1; j--) {
				PileSegment neighbor = this.segments[j];
				if(neighbor == null) continue;
				mult *= neighbor.getNeutronMult(this);
				if(neighbor.segType == PileChannelType.FUEL) {
					for(PileChannel chan : neighbor.channels) chan.incomingNeutrons += outgoing * mult;
				}
			}

			mult = 1D;
			for(int j = i + 1; j < this.segments.length - 1; j++) {
				PileSegment neighbor = this.segments[j];
				if(neighbor == null) continue;
				mult *= neighbor.getNeutronMult(this);
				if(neighbor.segType == PileChannelType.FUEL) {
					for(PileChannel chan : neighbor.channels) chan.incomingNeutrons += outgoing * mult;
				}
			}
		}
	}

	protected void handleVentilation() {

		for(PileChannel chan : this.ventilationChannels) {
			if(chan.air <= 0) continue;

			double airCap = (double) chan.air / (double) PileChannel.MAX_AIR;

			for(PileChannel fuel : this.fuelChannels) {
				if(Math.abs(fuel.entry.getPos().getY() - chan.entry.getPos().getY()) <= 1) {
					fuel.heat *= (1D - airCap * 0.05D);
				}
			}

			int toUse = (int) Math.ceil(airCap * 5D);
			chan.air -= toUse;

			if(world.getTotalWorldTime() % 3 != 0) continue;

			double x = chan.entry.getPos().getX() + 0.5 + chan.entry.getDir().offsetX * (this.width - 0.375);
			double y = chan.entry.getPos().getY() + 0.5;
			double z = chan.entry.getPos().getZ() + 0.5 + chan.entry.getDir().offsetZ * (this.width - 0.375);
			Random rand = world.rand;

			NBTTagCompound data = new NBTTagCompound();
			data.setFloat("lift", 1F);
			data.setFloat("base", (0.125F + rand.nextFloat() * 0.125F) * (float) airCap);
			data.setFloat("max", 1F * (float) airCap);
			data.setFloat("strafe", 0.0025F);
			data.setBoolean("noWind", true);
			data.setInteger("life", 20 + rand.nextInt(30));
			data.setInteger("color", 0xa0a0a0);
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Tower, data, x, y, z),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
		}

		for(PileChannel chan : this.fuelChannels) {
			chan.heat *= 0.999;
			if(chan.heat < 20) chan.heat = 20;
		}
	}

	protected void handleMeltdown() {

		this.highestHeat = 0;
		for(PileChannel chan : this.fuelChannels) {
			if(chan.heat > this.highestHeat) this.highestHeat = chan.heat;
		}

		if(this.highestHeat > MAX_HEAT) {
			this.destroy();
			double avgX = 0;
			double avgZ = 0;
			for(PileChannel chan : this.fuelChannels) {
				avgX += chan.entry.getPos().getX() + 0.5 + chan.entry.getDir().offsetX * (chan.length - 1) / 2D;
				avgZ += chan.entry.getPos().getZ() + 0.5 + chan.entry.getDir().offsetZ * (chan.length - 1) / 2D;
			}
			avgX /= this.fuelChannels.size();
			avgZ /= this.fuelChannels.size();
			meltingDown = true;
			world.newExplosion(null, avgX, pos.getY() + up, avgZ, 15F, true, true);
			meltingDown = false;

			for(int i = 0; i < 15; i++) {
				double mY = world.rand.nextDouble() * 0.5 + 1D;
				EntityBulletBaseMK4 fragment = new EntityBulletBaseMK4(world, null, pile_debris, 100F, 0.35F, avgX, pos.getY() + up + 1, avgZ, 0, mY, 0);
				world.spawnEntity(fragment);
			}
		}
	}

	protected void recalculateSegments() {
		this.segments = new PileSegment[width];

		for(PileChannel chan : fuelChannels) {
			int index = getChannelVerticalIndex(chan);
			if(index < 0 || index >= this.segments.length) continue;

			if(this.segments[index] == null) {
				this.segments[index] = new PileSegment(PileChannelType.FUEL).addChan(chan);
			} else {
				if(this.segments[index].segType == PileChannelType.FUEL) this.segments[index].addChan(chan);
			}
		}

		for(PileChannel chan : controlChannels) {
			int index = getChannelVerticalIndex(chan);
			if(index < 0 || index >= this.segments.length) continue;

			if(this.segments[index] == null) {
				this.segments[index] = new PileSegment(PileChannelType.CONTROL).addChan(chan);
			} else {
				if(this.segments[index].segType == PileChannelType.CONTROL) this.segments[index].addChan(chan);
			}
		}
	}

	protected int getChannelVerticalIndex(PileChannel chan) {
		BlockPos entry = chan.entry.getPos();
		ForgeDirection right = chan.entry.getDir().getRotation(ForgeDirection.UP);
		int deltaX = (entry.getX() - pos.getX()) * right.offsetX;
		int deltaZ = (entry.getZ() - pos.getZ()) * right.offsetZ;
		int abs = deltaX == 0 ? deltaZ : deltaX;
		return abs + this.left;
	}

	public void destroy() {
		world.setBlockState(pos, ModBlocks.pile_brick.getDefaultState());
	}

	public enum PileOrientation {
		NORTH_SOUTH,
		EAST_WEST,
		NEITHER;

		public static final PileOrientation[] VALUES = values();

		public static PileOrientation getOrientation(ForgeDirection dir) {
			if(dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH) return NORTH_SOUTH;
			if(dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) return EAST_WEST;
			return NEITHER;
		}
	}

	public class PileChannel {

		public final DirPos entry;
		public final int length;
		public final PileChannelType type;

		public final ItemStack[] rods;
		public double heat = 0D;
		public double outgoingNeutrons = 0D;
		public double incomingNeutrons = 0D;
		public static final int MAX_AIR = 1_000;
		public int air;
		public double control = 1D;

		public PileChannel(int x, int y, int z, ForgeDirection dir) {
			this.entry = new DirPos(x, y, z, dir);
			this.type = PileChannelType.getChannelType(dir, orientation);
			this.length =
					type == PileChannelType.CONTROL ? height :
					type == PileChannelType.FUEL ? depth : width;

			this.rods = new ItemStack[length];
			for(int i = 0; i < length; i++) this.rods[i] = ItemStack.EMPTY;
		}

		public PileChannel(int x, int y, int z, ForgeDirection dir, int length, PileChannelType type) {
			this.entry = new DirPos(x, y, z, dir);
			this.type = type;
			this.length = length;
			this.rods = new ItemStack[length];
			for(int i = 0; i < length; i++) this.rods[i] = ItemStack.EMPTY;
		}

		public void writeChannelToNBT(NBTTagCompound nbt, String name) {
			nbt.setInteger(name + "_x", entry.getPos().getX());
			nbt.setInteger(name + "_y", entry.getPos().getY());
			nbt.setInteger(name + "_z", entry.getPos().getZ());
			nbt.setByte(name + "_d", (byte) entry.getDir().ordinal());

			if(type == PileChannelType.FUEL) {
				NBTTagList list = new NBTTagList();
				for(int i = 0; i < rods.length; i++) {
					if(!rods[i].isEmpty()) {
						NBTTagCompound nbt1 = new NBTTagCompound();
						nbt1.setByte("slot", (byte) i);
						rods[i].writeToNBT(nbt1);
						list.appendTag(nbt1);
					}
				}
				nbt.setTag(name + "items", list);

				nbt.setDouble(name + "heat", heat);
				nbt.setDouble(name + "neutrons", incomingNeutrons);
			}

			if(type == PileChannelType.VENTILATION) {
				nbt.setInteger(name + "air", air);
			}

			if(type == PileChannelType.CONTROL) {
				nbt.setDouble(name + "control", control);
			}
		}

		public void loadItem(ItemStack stack) {
			if(stack.isEmpty()) return;
			if(rods.length <= 0) { dropItem(stack, -1); return; }

			for(int i = 0; i < rods.length; i++) {
				if(rods[i].isEmpty()) {
					rods[i] = stack;
					return;

				} else {
					ItemStack prev = rods[i];
					rods[i] = stack;
					stack = prev;
				}
			}

			dropItem(stack, length);
		}

		public void ejectAll() {
			for(int i = 0; i < this.rods.length; i++) {
				this.dropItem(rods[i], length);
				this.rods[i] = ItemStack.EMPTY;
			}
		}

		public void dropItem(ItemStack stack, int depth) {
			if(stack.isEmpty()) return;
			int x = entry.getPos().getX() + entry.getDir().offsetX * depth;
			int y = entry.getPos().getY();
			int z = entry.getPos().getZ() + entry.getDir().offsetZ * depth;

			if(stack.hasTagCompound() && stack.getTagCompound().hasKey(ItemPileRodMK2.KEY_NBT_DEPLETION)) {
				stack.getTagCompound().removeTag(ItemPileRodMK2.KEY_NBT_DEPLETION);
				if(stack.getTagCompound().isEmpty()) stack.setTagCompound(null);
			}

			EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack);
			world.spawnEntity(item);
		}
	}

	public PileChannel readChannelFromNBT(NBTTagCompound nbt, String name) {
		int x = nbt.getInteger(name + "_x");
		int y = nbt.getInteger(name + "_y");
		int z = nbt.getInteger(name + "_z");
		ForgeDirection dir = ForgeDirection.getOrientation(nbt.getByte(name + "_d"));

		PileChannel chan = new PileChannel(x, y, z, dir);

		if(chan.type == PileChannelType.FUEL) {
			NBTTagList list = nbt.getTagList(name + "items", 10);
			for(int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				byte b0 = nbt1.getByte("slot");
				if(b0 >= 0 && b0 < chan.rods.length) {
					chan.rods[b0] = new ItemStack(nbt1);
				}
			}

			chan.heat = nbt.getDouble(name + "heat");
			chan.incomingNeutrons = nbt.getDouble(name + "neutrons");
		}

		if(chan.type == PileChannelType.VENTILATION) {
			chan.air = nbt.getInteger(name + "air");
		}

		if(chan.type == PileChannelType.CONTROL) {
			chan.control = nbt.getDouble(name + "control");
		}

		return chan;
	}

	public enum PileChannelType {
		FUEL, VENTILATION, CONTROL;

		public static PileChannelType getChannelType(ForgeDirection channelDir, PileOrientation pileOrientation) {

			if(channelDir == ForgeDirection.UP || channelDir == ForgeDirection.DOWN) {
				return PileChannelType.CONTROL;
			} else if(PileOrientation.getOrientation(channelDir) == pileOrientation) {
				return PileChannelType.FUEL;
			} else {
				return PileChannelType.VENTILATION;
			}
		}
	}

	public static class PileSegment {

		public List<PileChannel> channels = new ArrayList<>();
		public final PileChannelType segType;

		public PileSegment(PileChannelType segType) {
			this.segType = segType;
		}

		public PileSegment addChan(PileChannel chan) {
			this.channels.add(chan);
			return this;
		}

		public double getNeutronMult(TileEntityPileCore core) {
			if(this.segType != PileChannelType.CONTROL) return 1D;
			int size = core.depth - 1;
			if(size < 3) return 0D;
			double total = 0D;

			for(PileChannel chan : channels) total += chan.control;
			return MathHelper.clamp(total / size, 0D, 0.5D);
		}
	}

	public static BulletConfig pile_debris;

	public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_STANDARD_EXPLODE = (bullet, mop) -> {
		bullet.world.newExplosion(bullet, bullet.posX, bullet.posY, bullet.posZ, 5F, true, false);
		bullet.setDead();
	};

	public static Consumer<Entity> LAMBDA_FIRE = (bullet) -> {
		if(bullet.world.isRemote && MainRegistry.proxy.me().getDistance(bullet) < 100) FlameCreator.composeEffectClient(bullet.world, bullet.posX, bullet.posY - 0.125, bullet.posZ, FlameCreator.META_FIRE);
	};

	static {
		pile_debris = new BulletConfig().setLife(200).setVel(1F).setGrav(0.1F).setOnUpdate(LAMBDA_FIRE).setOnImpact(LAMBDA_STANDARD_EXPLODE);
	}
}
