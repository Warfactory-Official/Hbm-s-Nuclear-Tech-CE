package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityDroneCrate;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DroneCrate extends BlockContainer implements ILookOverlay, ITooltipProvider {
    private static Random rand = new Random();

    public DroneCrate(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(MainRegistry.controlTab);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityDroneCrate();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.drone_linker) return false;

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
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityDroneCrate tileEntity = (TileEntityDroneCrate) world.getTileEntity(pos);

        if(tileEntity != null) {
            for(int i = 0; i < tileEntity.inventory.getSlots(); ++i) {
                ItemStack itemstack = tileEntity.inventory.getStackInSlot(i);

                if(!itemstack.isEmpty()) {
                    float f = rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = rand.nextFloat() * 0.8F + 0.1F;

                    while(!itemstack.isEmpty()) {
                        int j = rand.nextInt(21) + 10;

                        if(j > itemstack.getCount()) {
                            j = itemstack.getCount();
                        }

                        itemstack.shrink(j);
                        EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));

                        if(itemstack.hasTagCompound()) {
                            entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
                        }

                        float f3 = 0.05F;
                        entityitem.motionX = (float) rand.nextGaussian() * f3;
                        entityitem.motionY = (float) rand.nextGaussian() * f3 + 0.2F;
                        entityitem.motionZ = (float) rand.nextGaussian() * f3;
                        world.spawnEntity(entityitem);
                    }
                }
            }

            world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        addStandardInfo(tooltip);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        TileEntityDroneCrate tile = (TileEntityDroneCrate) world.getTileEntity(pos);
        List<String> text = new ArrayList<>();

        if (tile.nextY != -1) {
            text.add("Next waypoint: " + tile.nextX + " / " + tile.nextY + " / " + tile.nextZ);
            ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
        }
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        this.printHook(event, world, new BlockPos(x, y, z));
    }
}
