package com.hbm.tileentity.machine.pile;

import com.hbm.api.block.IPileNeutronReceiver;
import com.hbm.blocks.machine.pile.BlockGraphiteNeutronDetector;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
@Deprecated
@AutoRegister
public class TileEntityPileNeutronDetector extends TileEntity implements IPileNeutronReceiver, ITickable {

    public int lastNeutrons;
    public int neutrons;
    public int maxNeutrons = 10;

    @Override
    public void update() {

        if(!world.isRemote) {

            //lastNeutrons is used to reduce the responsiveness of control rods; should cut down on sound/updates whilst keeping them still useful for automatic control.
            //Even with it, the auto rods are *very* subject to triggering on and off rapidly - this is necessary, as rays in smaller piles aren't guarenteed to consistently flood all surrounding areas
            if(this.neutrons >= this.maxNeutrons && (this.getBlockMetadata() & 8) > 0)
                ((BlockGraphiteNeutronDetector)world.getBlockState(pos).getBlock()).triggerRods(world, pos);
            if(this.neutrons < this.maxNeutrons && this.lastNeutrons < this.maxNeutrons && (this.getBlockMetadata() & 8) == 0)
                ((BlockGraphiteNeutronDetector)world.getBlockState(pos).getBlock()).triggerRods(world, pos);

            this.lastNeutrons = this.neutrons;
            this.neutrons = 0;
        }
    }

    @Override
    public void receiveNeutrons(int n) {
        this.neutrons += n;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("maxNeutrons", this.maxNeutrons);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.maxNeutrons = nbt.getInteger("maxNeutrons");
    }
}
