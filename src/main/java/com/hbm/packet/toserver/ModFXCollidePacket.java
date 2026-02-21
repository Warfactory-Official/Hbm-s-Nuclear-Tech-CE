package com.hbm.packet.toserver;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockCloudResidue;
import com.hbm.blocks.machine.PinkCloudBroadcaster;
import com.hbm.blocks.machine.RadioRec;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionNukeGeneric;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
// Th3_Sl1ze: so, since ModEntityFX is non-existent and all related classes are either deleted or moved to inherit Particle,
// which is a client-side class, I have to make this goofy packet.
// Hopefully no one's going to kill me after this..
// mlbv: this is a severe security risk...
public class ModFXCollidePacket implements IMessage {

    public enum Action {
        SOLINIUM_AREA,
        PLACE_RESIDUE,
        CONVERT_RADIOREC,
        EXPLOSION_PC,
        EXPLOSION_POISON,
        EXPLOSION_C;

        public static final Action[] VALUES = values();
    }

    private Action action;
    private BlockPos pos;
    private BlockPos extra; // optional (e.g. prevPos for residue), may be null
    private int intArg;     // optional (e.g. radius for explosions)

    public ModFXCollidePacket() { }

    public ModFXCollidePacket(Action action, BlockPos pos) {
        this(action, pos, null, -1);
    }

    public ModFXCollidePacket(Action action, BlockPos pos, int intArg) {
        this(action, pos, null, intArg);
    }

    public ModFXCollidePacket(Action action, BlockPos pos, BlockPos extra) {
        this(action, pos, extra, -1);
    }

    public ModFXCollidePacket(Action action, BlockPos pos, BlockPos extra, int intArg) {
        this.action = action;
        this.pos = pos.toImmutable();
        this.extra = extra == null ? null : extra.toImmutable();
        this.intArg = intArg;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.action.ordinal());
        buf.writeLong(this.pos.toLong());
        buf.writeBoolean(this.extra != null);
        if (this.extra != null) {
            buf.writeLong(this.extra.toLong());
        }
        buf.writeInt(this.intArg);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = Action.VALUES[buf.readByte()];
        this.pos = BlockPos.fromLong(buf.readLong());
        if (buf.readBoolean()) {
            this.extra = BlockPos.fromLong(buf.readLong());
        } else {
            this.extra = null;
        }
        this.intArg = buf.readInt();
    }

    public static class Handler implements IMessageHandler<ModFXCollidePacket, IMessage> {
        @Override
        public IMessage onMessage(ModFXCollidePacket msg, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            world.addScheduledTask(() -> {
                switch (msg.action) {
                    case SOLINIUM_AREA -> {
                        BlockPos c = msg.pos;
                        for (int a = -1; a < 2; a++) {
                            for (int b = -1; b < 2; b++) {
                                for (int d = -1; d < 2; d++) {
                                    ExplosionNukeGeneric.solinium(world, c.add(a, b, d));
                                }
                            }
                        }
                    }
                    case PLACE_RESIDUE -> {
                        BlockPos prevPos = msg.pos;
                        if (BlockCloudResidue.hasPosNeightbour(world, prevPos)) {
                            IBlockState st = world.getBlockState(prevPos);
                            if (st.getBlock().isReplaceable(world, prevPos)) {
                                if (world.rand.nextInt(5) != 0) {
                                    world.setBlockState(prevPos, ModBlocks.residue.getDefaultState());
                                }
                            }
                        }
                    }
                    case CONVERT_RADIOREC -> {
                        BlockPos p = msg.pos;
                        IBlockState s = world.getBlockState(p);
                        if (s.getBlock() == ModBlocks.radiorec) {
                            EnumFacing e = s.getValue(RadioRec.FACING);
                            world.setBlockState(p, ModBlocks.broadcaster_pc.getDefaultState().withProperty(PinkCloudBroadcaster.FACING, e), 2);
                        }
                    }
                    case EXPLOSION_PC -> {
                        BlockPos p = msg.pos;
                        int r = msg.intArg > 0 ? msg.intArg : 2;
                        ExplosionChaos.pc(world, p.getX(), p.getY(), p.getZ(), r);
                    }
                    case EXPLOSION_POISON -> {
                        BlockPos p = msg.pos;
                        int r = msg.intArg > 0 ? msg.intArg : 2;
                        ExplosionChaos.poison(world, p.getX(), p.getY(), p.getZ(), r);
                    }
                    case EXPLOSION_C -> {
                        BlockPos p = msg.pos;
                        int r = msg.intArg > 0 ? msg.intArg : 2;
                        ExplosionChaos.c(world, p.getX(), p.getY(), p.getZ(), r);
                    }
                }
            });
            return null;
        }
    }
}
