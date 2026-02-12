package com.hbm.items.tool;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockConveyor;
import com.hbm.blocks.network.BlockConveyorBase;
import com.hbm.blocks.network.BlockConveyorBendable;
import com.hbm.blocks.network.BlockCraneBase;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.render.util.RenderOverhead;
import com.hbm.util.I18nUtil;
import com.hbm.wiaj.WorldInAJar;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemConveyorWand extends Item implements ILookOverlay {

    private static RayTraceResult lastMop;
    private static EnumFacing lastSide;
    private static float lastYaw;

    public ItemConveyorWand(String name) {
        this.setRegistryName(name);
        this.setTranslationKey(name);
        this.setHasSubtypes(true);
        ModItems.ALL_ITEMS.add(this);
    }

    public static ConveyorType getType(ItemStack stack) {
        if (stack.isEmpty()) return ConveyorType.REGULAR;
        return ConveyorType.VALUES[Math.min(stack.getItemDamage(), ConveyorType.VALUES.length - 1)];
    }

    public static Block getConveyorBlock(ConveyorType type) {
        switch (type) {
            case EXPRESS:
                return ModBlocks.conveyor_express;
            case DOUBLE:
                return ModBlocks.conveyor_double;
            case TRIPLE:
                return ModBlocks.conveyor_triple;
            default:
                return ModBlocks.conveyor;
        }
    }

    public static boolean hasSnakesAndLadders(ConveyorType type) {
        return type == ConveyorType.REGULAR;
    }

    private static int construct(World routeWorld, @Nullable IBlockAccess buildWorld, ConveyorType type, EntityPlayer player, BlockPos pos1,
                                 EnumFacing side1, BlockPos pos2, EnumFacing side2, BlockPos boxOrigin, int max) {
        if (pos1.equals(pos2) && side1 == side2 && (side1 == EnumFacing.UP || side1 == EnumFacing.DOWN)) {
            BlockPos placePos = pos1.offset(side1);
            if (!routeWorld.getBlockState(placePos).getBlock().isReplaceable(routeWorld, placePos)) return -1;
            IBlockState placeState = getConveyorBlock(type).getDefaultState().withProperty(BlockConveyor.FACING, player.getHorizontalFacing());
            if (buildWorld instanceof World) ((World) buildWorld).setBlockState(placePos.subtract(boxOrigin), placeState, 3);
            else if (buildWorld instanceof WorldInAJar) ((WorldInAJar) buildWorld).setBlockState(placePos.subtract(boxOrigin), placeState);
            return 1;
        }

        boolean hasVertical = hasSnakesAndLadders(type);
        BlockPos targetPos = pos2.offset(side2);
        BlockPos currentPos = pos1.offset(side1);
        EnumFacing currentDir = side1.getAxis().isVertical() ? getTargetDirection(currentPos, targetPos, hasVertical) : side1;
        IBlockState targetBlockState = routeWorld.getBlockState(pos2);
        boolean shouldTurnToTarget = side2.getAxis().isHorizontal() || targetBlockState.getBlock() instanceof BlockCraneBase ||
                                     targetBlockState.getBlock() == ModBlocks.conveyor_lift ||
                                     targetBlockState.getBlock() == ModBlocks.conveyor_chute;
        EnumFacing horDir = currentDir.getAxis().isVertical() ? player.getHorizontalFacing().getOpposite() : currentDir;

        if (hasVertical && currentPos.getY() > targetPos.getY() &&
            routeWorld.getBlockState(currentPos.down()).getBlock().isReplaceable(routeWorld, currentPos.down())) {
            currentDir = EnumFacing.DOWN;
        }

        for (int loopDepth = 1; loopDepth <= max; loopDepth++) {
            if (!routeWorld.getBlockState(currentPos).getBlock().isReplaceable(routeWorld, currentPos)) return -1;

            BlockPos nextPos = currentPos.offset(currentDir);
            boolean notAtTarget = (shouldTurnToTarget ? taxiDistance(nextPos, pos2) : taxiDistance(currentPos, targetPos)) > 0;
            boolean willBeObstructed = notAtTarget && !routeWorld.getBlockState(nextPos).getBlock().isReplaceable(routeWorld, nextPos);
            boolean shouldTurn = (taxiDistance(nextPos, targetPos) >= taxiDistance(currentPos, targetPos) && notAtTarget) || willBeObstructed;

            EnumFacing nextDir = currentDir;
            if (shouldTurn) {
                nextDir = getTargetDirection(currentPos, shouldTurnToTarget ? pos2 : targetPos, targetPos, currentDir, willBeObstructed, hasVertical);
            }
            EnumFacing placeDir = currentDir;
            if (hasVertical && currentDir.getAxis().isHorizontal() && nextDir.getAxis().isVertical()) {
                placeDir = nextDir;
            }

            Block placeBlock = getConveyorForDirection(type, placeDir);
            IBlockState placeState;

            if (placeBlock instanceof BlockConveyorBendable) {
                placeState = placeBlock.getDefaultState().withProperty(BlockConveyor.FACING, currentDir.getOpposite());
                if (nextDir != currentDir) {
                    if (currentDir.rotateY() == nextDir)
                        placeState = placeState.withProperty(BlockConveyorBendable.CURVE, BlockConveyorBendable.CurveType.RIGHT);
                    else if (currentDir.rotateYCCW() == nextDir)
                        placeState = placeState.withProperty(BlockConveyorBendable.CURVE, BlockConveyorBendable.CurveType.LEFT);
                }
            } else {
                EnumFacing facing = side2.getAxis().isVertical() ? horDir.getOpposite() : side2;
                placeState = placeBlock.getDefaultState().withProperty(BlockConveyor.FACING, facing);
            }

            if (buildWorld instanceof World) ((World) buildWorld).setBlockState(currentPos.subtract(boxOrigin), placeState, 3);
            else if (buildWorld instanceof WorldInAJar) ((WorldInAJar) buildWorld).setBlockState(currentPos.subtract(boxOrigin), placeState);

            if (currentPos.equals(targetPos)) return loopDepth;

            currentDir = nextDir;
            if (currentDir.getAxis().isHorizontal()) horDir = currentDir;
            currentPos = currentPos.offset(currentDir);
        }
        return 0;
    }

    private static Block getConveyorForDirection(ConveyorType type, EnumFacing dir) {
        if (hasSnakesAndLadders(type)) {
            if (dir == EnumFacing.UP) return ModBlocks.conveyor_lift;
            if (dir == EnumFacing.DOWN) return ModBlocks.conveyor_chute;
        }
        return getConveyorBlock(type);
    }

    private static EnumFacing getTargetDirection(BlockPos from, BlockPos to, boolean hasVertical) {
        return getTargetDirection(from, to, to, null, false, hasVertical);
    }

    private static EnumFacing getTargetDirection(BlockPos from, BlockPos to, BlockPos finalTarget, @Nullable EnumFacing heading,
                                                 boolean willBeObstructed, boolean hasVertical) {
        if (hasVertical && (from.getY() != to.getY() || from.getY() != finalTarget.getY()) &&
            (willBeObstructed || (from.getX() == to.getX() && from.getZ() == to.getZ()) ||
             (from.getX() == finalTarget.getX() && from.getZ() == finalTarget.getZ()))) {
            return from.getY() > to.getY() ? EnumFacing.DOWN : EnumFacing.UP;
        }
        if (Math.abs(from.getX() - to.getX()) > Math.abs(from.getZ() - to.getZ())) {
            if (heading != null && heading.getAxis() == EnumFacing.Axis.X) return from.getZ() > to.getZ() ? EnumFacing.NORTH : EnumFacing.SOUTH;
            return from.getX() > to.getX() ? EnumFacing.WEST : EnumFacing.EAST;
        } else {
            if (heading != null && heading.getAxis() == EnumFacing.Axis.Z) return from.getX() > to.getX() ? EnumFacing.WEST : EnumFacing.EAST;
            return from.getZ() > to.getZ() ? EnumFacing.NORTH : EnumFacing.SOUTH;
        }
    }

    private static int taxiDistance(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (ConveyorType type : ConveyorType.VALUES) {
                items.add(new ItemStack(this, 1, type.ordinal()));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + "." + getType(stack).name().toLowerCase(Locale.US);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            for(String s : I18nUtil.resolveKeyArray(super.getTranslationKey(stack) + ".desc")) {
                tooltip.add(TextFormatting.YELLOW + s);
            }
            if (hasSnakesAndLadders(getType(stack))) {
                tooltip.add(TextFormatting.AQUA + I18nUtil.resolveKey("item.conveyor_wand.vertical.desc"));
            }
        } else {
            tooltip.add(I18nUtil.resolveKey("desc.misc.lshift"));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY,
                                      float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && !stack.hasTagCompound()) {
            IBlockState onState = world.getBlockState(pos);
            Block onBlock = onState.getBlock();
            ConveyorType type = getType(stack);

            if (hasSnakesAndLadders(type) && onBlock == ModBlocks.conveyor &&
                onState.getValue(BlockConveyorBendable.CURVE) == BlockConveyorBendable.CurveType.STRAIGHT) {
                EnumFacing onBlockFacing = onState.getValue(BlockConveyor.FACING);
                if (facing == EnumFacing.UP) {
                    world.setBlockState(pos, ModBlocks.conveyor_lift.getDefaultState().withProperty(BlockConveyor.FACING, onBlockFacing));
                    return EnumActionResult.SUCCESS;
                } else if (facing == EnumFacing.DOWN) {
                    world.setBlockState(pos, ModBlocks.conveyor_chute.getDefaultState().withProperty(BlockConveyor.FACING, onBlockFacing));
                    return EnumActionResult.SUCCESS;
                }
            }

            Block toPlaceBlock = getConveyorBlock(type);
            if (hasSnakesAndLadders(type)) {
                if (onBlock == ModBlocks.conveyor_lift && facing == EnumFacing.UP) toPlaceBlock = ModBlocks.conveyor_lift;
                if (onBlock == ModBlocks.conveyor_chute && facing == EnumFacing.DOWN) toPlaceBlock = ModBlocks.conveyor_chute;
            }

            BlockPos placePos = pos.offset(facing);
            if (world.getBlockState(placePos).getBlock().isReplaceable(world, placePos)) {
                IBlockState defaultState = toPlaceBlock.getDefaultState();
                world.setBlockState(placePos, defaultState, 11);
                IBlockState placedState = world.getBlockState(placePos);
                placedState.getBlock().onBlockPlacedBy(world, placePos, placedState, player, stack);

                if (!player.capabilities.isCreativeMode) stack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }

        // --- Drag-and-Drop Build Logic ---
        IBlockState onState = world.getBlockState(pos);
        if (onState.getBlock() instanceof BlockConveyorBendable bendable) {
            EnumFacing moveDir = stack.hasTagCompound() ? bendable.getInputDirection(world, pos) : bendable.getOutputDirection(world, pos);
            BlockPos adjacentPos = pos.offset(moveDir);
            if (world.getBlockState(adjacentPos).getBlock().isReplaceable(world, adjacentPos)) {
                pos = pos.offset(moveDir);
                facing = moveDir.getOpposite();
            }
        }

        if (!stack.hasTagCompound()) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setLong("pos", pos.toLong());
            nbt.setInteger("side", facing.getIndex());

            int count = player.capabilities.isCreativeMode ? 256 : 0;
            if (!player.capabilities.isCreativeMode) {
                for (ItemStack inventoryStack : player.inventory.mainInventory) {
                    if (!inventoryStack.isEmpty() && inventoryStack.getItem() == this && inventoryStack.getItemDamage() == stack.getItemDamage()) {
                        count += inventoryStack.getCount();
                    }
                }
            }
            nbt.setInteger("count", count);
            stack.setTagCompound(nbt);
        } else {
            NBTTagCompound nbt = stack.getTagCompound();
            BlockPos startPos = BlockPos.fromLong(nbt.getLong("pos"));
            EnumFacing startFacing = EnumFacing.byIndex(nbt.getInteger("side"));
            int count = nbt.getInteger("count");

            if (!world.isRemote) {
                ConveyorType type = getType(stack);
                int constructCount = construct(world, null, type, player, startPos, startFacing, pos, facing, BlockPos.ORIGIN, count);

                if (constructCount > 0) {
                    int toRemove = construct(world, world, type, player, startPos, startFacing, pos, facing, BlockPos.ORIGIN, count);
                    if (!player.capabilities.isCreativeMode) {
                        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                            ItemStack inventoryStack = player.inventory.mainInventory.get(i);
                            if (!inventoryStack.isEmpty() && inventoryStack.getItem() == this &&
                                inventoryStack.getItemDamage() == stack.getItemDamage()) {
                                int removing = Math.min(toRemove, inventoryStack.getCount());
                                inventoryStack.shrink(removing);
                                toRemove -= removing;
                            }
                            if (toRemove <= 0) break;
                        }
                    }
                    player.sendMessage(new TextComponentString("Conveyor built!"));
                } else if (constructCount == 0) {
                    player.sendMessage(new TextComponentString("Not enough conveyors, build cancelled"));
                } else {
                    player.sendMessage(new TextComponentString("Conveyor obstructed, build cancelled"));
                }
            } else {
                RenderOverhead.clearActionPreview();
                lastMop = null;
            }
            stack.setTagCompound(null);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!(entity instanceof EntityPlayer player)) return;

        if (!isSelected && stack.hasTagCompound()) {
            ItemStack held = player.getHeldItemMainhand();
            if (held.isEmpty() || held.getItem() != this || held.getItemDamage() != stack.getItemDamage()) {
                stack.setTagCompound(null);
                if (world.isRemote) {
                    RenderOverhead.clearActionPreview();
                    lastMop = null;
                }
            }
        }

        if (world.isRemote && isSelected && stack.hasTagCompound()) {
            RayTraceResult mop = Minecraft.getMinecraft().objectMouseOver;
            if (mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
                RenderOverhead.clearActionPreview();
                lastMop = null;
                return;
            }

            BlockPos pos = mop.getBlockPos();
            EnumFacing side = mop.sideHit;
            IBlockState onState = world.getBlockState(pos);
            if (onState.getBlock() instanceof BlockConveyorBendable bendable) {
                EnumFacing moveDir = bendable.getInputDirection(world, pos);
                if (world.getBlockState(pos.offset(moveDir)).getBlock().isReplaceable(world, pos.offset(moveDir))) {
                    side = moveDir.getOpposite();
                }
            }

            if (lastMop != null && mop.getBlockPos().equals(lastMop.getBlockPos()) && side == lastSide && Math.abs(lastYaw - player.rotationYaw) < 15)
                return;
            lastMop = mop;
            lastYaw = player.rotationYaw;
            lastSide = side;

            NBTTagCompound nbt = stack.getTagCompound();
            BlockPos startPos = BlockPos.fromLong(nbt.getLong("pos"));
            EnumFacing startSide = EnumFacing.byIndex(nbt.getInteger("side"));
            int count = nbt.getInteger("count");
            BlockPos min = new BlockPos(Math.min(startPos.getX(), pos.getX()) - 1, Math.min(startPos.getY(), pos.getY()) - 1,
                    Math.min(startPos.getZ(), pos.getZ()) - 1);
            int sizeX = Math.abs(startPos.getX() - pos.getX()) + 3;
            int sizeY = Math.abs(startPos.getY() - pos.getY()) + 3;
            int sizeZ = Math.abs(startPos.getZ() - pos.getZ()) + 3;

            WorldInAJar wiaj = new WorldInAJar(sizeX, sizeY, sizeZ);
            boolean pathSuccess = construct(world, wiaj, getType(stack), player, startPos, startSide, pos, side, min, count) > 0;
            RenderOverhead.setActionPreview(wiaj, min, pathSuccess);
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        if (!player.isSneaking() || !player.capabilities.isCreativeMode || !(player instanceof EntityPlayerMP)) return false;
        World world = player.world;
        if (!world.isRemote && world.getBlockState(pos).getBlock() instanceof BlockConveyorBase) {
            breakExtra(world, (EntityPlayerMP) player, pos, 32);
        }
        return false;
    }

    private void breakExtra(World world, EntityPlayerMP player, BlockPos pos, int depth) {
        if (depth <= 0) return;
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockConveyorBase conveyor)) return;

        int event = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
        if (event == -1) return;

        if (state.getBlock().removedByPlayer(state, world, pos, player, false)) {
            state.getBlock().onPlayerDestroy(world, pos, state);
        }
        player.connection.sendPacket(new SPacketBlockChange(world, pos));

        breakExtra(world, player, pos.offset(conveyor.getInputDirection(world, pos)), depth - 1);
        breakExtra(world, player, pos.offset(conveyor.getOutputDirection(world, pos)), depth - 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(Pre event, World world, BlockPos pos) {
        EntityPlayer player = MainRegistry.proxy.me();
        if (player == null || !player.isSneaking() || !player.capabilities.isCreativeMode) return;
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockConveyorBase) {
            List<String> text = new ArrayList<>();
            text.add("Break whole conveyor line");
            ILookOverlay.printGeneric(event, I18nUtil.resolveKey(state.getBlock().getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
        }
    }

    public enum ConveyorType {
        REGULAR,
        EXPRESS,
        DOUBLE,
        TRIPLE;

        public static final ConveyorType[] VALUES = values();
    }
}