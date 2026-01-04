package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineHeatBoiler extends BlockDummyable implements ILookOverlay, ITooltipProvider {

    public MachineHeatBoiler(Material materialIn, String s) {
        super(materialIn, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12) return new TileEntityHeatBoiler();
        if(meta >= 6) return new TileEntityProxyCombo(false, false, true);

        return null;
    }

    @Override
    public int[] getDimensions() {
        return new int[] {3, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!world.isRemote && !player.isSneaking()) {
            if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier identifier) {
                int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
                if(pos == null) return false;

                TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
                if(!(te instanceof TileEntityHeatBoiler boiler)) return false;

                FluidType type = identifier.getType(world, pos[0], pos[1], pos[2], player.getHeldItem(hand));
                boiler.tanks[0].setTankType(type);
                boiler.markDirty();
                player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));

                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityHeatBoiler boiler && boiler.hasExploded) {
            spawnScrap(world, pos);
        }
        IPersistentNBT.breakBlock(world, pos, state);
        super.breakBlock(world, pos, state);
    }

    private static void spawnScrap(World world, BlockPos pos) {
        if (world.isRemote) return;
        List<ItemStack> scrap = new ArrayList<>();
        scrap.add(new ItemStack(ModItems.ingot_steel, 4));
        scrap.add(new ItemStack(ModItems.plate_copper, 8));

        for (ItemStack stack : scrap) {
            EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            world.spawnEntity(entity);
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        IPersistentNBT.onBlockHarvested(worldIn, pos, player);
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x = x + dir.offsetX * o;
        z = z + dir.offsetZ * o;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        this.makeExtra(world, x + rot.offsetX, y, z + rot.offsetZ); //these add the side ports
        this.makeExtra(world, x - rot.offsetX, y, z - rot.offsetZ);
        this.makeExtra(world, x, y + 3, z); 
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        this.addStandardInfo(list);
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public void printHook(Pre event, World world, int x, int y, int z) {
        int[] pos = this.findCore(world, x, y, z);

        if(pos == null)
            return;

        TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
        if(!(te instanceof TileEntityHeatBoiler boiler)) return;

        List<String> text = new ArrayList<>();

        for(int i = 0; i < boiler.tanks.length; i++)
            text.add((i < 1 ? "§a-> " : "§c<- ") + "§r" + boiler.tanks[i].getTankType().getLocalizedName() + ": " + boiler.tanks[i].getFill() + "/" + boiler.tanks[i].getMaxFill() + "mB");

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        BlockPos core = this.findCore(world, pos);
        if (core != null) {
            IPersistentNBT.onBlockPlacedBy(world, core, stack);
            TileEntity te = world.getTileEntity(core);
            if (te instanceof TileEntityHeatBoiler boiler && stack.getMetadata() == 1) {
                boiler.hasExploded = true;
                boiler.markDirty();
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    }
}
