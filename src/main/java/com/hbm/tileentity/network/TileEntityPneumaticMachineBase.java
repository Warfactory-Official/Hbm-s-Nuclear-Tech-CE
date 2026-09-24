package com.hbm.tileentity.network;

import com.hbm.api.ntl.IPneumaticConnector;
import com.hbm.api.ntl.StackCache;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PneumaticNetwork;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityPneumaticMachineBase extends TileEntityMachineBase implements ITickable, IPneumaticConnector, IGUIProvider {

	protected TileEntityPneumoTube.PneumaticNode node;
	public StackCache cache;

	public TileEntityPneumaticMachineBase(int slotCount) {
		super(slotCount);
	}

	@Override
	public void update() {

		if(!world.isRemote) {

			if(this.node == null || this.node.expired) {
				if(this.cache != null) this.cache.dissolveCache();

				this.node = UniNodespace.getNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);

				if(this.node == null || this.node.expired) {
					this.node = new TileEntityPneumoTube.PneumaticNode(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).setConnections(
							new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
							new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
							new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
							new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
							new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
							new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
					);
					UniNodespace.createNode(world, this.node);
				}
			}

			if(this.cache == null || this.cache.hasExpired) {
				this.cache = new StackCache(pos.getX(), pos.getY(), pos.getZ());
			}

			if(this.node != null && this.node.hasValidNet()) {
				this.node.net.addStackCache(cache);
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!world.isRemote && this.node != null) {
			UniNodespace.destroyNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
			this.node = null;
		}

		if(this.cache != null) this.cache.dissolveCache();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(!world.isRemote && this.node != null) {
			UniNodespace.destroyNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
			this.node = null;
		}

		if(this.cache != null) this.cache.dissolveCache();
	}
}
