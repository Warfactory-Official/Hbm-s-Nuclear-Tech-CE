package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyDyn;
import com.hbm.tileentity.machine.TileEntityMachineChemicalFactory;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public class MachineChemicalFactory extends BlockDummyable implements ITooltipProvider, ILookOverlay {

    public MachineChemicalFactory(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if(meta >= 12) return new TileEntityMachineChemicalFactory();
        if(meta >= 6) return new TileEntityProxyDyn().inventory().power().fluid();
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(world, pos, player, 0);
    }

    @Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }
    @Override public int getOffset() { return 2; }

    @Override
    public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);

        x -= dir.offsetX * 2;
        z -= dir.offsetZ * 2;

        for(int i = -2; i <= 2; i++) for(int j = -2; j <= 2; j++) {
            if(Math.abs(i) == 2 || Math.abs(j) == 2) this.makeExtra(world, x + i, y, z + j);
        }

        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        for(int i = -2; i <= 2; i++) {
            this.makeExtra(world, x + dir.offsetX * i + rot.offsetX * 2, y + 2, z + dir.offsetZ * i + rot.offsetZ * 2);
            this.makeExtra(world, x + dir.offsetX * i - rot.offsetX * 2, y + 2, z + dir.offsetZ * i - rot.offsetZ * 2);
        }
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        this.addStandardInfo(tooltip);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);
        if(corePos == null) return;

        TileEntity te = world.getTileEntity(corePos);
        if(!(te instanceof TileEntityMachineChemicalFactory chemfac)) return;

        DirPos[] cool = chemfac.getCoolPos();
        DirPos[] io = chemfac.getIOPos();

        for(DirPos dirPos : cool) if(dirPos.compare(pos.getX() + dirPos.getDir().offsetX, pos.getY(), pos.getZ() + dirPos.getDir().offsetZ)) {
            List<String> text = new ArrayList<>();

            text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + chemfac.water.getTankType().getLocalizedName());
            text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + chemfac.lps.getTankType().getLocalizedName());

            ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
            break;
        }


        for(int i = 0; i < io.length; i++) {
            DirPos port = io[i];
            if(port.compare(pos.getX() + port.getDir().offsetX, pos.getY(), pos.getZ() + port.getDir().offsetZ)) {
                List<String> text = new ArrayList();
                text.add(TextFormatting.YELLOW + "-> " + TextFormatting.RESET + "Recipe field [" + (i + 1) + "]");
                ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
                break;
            }
        }
    }
}
