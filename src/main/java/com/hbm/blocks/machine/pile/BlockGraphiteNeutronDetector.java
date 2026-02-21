package com.hbm.blocks.machine.pile;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.pile.TileEntityPileNeutronDetector;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGraphiteNeutronDetector extends BlockGraphiteDrilledTE {
    public BlockGraphiteNeutronDetector(String s) {
        super(s);
        this.blockFrames = new BlockBakeFrame[16];
        for (int meta = 0; meta < 16; meta++) {
            boolean isAluminum = (meta & 4) != 0;
            boolean isOut = (meta & 8) != 0;
            String front;
            if (isAluminum) {
                front = isOut ? "block_graphite_detector_out_aluminum" : "block_graphite_detector_aluminum";
            } else {
                front = isOut ? "block_graphite_detector_out" : "block_graphite_detector";
            }
            this.blockFrames[meta] = new BlockBakeFrame(front, "block_graphite", front);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPileNeutronDetector();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + "block_graphite_detector_out_aluminum"));
        map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + "block_graphite_detector_out"));
    }

    public void triggerRods(World world, BlockPos pos) {
        int oldMeta = getMetaFromState(world.getBlockState(pos));
        int newMeta = oldMeta ^ 8; // toggle bit #4
        int pureMeta = oldMeta & 3;

        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockMeta.META, newMeta), 3);

        world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, HBMSoundHandler.techBleep, SoundCategory.BLOCKS, 0.02F, 1.0F);

        EnumFacing dir = EnumFacing.byIndex(pureMeta * 2);

        for (int i = -1; i <= 1; i++) {
            BlockPos iPos = pos.offset(dir, i);
            while (world.getBlockState(iPos).getBlock() == ModBlocks.block_graphite_rod &&
                    getMetaFromState(world.getBlockState(iPos)) == oldMeta) {
                world.setBlockState(iPos, world.getBlockState(iPos).withProperty(BlockMeta.META, newMeta), 3);
                iPos = iPos.offset(dir, i);
            }
        }
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand, ToolType tool) {
        if (!world.isRemote) {
            BlockPos pos = new BlockPos(x, y, z);
            int meta = getMetaFromState(world.getBlockState(pos));
            int cfg = meta & 3;
            int sideIdx = side.getIndex();

            if (tool == ToolType.SCREWDRIVER) {
                if (!player.isSneaking()) {
                    if (sideIdx == cfg * 2 || sideIdx == cfg * 2 + 1) {
                        world.setBlockState(pos, ModBlocks.block_graphite_drilled.getDefaultState().withProperty(BlockMeta.META, meta & 7), 3);
                        ejectItem(world, x, y, z, side, getInsertedItem());
                    }
                } else {
                    TileEntityPileNeutronDetector pile = (TileEntityPileNeutronDetector) world.getTileEntity(pos);
                    if (pile != null) {
                        player.sendMessage(new TextComponentString("CP1 FUEL ASSEMBLY " + x + " " + y + " " + z).setStyle(new Style().setColor(TextFormatting.GOLD)));
                        player.sendMessage(new TextComponentString("FLUX: " + pile.lastNeutrons + "/" + pile.maxNeutrons).setStyle(new Style().setColor(TextFormatting.YELLOW)));
                    }
                }
            }

            if (tool == ToolType.DEFUSER) {
                TileEntityPileNeutronDetector pile = (TileEntityPileNeutronDetector) world.getTileEntity(pos);
                if (pile != null) {
                    if (player.isSneaking()) {
                        if (pile.maxNeutrons > 1)
                            pile.maxNeutrons--;
                    } else {
                        pile.maxNeutrons++;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected ItemStack getInsertedItem() {
        return new ItemStack(ModItems.pile_rod_detector);
    }
}
