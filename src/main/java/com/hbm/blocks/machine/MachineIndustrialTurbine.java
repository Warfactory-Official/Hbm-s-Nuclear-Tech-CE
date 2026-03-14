package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineIndustrialTurbine;
import com.hbm.tileentity.machine.TileEntityTurbineBase;
import com.hbm.util.BobMathUtil;

import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineIndustrialTurbine extends BlockDummyable implements ITooltipProvider, ILookOverlay {

    public MachineIndustrialTurbine(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if(meta >= 12) return new TileEntityMachineIndustrialTurbine();
        if(meta >= 6) return new TileEntityProxyCombo().fluid().power();
        return null;
    }

    @Override
    public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!player.isSneaking()) {
            BlockPos posC = this.findCore(world, pos);
            if(posC == null) return true;

            TileEntityTurbineBase entity = (TileEntityTurbineBase) world.getTileEntity(posC);
            if(entity != null) {

                ForgeDirection dir = ForgeDirection.getOrientation(entity.getBlockMetadata() - offset);

                if(pos.getX() == entity.getPos().getX() + dir.offsetX * 3 && pos.getZ() == entity.getPos().getZ() + dir.offsetZ * 3 && pos.getY() == entity.getPos().getY() + 1) {
                    if(!world.isRemote) {
                        if(!entity.operational) {
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.chungus_lever, SoundCategory.BLOCKS,
                            1.5F, 1.0F);
                            entity.onLeverPull();
                        } else {
                            player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot change compressor setting while operational!"));
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

    @Override public int[] getDimensions() { return new int[] { 2, 0, 3, 3, 1, 1 }; }
    @Override public int getOffset() { return 3; }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x += dir.offsetX * o;
        z += dir.offsetZ * o;

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        this.makeExtra(world, x + dir.offsetX * 3 + rot.offsetX, y, z + dir.offsetZ * 3 + rot.offsetZ);
        this.makeExtra(world, x + dir.offsetX * 3 - rot.offsetX, y, z + dir.offsetZ * 3 - rot.offsetZ);
        this.makeExtra(world, x - dir.offsetX + rot.offsetX, y, z - dir.offsetZ + rot.offsetZ);
        this.makeExtra(world, x - dir.offsetX - rot.offsetX, y, z - dir.offsetZ - rot.offsetZ);
        this.makeExtra(world, x + dir.offsetX * 3, y + 2, z + dir.offsetZ * 3);
        this.makeExtra(world, x - dir.offsetX, y + 2, z - dir.offsetZ);
        this.makeExtra(world, x - dir.offsetX * 3, y + 1, z - dir.offsetZ * 3);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        this.addStandardInfo(tooltip);
    }

    private static final String[] blocks = new String[] {"▖ ", "▘ ", " ▘", " ▖"}; // right hand side quarter blocks break the renderer so we cheat a little

    @Override
    public void printHook(Pre event, World world, BlockPos pos) {
        BlockPos posC = this.findCore(world, pos);
        if(posC == null) return;

        TileEntity te = world.getTileEntity(posC);
        if(!(te instanceof TileEntityMachineIndustrialTurbine chungus)) return;

        List<String> text = new ArrayList<>();

        FluidTankNTM tankInput = chungus.tanks[0];
        FluidTankNTM tankOutput = chungus.tanks[1];

        FluidType inputType = tankInput.getTankType();
        FluidType outputType = Fluids.NONE;

        if(inputType.hasTrait(FT_Coolable.class)) {
            outputType = inputType.getTrait(FT_Coolable.class).coolsTo;
        }

        int color = ((int) (0xFF - 0xFF * chungus.spin)) << 16 | ((int)(0xFF * chungus.spin) << 8);
        int time = (int) ((world.getTotalWorldTime() / 4) % 4);

        text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + inputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankInput.getFill()) + "/" + String.format(Locale.US, "%,d", tankInput.getMaxFill()) + "mB");
        text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + outputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankOutput.getFill()) + "/" + String.format(Locale.US, "%,d", tankOutput.getMaxFill()) + "mB");
        text.add("&[" + color + "&]" + TextFormatting.RED + "<- " + TextFormatting.WHITE + BobMathUtil.getShortNumber(chungus.powerBuffer) + "HE (" +
                TextFormatting.RESET + blocks[chungus.powerBuffer <= 0 ? 0 : time] + (int) Math.round(chungus.spin * 100) + "%" + TextFormatting.WHITE + ")");


        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}
