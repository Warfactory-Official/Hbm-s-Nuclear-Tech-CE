package com.hbm.tileentity.bomb;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityLaunchPad extends TileEntityLaunchPadBase {
    public TileEntityLaunchPad() {
        super(7);
    }

    @Override
    public boolean isReadyForLaunch() { return delay <= 0; }
    @Override
    public double getLaunchOffset() { return 1D; }

    public int delay = 0;

    @Override
    public void update() {
        if(!world.isRemote) {

            if(this.delay > 0) delay--;

            if(!this.isMissileValid() || !this.hasFuel()) {
                this.delay = 100;
            }

            if(!this.hasFuel() || !this.isMissileValid()) {
                this.state = STATE_MISSING;
            } else {
                if(this.delay > 0) {
                    this.state = STATE_LOADING;
                } else {
                    this.state = STATE_READY;
                }
            }

        } else {

            List<EntityMissileBaseNT> entities = world.getEntitiesWithinAABB(EntityMissileBaseNT.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));

            if(!entities.isEmpty()) {
                for(int i = 0; i < 15; i++) {

                    ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
                    if(world.rand.nextBoolean()) dir = dir.getOpposite();
                    if(world.rand.nextBoolean()) dir = dir.getRotation(ForgeDirection.UP);
                    float moX = (float) (world.rand.nextGaussian() * 0.15F + 0.75) * dir.offsetX;
                    float moZ = (float) (world.rand.nextGaussian() * 0.15F + 0.75) * dir.offsetZ;

                    NBTTagCompound data = new NBTTagCompound();
                    data.setDouble("posX", pos.getX() + 0.5);
                    data.setDouble("posY", pos.getY() + 0.25);
                    data.setDouble("posZ", pos.getZ() + 0.5);
                    data.setString("type", "launchSmoke");
                    data.setDouble("moX", moX);
                    data.setDouble("moY", 0);
                    data.setDouble("moZ", moZ);
                    MainRegistry.proxy.effectNT(data);
                }
            }
        }

        super.update();
    }

    @Override
    public void finalizeLaunch(Entity missile) {
        super.finalizeLaunch(missile);
        this.delay = 100;
    }

    @Override
    public DirPos[] getConPos() {
        return new DirPos[] {
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.delay = nbt.getInteger("delay");
        if (inventory.getSlots() != 7) {
            resizeInventory(7);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("delay", delay);
        return super.writeToNBT(nbt);
    }
    
    // Th3_Sl1ze: do we even need that? as far as I know, that doesn't exist on 1.7
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "launchpad";
    }

    @Callback(doc = "setTarget(x:int, z:int):boolean; Sets coordinates in the installed designator item.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setTarget(Context context, Arguments args) {
        ItemStack designatorStack = inventory.getStackInSlot(1);
        if (!designatorStack.isEmpty() && designatorStack.getItem() instanceof IDesignatorItem) {
            NBTTagCompound nbt = designatorStack.hasTagCompound() ? designatorStack.getTagCompound() :
                    new NBTTagCompound();
            nbt.setInteger("pos.getX()", args.checkInteger(0));
            nbt.setInteger("pos.getZ()", args.checkInteger(1));
            designatorStack.setTagCompound(nbt);
            return new Object[]{true};
        }
        return new Object[]{false, "No valid designator installed"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{"getEnergyInfo", "canLaunch", "launch", "setTarget"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getEnergyInfo" -> getEnergyInfo(context, args);
            case "canLaunch" -> canLaunch(context, args);
            case "launch" -> launch(context, args);
            case "setTarget" -> setTarget(context, args);
            default -> throw new NoSuchMethodException();
        };
    }
}