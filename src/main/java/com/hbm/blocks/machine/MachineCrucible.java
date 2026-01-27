package com.hbm.blocks.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.material.Mats;
import com.hbm.items.machine.ItemScraps;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCrucible;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class MachineCrucible extends BlockDummyable implements ICrucibleAcceptor, ITooltipProvider {

    public MachineCrucible(String s) {
        super(Material.ROCK, s);

        this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D));
        this.bounding.add(new AxisAlignedBB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D));
        this.bounding.add(new AxisAlignedBB(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D));
        this.bounding.add(new AxisAlignedBB(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D));
        this.bounding.add(new AxisAlignedBB(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D));
        FULL_BLOCK_AABB.setMaxY(0.999D); //item bounce prevention
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {

        if(meta >= 12) return new TileEntityCrucible();
        return new TileEntityProxyCombo(true, false, false);
    }

    @Override
    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {

        if(world.isRemote) {
            return true;
        }

        if(!player.isSneaking()) {
            BlockPos corePos = findCore(world, pos);

            if(corePos == null) return false;

            ItemStack heldItem = player.getHeldItem(hand);

            if(!heldItem.isEmpty() && heldItem.getItem() instanceof ItemTool && heldItem.getItem().getToolClasses(heldItem).contains("shovel")) {
                TileEntityCrucible crucible = (TileEntityCrucible) world.getTileEntity(corePos);
                if (crucible == null) return false;

                List<Mats.MaterialStack> stacks = new ArrayList<>();
                stacks.addAll(crucible.recipeStack);
                stacks.addAll(crucible.wasteStack);

                for(Mats.MaterialStack stack : stacks) {
                    ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(stack.material, stack.amount));
                    if(!player.inventory.addItemStackToInventory(scrap)) {
                        EntityItem item = new EntityItem(world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, scrap);
                        world.spawnEntity(item);
                    }
                }

                player.inventoryContainer.detectAndSendChanges();
                crucible.recipeStack.clear();
                crucible.wasteStack.clear();
                crucible.markDirty();
            }
        }

        return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
    }

    @Override
    public int[] getDimensions() {
        return new int[] {1, 0, 1, 1, 1, 1};
    }

    @Override
    public int getOffset() {
        return 1;
    }

    @Override
    public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {

        TileEntity te = world.getTileEntity(pos);

        if(te instanceof TileEntityCrucible crucible) {
            List<Mats.MaterialStack> stacks = new ArrayList<>();
            stacks.addAll(crucible.recipeStack);
            stacks.addAll(crucible.wasteStack);

            for(Mats.MaterialStack stack : stacks) {
                ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(stack.material, stack.amount));
                EntityItem item = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, scrap);
                world.spawnEntity(item);
            }

            crucible.recipeStack.clear();
            crucible.wasteStack.clear();
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldDrawHighlight(World world, BlockPos pos) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos) {

        int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
        if(posC == null) return;
        TileEntity tile = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
        if(!(tile instanceof TileEntityCrucible crucible)) return;

        int x = crucible.getPos().getX();
        int y = crucible.getPos().getY();
        int z = crucible.getPos().getZ();

        EntityPlayer player = event.getPlayer();
        float partialTicks = event.getPartialTicks();
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
        float exp = 0.002F;

        ICustomBlockHighlight.setup();
		/*event.context.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(x - 1, y, z - 1, x + 2, y + 0.5, z + 2).expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);
		event.context.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(x - 0.75, y + 0.5, z - 0.75, x + 1.75, y + 1.5, z + 1.75).expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);
		event.context.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(x - 0.5, y + 0.75, z - 0.5, x + 1.5, y + 1.5, z + 1.5).expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);*/
        for (AxisAlignedBB aabb : this.bounding) {
            AxisAlignedBB transformedAABB = aabb.expand(exp, exp, exp).offset(x - dX + 0.5, y - dY, z - dZ + 0.5);
            RenderGlobal.drawSelectionBoundingBox(transformedAABB, 0, 0, 0, -1);
        }
        ICustomBlockHighlight.cleanup();
    }

    @Override
    public boolean canAcceptPartialPour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {

        BlockPos corePos = this.findCore(world, pos);
        if(corePos == null) return false;
        TileEntity tile = world.getTileEntity(corePos);
        if(!(tile instanceof TileEntityCrucible crucible)) return false;

        return crucible.canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
    }

    @Override
    public Mats.MaterialStack pour(World world, BlockPos pos, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {

        BlockPos corePos = this.findCore(world, pos);
        if(corePos == null) return stack;
        TileEntity tile = world.getTileEntity(corePos);
        if(!(tile instanceof TileEntityCrucible crucible)) return stack;

        return crucible.pour(world, pos, dX, dY, dZ, side, stack);
    }

    @Override public boolean canAcceptPartialFlow(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) { return false; }
    @Override public Mats.MaterialStack flow(World world, BlockPos pos, ForgeDirection side, Mats.MaterialStack stack) { return null; }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
    }
}
