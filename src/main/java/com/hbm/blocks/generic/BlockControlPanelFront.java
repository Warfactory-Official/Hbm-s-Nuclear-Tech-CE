//package com.hbm.blocks.generic;
//
//import java.util.Random;
//
//import com.hbm.blocks.BlockControlPanelType;
//import com.hbm.blocks.ModBlocks;
//import com.hbm.interfaces.ICustomSelectionBox;
//import com.hbm.inventory.control_panel.Control;
//import com.hbm.inventory.control_panel.ControlEvent;
//import com.hbm.inventory.control_panel.ControlPanel;
//import com.hbm.inventory.control_panel.types.DataValueFloat;
//import com.hbm.items.ModItems;
//import com.hbm.main.ClientProxy;
//import com.hbm.main.MainRegistry;
//import com.hbm.packet.toserver.NBTControlPacket;
//import com.hbm.packet.PacketDispatcher;
//import com.hbm.tileentity.machine.TileEntityControlPanel;
//
//import net.minecraft.block.BlockContainer;
//import net.minecraft.block.BlockHorizontal;
//import net.minecraft.block.material.Material;
//import net.minecraft.block.properties.IProperty;
//import net.minecraft.block.properties.PropertyEnum;
//import net.minecraft.block.state.BlockFaceShape;
//import net.minecraft.block.state.BlockStateContainer;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.client.renderer.RenderGlobal;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.init.Items;
//import net.minecraft.item.Item;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.*;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//public class BlockControlPanelFront extends BlockContainer implements ICustomSelectionBox {
//
//    public static final PropertyEnum<EnumFacing> FACING = BlockHorizontal.FACING;
//
//    public BlockControlPanelFront(String s) {
//        super(Material.IRON);
//        this.setHardness(3F);
//        this.setResistance(30F);
//        this.setTranslationKey(s);
//        this.setRegistryName(s);
//        ModBlocks.ALL_BLOCKS.add(this);
//    }
//
//    @Nullable
//    @Override
//    public TileEntity createNewTileEntity(World worldIn, int meta) {
//        TileEntityControlPanel te = new TileEntityControlPanel();
//        te.panel = new ControlPanel(te, (float) (1-Math.tan(Math.toRadians(11.31))/2), (float) Math.toRadians(11.31), 0, 0, .08F, 0);
//        te.panelType = BlockControlPanelType.FRONT_PANEL;
//        return te;
//    }
//
//    @Override
//    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
//        if(!worldIn.isRemote){
//            if(playerIn.getHeldItem(hand).getItem() == ModItems.screwdriver || playerIn.getHeldItem(hand).getItem() == ModItems.screwdriver_desh)
//                playerIn.openGui(MainRegistry.instance, ModBlocks.guiID_control_panel, worldIn, pos.getX(), pos.getY(), pos.getZ());
//        } else {
//            TileEntityControlPanel control = (TileEntityControlPanel)worldIn.getTileEntity(pos);
//            Control ctrl = control.panel.getSelectedControl(playerIn.getPositionEyes(1), playerIn.getLook(1));
//            if(ctrl != null) {
//                NBTTagCompound dat = ControlEvent.newEvent("ctrl_press").setVar("isSneaking", new DataValueFloat(playerIn.isSneaking())).writeToNBT(new NBTTagCompound());
//                dat.setInteger("click_control", ctrl.panel.controls.indexOf(ctrl));
//                PacketDispatcher.wrapper.sendToServer(new NBTControlPacket(dat, pos));
//                return true;
//            }
//        }
//        return true;
//    }
//
//    @Nonnull
//    @Override
//    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
//        TileEntity te = source.getTileEntity(pos);
//        if (te instanceof TileEntityControlPanel) {
//            AxisAlignedBB ret = ((TileEntityControlPanel) te).getBoundingBox(state.getValue(UP), state.getValue(DOWN), state.getValue(FACING));
//            if (ret != null) {
//                return ret;
//            }
//        }
//        return super.getBoundingBox(state, source, pos);
//    }
//
//    @Override
//    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
//        return BlockFaceShape.UNDEFINED;
//    }
//
//    @Override
//    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
//        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
//    }
//
//    @Override
//    public EnumBlockRenderType getRenderType(IBlockState state) {
//        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
//    }
//
//    @Override
//    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
//        return Items.AIR;
//    }
//
//    @Override
//    public boolean isFullCube(IBlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isBlockNormalCube(IBlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isNormalCube(IBlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
//        return false;
//    }
//    @Override
//    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
//        return false;
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean renderBox(World world, EntityPlayer player, IBlockState state, BlockPos pos, double x, double y, double z, float partialTicks){
//        TileEntityControlPanel control = (TileEntityControlPanel)world.getTileEntity(pos);
//        Control ctrl = control.panel.getSelectedControl(player.getPositionEyes(partialTicks), player.getLook(partialTicks));
//
//        if (ctrl != null) {
//            GlStateManager.pushMatrix();
//            GlStateManager.translate(x, y, z);
//            control.panel.transform.store(ClientProxy.AUX_GL_BUFFER);
//            ClientProxy.AUX_GL_BUFFER.rewind();
//            GL11.glMultMatrix(ClientProxy.AUX_GL_BUFFER);
//            if (ctrl.getBoundingBox() != null)
//                // offset to bury bottom lines
//                RenderGlobal.drawSelectionBoundingBox(ctrl.getBoundingBox().offset(0, -.01F, 0), 0, 0, 0, 0.4F);
//            GlStateManager.popMatrix();
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    protected BlockStateContainer createBlockState() {
//        return new BlockStateContainer(this, new IProperty[]{FACING});
//    }
//
//    @Override
//    public int getMetaFromState(IBlockState state) {
//        return state.getValue(FACING).getIndex();
//    }
//
//    @Override
//    public IBlockState getStateFromMeta(int meta) {
//        EnumFacing enumfacing = EnumFacing.byIndex(meta);
//        return this.getDefaultState().withProperty(FACING, enumfacing);
//    }
//
//    @Override
//    public IBlockState withRotation(IBlockState state, Rotation rot) {
//        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
//    }
//
//    @Override
//    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
//    {
//        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
//    }
//}
