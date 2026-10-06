package com.hbm.integration.schematica;

import com.hbm.blocks.BlockDummyable;
import com.hbm.integration.SchematicFluidSync;
import com.hbm.main.MainRegistry;
import com.hbm.util.Compat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class SchematicaCompat {

    private static final String MISSING_ITEM = "Could not find the item for: ";
    private static Field schematicField;
    private static Field positionField;
    private static Object printer;
    private static Method isPrinting;
    private static Method swapToItem;

    private SchematicaCompat() {
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        try {
            Class<?> registryClass = Class.forName("com.github.lunatrius.schematica.client.printer.registry.PlacementRegistry");
            Class<?> dataClass = Class.forName("com.github.lunatrius.schematica.client.printer.registry.PlacementData");
            Class<?> facingClass = Class.forName("com.github.lunatrius.schematica.client.printer.registry.IValidPlayerFacing");
            schematicField = Class.forName("com.github.lunatrius.schematica.proxy.ClientProxy").getField("schematic");
            positionField = schematicField.getType().getField("position");

            Object validator = Proxy.newProxyInstance(facingClass.getClassLoader(), new Class<?>[]{facingClass}, (proxy, method, args) ->
                    method.getDeclaringClass() == Object.class ? method.invoke(SchematicaCompat.class, args)
                            : isValidFacing((IBlockState) args[0], (EntityPlayer) args[1], (BlockPos) args[2]));
            Object data = dataClass.getConstructor(facingClass).newInstance(validator);

            Field mapField = registryClass.getDeclaredField("blockPlacementMap");
            mapField.setAccessible(true);
            Map<Block, Object> map = (Map<Block, Object>) mapField.get(registryClass.getField("INSTANCE").get(null));
            for (Block block : ForgeRegistries.BLOCKS) {
                if (block instanceof BlockDummyable) map.put(block, data);
            }

            Class<?> printerClass = Class.forName("com.github.lunatrius.schematica.client.printer.SchematicPrinter");
            printer = printerClass.getField("INSTANCE").get(null);
            isPrinting = printerClass.getMethod("isPrinting");
            swapToItem = printerClass.getDeclaredMethod("swapToItem", InventoryPlayer.class, ItemStack.class);
            swapToItem.setAccessible(true);
            MinecraftForge.EVENT_BUS.register(SchematicaCompat.class);
        } catch (Exception e) {
            MainRegistry.logger.error("Failed to register Schematica printer compat", e);
        }

        if (LogManager.getLogger(Compat.ModIds.SCHEMATICA) instanceof Logger logger) {
            logger.addFilter(new AbstractFilter() {
                @Override
                public Result filter(LogEvent event) {
                    Object[] params = event.getMessage().getParameters();
                    return params != null && params.length > 0 && params[0] instanceof IBlockState state && state.getBlock() instanceof BlockDummyable
                            && event.getMessage().getFormattedMessage().startsWith(MISSING_ITEM) ? Result.DENY : Result.NEUTRAL;
                }
            });
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (event.phase != TickEvent.Phase.END || player == null || mc.world == null || player.ticksExisted % 4 != 0) return;
        try {
            if (!(boolean) isPrinting.invoke(printer)) return;
            World schematic = (World) schematicField.get(null);
            if (schematic == null) return;
            Vec3i offset = (Vec3i) positionField.get(schematic);
            BlockPos center = new BlockPos(player);
            for (BlockPos pos : BlockPos.getAllInBox(center.add(-4, -4, -4), center.add(4, 4, 4))) {
                IBlockState placed = mc.world.getBlockState(pos);
                if (placed.getMaterial() == Material.AIR) continue;
                BlockPos schematicPos = pos.subtract(offset);
                IBlockState target = schematic.getBlockState(schematicPos);
                if (target.getBlock() != placed.getBlock()) continue;
                if (placed.getBlock().hasTileEntity(placed)) SchematicFluidSync.sync(schematic, schematicPos, pos, true);
                if (!(placed.getBlock() instanceof BlockDummyable block) || placed.getValue(BlockDummyable.META) < 12 || target == placed) continue;
                ItemStack stack = block.getSchematicFinishItem(target.getValue(BlockDummyable.META), placed.getValue(BlockDummyable.META));
                if (stack.isEmpty()) continue;
                int slot = player.inventory.currentItem;
                if (!(boolean) swapToItem.invoke(printer, player.inventory, stack)) continue;
                mc.playerController.processRightClickBlock(player, mc.world, pos, EnumFacing.UP, new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), EnumHand.MAIN_HAND);
                player.inventory.currentItem = slot;
                return;
            }
        } catch (ReflectiveOperationException e) {
            MainRegistry.logger.error("Schematica printer compat failed", e);
            MinecraftForge.EVENT_BUS.unregister(SchematicaCompat.class);
        }
    }

    private static boolean isValidFacing(IBlockState state, EntityPlayer player, BlockPos pos) throws IllegalAccessException {
        if (!(state.getBlock() instanceof BlockDummyable block)) return true;
        World schematic = (World) schematicField.get(null);
        if (schematic == null) return true;
        Vec3i offset = (Vec3i) positionField.get(schematic);
        BlockPos core = block.findCore(schematic, pos.subtract(offset));
        return core != null && block.isValidSchematicFacing(player, core.add(offset), schematic.getBlockState(core).getValue(BlockDummyable.META));
    }
}
