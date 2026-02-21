package com.hbm.entity.item;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockSupplyCrate;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_parachute_crate", trackingRange = 1000)
public class EntityParachuteCrate extends Entity {

    public List<ItemStack> items = new ArrayList<>();

    public EntityParachuteCrate(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    @Override
    public void onUpdate() {

        this.lastTickPosX = this.prevPosX = posX;
        this.lastTickPosY = this.prevPosY = posY;
        this.lastTickPosZ = this.prevPosZ = posZ;
        this.setPosition(posX + this.motionX, posY + this.motionY, posZ + this.motionZ);

        if(this.motionY > -0.2) this.motionY -= 0.02;
        if(posY > 600) posY = 600;
        BlockPos pos = new BlockPos((int) Math.floor(this.posX), (int) Math.floor(this.posY), (int) Math.floor(this.posZ));
        if(this.world.getBlockState(pos).getBlock() != Blocks.AIR) {

            this.setDead();

            if(!world.isRemote) {

                world.setBlockState(pos.up(1), ModBlocks.crate_supply.getDefaultState());
                BlockSupplyCrate.TileEntitySupplyCrate crate = (BlockSupplyCrate.TileEntitySupplyCrate) world.getTileEntity(pos.up(1));
                if(crate != null) crate.items.addAll(this.items);
            }
        }
    }

    @Override protected void entityInit() { }
    @Override @SideOnly(Side.CLIENT) public boolean isInRangeToRenderDist(double distance) { return true; }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        items.clear();
        NBTTagList list = nbt.getTagList("items", 10);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound nbt1 = list.getCompoundTagAt(i);
            items.add(new ItemStack(nbt1));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (ItemStack item : items) {
            NBTTagCompound nbt1 = new NBTTagCompound();
            item.writeToNBT(nbt1);
            list.appendTag(nbt1);
        }
        nbt.setTag("items", list);
    }
}
