package com.hbm.packet.toclient;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.sound.*;
import com.hbm.tileentity.machine.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.Supplier;

public class LoopedSoundPacket implements IMessage {

    private int x;
    private int y;
    private int z;

    public LoopedSoundPacket() {
    }

    public LoopedSoundPacket(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public LoopedSoundPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<LoopedSoundPacket, IMessage> {
        private static void process(LoopedSoundPacket msg) {
            Minecraft mc = Minecraft.getMinecraft();
            TileEntity te = mc.world.getTileEntity(new BlockPos(msg.x, msg.y, msg.z));
            if (te == null) return;
            if (te instanceof TileEntityFEL fel && fel.isOn) {
                playUniqueSound(fel, SoundLoopFel.list, () -> new SoundLoopFel(HBMSoundHandler.fel, fel));
            } else if (te instanceof TileEntityMachineMiningLaser laser && laser.isOn) {
                playUniqueSound(laser, SoundLoopFel.list, () -> new SoundLoopFel(HBMSoundHandler.fel, laser));
            } else if (te instanceof TileEntityMachineTurbofan turbofan && turbofan.wasOn) {
                playUniqueSound(turbofan, SoundLoopTurbofan.list, () -> new SoundLoopTurbofan(HBMSoundHandler.turbofanOperate, turbofan));
            } else if (te instanceof TileEntityMachineCentrifuge centrifuge && centrifuge.isProgressing) {
                playUniqueSound(centrifuge, SoundLoopCentrifuge.list, () -> new SoundLoopCentrifuge(HBMSoundHandler.centrifugeOperate, centrifuge));
            } else if (te instanceof TileEntityMachineGasCent gasCent && gasCent.isProgressing) {
                playUniqueSound(gasCent, SoundLoopCentrifuge.list, () -> new SoundLoopCentrifuge(HBMSoundHandler.centrifugeOperate, gasCent));
            } else if (te instanceof TileEntityHeatBoilerIndustrial boiler) {
                SoundEvent event = switch (Math.abs(te.getPos().hashCode()) % 3) {
                    case 1 -> HBMSoundHandler.boiler_groan2;
                    case 2 -> HBMSoundHandler.boiler_groan3;
                    default -> HBMSoundHandler.boiler_groan1;
                };
                playUniqueSound(boiler, SoundLoopHeatBoilerIndustrial.list, () -> new SoundLoopHeatBoilerIndustrial(event, boiler));
            }
        }

        private static <T extends TileEntity, L extends SoundLoopMachine> void playUniqueSound(T te, List<L> soundLoops, Supplier<L> loopFactory) {
            boolean alreadyPlaying = soundLoops.stream().anyMatch(loop -> loop.getTE() == te && !loop.isDonePlaying());
            if (!alreadyPlaying && te.getWorld().isRemote) {
                Minecraft.getMinecraft().getSoundHandler().playSound(loopFactory.get());
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(LoopedSoundPacket msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> process(msg));
            return null;
        }
    }
}
