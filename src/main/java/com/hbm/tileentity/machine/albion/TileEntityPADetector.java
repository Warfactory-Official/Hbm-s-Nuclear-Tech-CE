package com.hbm.tileentity.machine.albion;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPADetector;
import com.hbm.inventory.gui.GUIPADetector;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes.ParticleAcceleratorRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.AdvancementManager;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.albion.TileEntityPASource.PAState;
import com.hbm.tileentity.machine.albion.TileEntityPASource.Particle;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AutoRegister
public class TileEntityPADetector extends TileEntityCooledBase implements IGUIProvider, IParticleUser {

    public static final long usage = 100_000;
    AxisAlignedBB bb = null;

    public TileEntityPADetector() {
        super(5);
    }

    @Override
    public String getDefaultName() {
        return "container.paDetector";
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());
        }

        super.update();
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{
                new DirPos(getPos().add(-rot.offsetX * 5, 0, -rot.offsetZ * 5), rot.getOpposite()),
                new DirPos(getPos().add(-rot.offsetX * 5, 1, -rot.offsetZ * 5), rot.getOpposite()),
                new DirPos(getPos().add(-rot.offsetX * 5, -1, -rot.offsetZ * 5), rot.getOpposite()),
                new DirPos(getPos().add(-rot.offsetX * 5 + dir.offsetX, 0, -rot.offsetZ * 5 + dir.offsetZ), rot.getOpposite()),
                new DirPos(getPos().add(-rot.offsetX * 5 - dir.offsetX, 0, -rot.offsetZ * 5 - dir.offsetZ), rot.getOpposite()),
        };
    }

    @Override
    public long getMaxPower() {
        return 1_000_000;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 1 || slot == 2;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int amount) {
        return slot == 3 || slot == 4;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[] { 1, 2, 3, 4 };
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(getPos().add(-4, -2, -4), getPos().add(5, 3, 5));
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPADetector(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPADetector(player.inventory, this);
    }

    @Override
    public boolean canParticleEnter(Particle particle, ForgeDirection dir, BlockPos pos) {
        ForgeDirection detectorDir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getRotation(ForgeDirection.DOWN);
        BlockPos input = getPos().offset(detectorDir.toEnumFacing(), -4);
        return input.equals(pos) && detectorDir == dir;
    }

    @Override
    public void onEnter(Particle particle, ForgeDirection dir) {
        particle.invalid = true;
        //particle will crash if not perfectly focused
        if (particle.defocus > 0) {
            particle.crash(PAState.CRASH_DEFOCUS);
            return;
        }
        if (this.power < usage) {
            particle.crash(PAState.CRASH_NOPOWER);
            return;
        }
        if (!isCool()) {
            particle.crash(PAState.CRASH_NOCOOL);
            return;
        }
        this.power -= usage;

        for (ParticleAcceleratorRecipe recipe : ParticleAcceleratorRecipes.recipes) {
            if (!recipe.matchesRecipe(particle.input1, particle.input2)) continue; // another W for continue

            if (particle.momentum < recipe.momentum) {
                particle.crash(PAState.CRASH_UNDERSPEED);
                return;
            }

            if (canAccept(recipe)) {
                if (recipe.output1.getItem().hasContainerItem(recipe.output1)) inventory.extractItem(1, 1, false);
                if (recipe.output2 != null && recipe.output2.getItem().hasContainerItem(recipe.output2)) inventory.extractItem(2, 1, false);

                if (inventory.getStackInSlot(3).isEmpty()) {
                    inventory.setStackInSlot(3, recipe.output1.copy());
                } else {
                    inventory.insertItem(3, recipe.output1, false);
                }

                if (recipe.output2 != null) {
                    if (inventory.getStackInSlot(4).isEmpty()) {
                        inventory.setStackInSlot(4, recipe.output2.copy());
                    } else {
                        inventory.insertItem(4, recipe.output2, false);
                    }
                }
            }
            if(recipe.output1.getItem() == ModItems.particle_digamma || recipe.output2 != null && recipe.output2.getItem() == ModItems.particle_digamma) {
                List<EntityPlayerMP> players = world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).grow(100, 50, 100));
                for(EntityPlayerMP player : players) AdvancementManager.grantAchievement(player, AdvancementManager.achOmega12);
            }
            particle.crash(PAState.SUCCESS);
            return;
        }

        particle.crash(PAState.CRASH_NORECIPE);
    }

    public boolean canAccept(ParticleAcceleratorRecipe recipe) {
        return checkSlot(recipe.output1, 1, 3) && checkSlot(recipe.output2, 2, 4);
    }

    public boolean checkSlot(ItemStack output, int containerSlot, int outputSlot) {
        if (output != null) {
            if (!inventory.getStackInSlot(outputSlot).isEmpty()) {
                ItemStack stackInSlot = inventory.getStackInSlot(outputSlot);
                //cancel if: output item does not match, meta does not match, resulting stacksize exceeds stack limit
                if (stackInSlot.getItem() != output.getItem() || stackInSlot.getItemDamage() != output.getItemDamage() || stackInSlot.getCount() + output.getCount() > output.getMaxStackSize())
                    return false;
            }
            if (output.getItem().hasContainerItem(output)) {
                ItemStack container = output.getItem().getContainerItem(output);
                ItemStack stackInSlot = inventory.getStackInSlot(containerSlot);
                //cancel if: container slot is empty, container item does not match, meta does not match
                return !stackInSlot.isEmpty() && stackInSlot.getItem() == container.getItem() && stackInSlot.getItemDamage() == container.getItemDamage();
            }
        }
        return true;
    }

    @Override
    public BlockPos getExitPos(Particle particle) {
        return null;
    }
}
