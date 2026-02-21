package com.hbm.blocks.network;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityDroneDock;
import com.hbm.tileentity.network.TileEntityDroneProvider;
import com.hbm.tileentity.network.TileEntityDroneRequester;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class DroneDock extends BlockContainer implements ITooltipProvider {
    public DroneDock(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(MainRegistry.controlTab);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(this == ModBlocks.drone_dock) return new TileEntityDroneDock();
        if(this == ModBlocks.drone_crate_provider) return new TileEntityDroneProvider();
        if(this == ModBlocks.drone_crate_requester) return new TileEntityDroneRequester();

        return null;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote) {
            return true;
        } else if(!player.isSneaking()) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        this.addStandardInfo(tooltip);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if(this == ModBlocks.drone_dock) this.dropContents(world, pos, state, 0, 9);
        if(this == ModBlocks.drone_crate_provider) this.dropContents(world, pos, state, 0, 9);
        if(this == ModBlocks.drone_crate_requester) this.dropContents(world, pos, state, 9, 18);
        super.breakBlock(world, pos, state);
    }

    private final Random rand = new Random();
    public void dropContents(World world, BlockPos pos, IBlockState state, int start, int end) {
        ISidedInventory sidedInventory = (ISidedInventory) world.getTileEntity(pos);

        if(sidedInventory != null) {

            for(int i = start; i < end; ++i) {
                ItemStack stack = sidedInventory.getStackInSlot(i);

                if(!stack.isEmpty()) {
                    float f = this.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

                    while(stack.getCount() > 0) {
                        int j = this.rand.nextInt(21) + 10;

                        if(j > stack.getCount()) {
                            j = stack.getCount();
                        }

                        stack.shrink(j);
                        EntityItem entity = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, new ItemStack(stack.getItem(), j, stack.getItemDamage()));

                        if(stack.hasTagCompound()) {
                            entity.getItem().setTagCompound(stack.getTagCompound().copy());
                        }

                        float f3 = 0.05F;
                        entity.motionX = (float) this.rand.nextGaussian() * f3;
                        entity.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
                        entity.motionZ = (float) this.rand.nextGaussian() * f3;
                        world.spawnEntity(entity);
                    }
                }
            }

            world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
        }
    }
}
