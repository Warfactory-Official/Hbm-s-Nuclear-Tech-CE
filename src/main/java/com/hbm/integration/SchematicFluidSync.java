package com.hbm.integration;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.SchematicFluidPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class SchematicFluidSync {

    private static final Map<BlockPos, Long> SENT = new HashMap<>();

    private SchematicFluidSync() {
    }

    public static boolean sync(World schematic, BlockPos schematicPos, BlockPos pos, boolean placed) {
        Minecraft mc = Minecraft.getMinecraft();
        FluidType wanted = SchematicFluidPacket.getFluid(schematic.getTileEntity(schematicPos));
        if (wanted == null || wanted == Fluids.NONE) return false;
        if (placed) {
            FluidType current = SchematicFluidPacket.getFluid(mc.world.getTileEntity(pos));
            if (current == null || current == wanted) return false;
        }
        if (!SchematicFluidPacket.hasIdentifier(mc.player)) return false;

        long time = mc.world.getTotalWorldTime();
        Long last = SENT.get(pos);
        if (last != null && time >= last && time - last < 100) return false;
        if (SENT.size() > 1024) SENT.clear();
        pos = pos.toImmutable();
        SENT.put(pos, time);
        PacketDispatcher.wrapper.sendToServer(new SchematicFluidPacket(pos, wanted));
        return true;
    }
}
