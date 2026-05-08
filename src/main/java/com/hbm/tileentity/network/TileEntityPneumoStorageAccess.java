package com.hbm.tileentity.network;

import com.hbm.api.ntl.IPneumaticConnector;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPneumoStorageAccess;
import com.hbm.inventory.gui.GUIPneumoStorageAccess;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PneumaticNetwork;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityPneumoStorageAccess extends TileEntityLoadedBase implements ITickable, IPneumaticConnector, IGUIProvider {

    protected TileEntityPneumoTube.PneumaticNode node;

    @Override
    public void update() {
        if (!world.isRemote) {
            if (this.node == null || this.node.expired) {
                this.node = UniNodespace.getNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
                if (this.node == null || this.node.expired) {
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
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote && this.node != null) {
            UniNodespace.destroyNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
            this.node = null;
        }
    }

    @Override
    public boolean canConnectPneumatic(ForgeDirection dir) {
        TileEntity tile = world != null ? world.getTileEntity(pos) : null;
        if (tile == null) return false;
        net.minecraft.util.EnumFacing facing = world.getBlockState(pos).getValue(com.hbm.blocks.network.PneumoStorageAccess.FACING);
        return dir == ForgeDirection.getOrientation(facing.getOpposite().getIndex());
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPneumoStorageAccess(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPneumoStorageAccess(player.inventory, this);
    }
}
