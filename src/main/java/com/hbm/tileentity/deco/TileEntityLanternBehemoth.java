package com.hbm.tileentity.deco;

import com.hbm.capability.HbmCapability;
import com.hbm.entity.missile.EntityBobmazon;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemKitCustom;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityLanternBehemoth extends TileEntityLoadedBase implements IRepairable, ITickable {
    public boolean isBroken = false;
    public int comTimer = -1;

    @Override
    public void update() {

        if(!world.isRemote) {

            if(comTimer == 360) world.playSound(null, getPos(), HBMSoundHandler.hornNearSingle, SoundCategory.BLOCKS, 10F, 1F);
            if(comTimer == 280) world.playSound(null, getPos(), HBMSoundHandler.hornFarSingle, SoundCategory.BLOCKS, 100F, 1F);
            if(comTimer == 220) world.playSound(null, getPos(), HBMSoundHandler.hornNearDual, SoundCategory.BLOCKS, 10F, 1F);
            if(comTimer == 100) world.playSound(null, getPos(), HBMSoundHandler.hornFarDual, SoundCategory.BLOCKS, 100F, 1F);

            if(comTimer == 0) {
                List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10, pos.getX() + 11, pos.getY() + 11, pos.getZ() + 11));
                EntityPlayer first = players.isEmpty() ? null : players.get(0);
                boolean bonus = first != null && (HbmCapability.getData(first).getReputation() >= 10);
                EntityBobmazon shuttle = new EntityBobmazon(world);
                shuttle.posX = pos.getX() + 0.5 + world.rand.nextGaussian() * 10;
                shuttle.posY = 300;
                shuttle.posZ = pos.getZ() + 0.5 + world.rand.nextGaussian() * 10;
                shuttle.payload = ItemKitCustom.create("Supplies", null, 0xffffff, 0x008000,
                        DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.BASIC, 4 + world.rand.nextInt(4)),
                        DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.ADVANCED, 4 + world.rand.nextInt(2)),
                        bonus ? new ItemStack(ModItems.gem_alexandrite) : new ItemStack(Items.DIAMOND, 6 + world.rand.nextInt(6)),
                        new ItemStack(Blocks.RED_FLOWER));

                world.spawnEntity(shuttle);
            }

            if(comTimer >= 0) {
                comTimer--;
            }

            networkPackNT(250);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() - 50, pos.getY() - 50, pos.getZ() - 50, pos.getX() + 51, pos.getY() + 51, pos.getZ() + 51));
        for(EntityPlayer player : players) {
            HbmCapability.IHBMData props = HbmCapability.getData(player);
            if(props.getReputation() > -25) props.setReputation(props.getReputation() - 1);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeBoolean(this.isBroken);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.isBroken = buf.readBoolean();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        isBroken = nbt.getBoolean("isBroken");
        comTimer = nbt.getInteger("comTimer");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isBroken", isBroken);
        nbt.setInteger("comTimer", comTimer);
        return nbt;
    }

    @Override
    public boolean isDamaged() {
        return isBroken;
    }

    List<AStack> repair = new ArrayList<>();
    @Override
    public List<AStack> getRepairMaterials() {

        if(!repair.isEmpty())
            return repair;

        repair.add(new RecipesCommon.OreDictStack(OreDictManager.STEEL.plate(), 2));
        repair.add(new RecipesCommon.ComparableStack(ModItems.circuit, 1, ItemEnums.EnumCircuitType.BASIC));
        return repair;
    }

    @Override
    public void repair() {
        this.isBroken = false;
        this.comTimer = 400;
        this.markDirty();
    }

    @Override public void tryExtinguish(World world, int x, int y, int z, EnumExtinguishType type) { }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    pos.getX() + 1,
                    pos.getY() + 6,
                    pos.getZ() + 1
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
