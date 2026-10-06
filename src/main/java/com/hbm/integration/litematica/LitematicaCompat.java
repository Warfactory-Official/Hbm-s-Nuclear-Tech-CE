package com.hbm.integration.litematica;

import com.hbm.blocks.BlockDummyable;
import com.hbm.integration.SchematicFluidSync;
import com.hbm.main.MainRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
public final class LitematicaCompat {

    private static Method getGenericTrace;
    private static Method getHitType;
    private static Method getRayTraceResult;
    private static Method getSchematicWorld;
    private static boolean failed;
    private static long lastAction;
    private static World pendingWorld;
    private static BlockPos pendingPos;

    private LitematicaCompat() {
    }

    public static void afterEasyPlace(EnumActionResult result) {
        if (pendingPos != null && result == EnumActionResult.SUCCESS) SchematicFluidSync.sync(pendingWorld, pendingPos, pendingPos, false);
        pendingWorld = null;
        pendingPos = null;
    }

    private static boolean syncPlacedFluid(Minecraft mc, World schematic, BlockPos pos) {
        IBlockState placed = mc.world.getBlockState(pos);
        if (placed.getBlock() instanceof BlockDummyable block) pos = block.findCore(mc.world, pos);
        return pos != null && schematic.getBlockState(pos).getBlock() == placed.getBlock() && SchematicFluidSync.sync(schematic, pos, pos, true);
    }

    public static EnumActionResult easyPlace(Minecraft mc) {
        pendingWorld = null;
        pendingPos = null;
        EntityPlayerSP player = mc.player;
        if (failed || player == null || mc.world == null) return null;
        try {
            if (getGenericTrace == null) {
                Class<?> wrapperClass = Class.forName("fi.dy.masa.litematica.util.RayTraceUtils$RayTraceWrapper");
                getHitType = wrapperClass.getMethod("getHitType");
                getRayTraceResult = wrapperClass.getMethod("getRayTraceResult");
                getSchematicWorld = Class.forName("fi.dy.masa.litematica.world.SchematicWorldHandler").getMethod("getSchematicWorld");
                getGenericTrace = Class.forName("fi.dy.masa.litematica.util.RayTraceUtils").getMethod("getGenericTrace", World.class, Entity.class, double.class, boolean.class);
            }

            Object wrapper = getGenericTrace.invoke(null, mc.world, player, 6D, true);
            World schematic = (World) getSchematicWorld.invoke(null);
            if (wrapper == null || schematic == null) return null;
            RayTraceResult trace = (RayTraceResult) getRayTraceResult.invoke(wrapper);
            if (trace == null || trace.typeOfHit != RayTraceResult.Type.BLOCK) return null;
            String type = ((Enum<?>) getHitType.invoke(wrapper)).name();
            if (!type.equals("SCHEMATIC_BLOCK") && !type.equals("VANILLA")) return null;
            if (syncPlacedFluid(mc, schematic, trace.getBlockPos())) return EnumActionResult.SUCCESS;

            boolean placed = type.equals("VANILLA") || mc.world.getBlockState(trace.getBlockPos()).getBlock() instanceof BlockDummyable;
            EnumActionResult result = placed ? finishMultiblock(mc, player, schematic, trace) : placeMultiblock(mc, player, schematic, trace);
            if (result == null && !placed) {
                pendingWorld = schematic;
                pendingPos = trace.getBlockPos();
            }
            return result;
        } catch (ReflectiveOperationException e) {
            failed = true;
            MainRegistry.logger.error("Litematica easy place compat failed", e);
            return null;
        }
    }

    private static EnumActionResult placeMultiblock(Minecraft mc, EntityPlayerSP player, World schematic, RayTraceResult trace) {
        if (!(schematic.getBlockState(trace.getBlockPos()).getBlock() instanceof BlockDummyable block)) return null;
        BlockPos core = block.findCore(schematic, trace.getBlockPos());
        if (core == null) return null;
        if (isThrottled(mc)) return EnumActionResult.SUCCESS;

        BlockPos origin = block.getSchematicOrigin(schematic, core);
        if (!mc.world.getBlockState(origin).getBlock().isReplaceable(mc.world, origin)) return EnumActionResult.FAIL;
        if (player.getDistanceSq(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5) >= 49) return EnumActionResult.FAIL;

        int coreMeta = schematic.getBlockState(core).getValue(BlockDummyable.META);
        float playerYaw = player.rotationYaw;
        boolean valid = false;
        for (int i = 0; i < 8 && !valid; i++) {
            player.rotationYaw = playerYaw + i * 45F;
            valid = block.isValidSchematicFacing(player, core, coreMeta);
        }
        float yaw = player.rotationYaw;
        player.rotationYaw = playerYaw;
        if (!valid) return EnumActionResult.FAIL;

        ItemStack stack = block.getPickBlock(schematic.getBlockState(origin), trace, schematic, origin, player);
        if (stack.isEmpty()) return null;
        EnumActionResult picked = pickItem(mc, player, stack);
        if (picked != null) return picked;

        if (yaw != playerYaw) {
            player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, player.rotationPitch, player.onGround));
            player.rotationYaw = yaw;
        }
        mc.playerController.processRightClickBlock(player, mc.world, origin, EnumFacing.UP, new Vec3d(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5), EnumHand.MAIN_HAND);
        if (yaw != playerYaw) {
            player.rotationYaw = playerYaw;
            player.connection.sendPacket(new CPacketPlayer.Rotation(playerYaw, player.rotationPitch, player.onGround));
        }
        SchematicFluidSync.sync(schematic, core, core, false);
        lastAction = mc.world.getTotalWorldTime();
        return EnumActionResult.SUCCESS;
    }

    private static EnumActionResult finishMultiblock(Minecraft mc, EntityPlayerSP player, World schematic, RayTraceResult trace) {
        BlockPos pos = trace.getBlockPos();
        if (!(mc.world.getBlockState(pos).getBlock() instanceof BlockDummyable block)) return null;
        BlockPos core = block.findCore(mc.world, pos);
        if (core == null) return null;
        IBlockState target = schematic.getBlockState(core);
        if (target.getBlock() != block) return null;
        ItemStack stack = block.getSchematicFinishItem(target.getValue(BlockDummyable.META), mc.world.getBlockState(core).getValue(BlockDummyable.META));
        if (stack.isEmpty()) return null;
        if (isThrottled(mc)) return EnumActionResult.SUCCESS;
        EnumActionResult picked = pickItem(mc, player, stack);
        if (picked != null) return picked;

        mc.playerController.processRightClickBlock(player, mc.world, pos, trace.sideHit, trace.hitVec, EnumHand.MAIN_HAND);
        lastAction = mc.world.getTotalWorldTime();
        return EnumActionResult.SUCCESS;
    }

    private static boolean isThrottled(Minecraft mc) {
        long delta = mc.world.getTotalWorldTime() - lastAction;
        return delta >= 0 && delta < 4;
    }

    private static EnumActionResult pickItem(Minecraft mc, EntityPlayerSP player, ItemStack stack) {
        InventoryPlayer inventory = player.inventory;
        if (ItemStack.areItemsEqual(inventory.getCurrentItem(), stack)) return null;
        for (int slot = 0; slot < inventory.mainInventory.size(); slot++) {
            if (!ItemStack.areItemsEqual(inventory.mainInventory.get(slot), stack)) continue;
            if (InventoryPlayer.isHotbar(slot)) {
                inventory.currentItem = slot;
                return null;
            }
            mc.playerController.pickItem(slot);
            return EnumActionResult.SUCCESS;
        }
        if (!player.capabilities.isCreativeMode) return EnumActionResult.FAIL;
        inventory.setPickedItemStack(stack);
        mc.playerController.sendSlotPacket(player.getHeldItem(EnumHand.MAIN_HAND), 36 + inventory.currentItem);
        return null;
    }
}
