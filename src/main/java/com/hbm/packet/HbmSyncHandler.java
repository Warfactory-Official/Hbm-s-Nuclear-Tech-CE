package com.hbm.packet;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmCapability.IHBMData;
import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.capability.HbmLivingProps;
import com.hbm.capability.HbmLivingProps.ContaminationEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HbmSyncHandler {

    public static final byte HBM_SYNC_VERSION = 1;

    public static final byte FLAG_RADIATION     = 1;
    public static final byte FLAG_DIGAMMA       = 2;
    public static final byte FLAG_STATUS        = 4;
    public static final byte FLAG_CONTAMINATION = 8;
    public static final byte FLAG_SHIELD        = 16;
    public static final byte FLAG_TOGGLES       = 32;
    public static final byte FLAG_REPUTATION    = 64;

    // playerStates keeps per-player hashes to compute dirty flags — cleaned up on logout to avoid leaks
    private static final ConcurrentHashMap<UUID, PlayerSyncState> playerStates = new ConcurrentHashMap<>();

    public static void removePlayer(UUID uuid) {
        playerStates.remove(uuid);
    }

    private static class PlayerSyncState {
        int contaminationHash;
        int shieldBits;
        int toggleBits;
        int lastRep;
    }

    public static byte computeFlags(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        PlayerSyncState state = playerStates.get(uuid);
        boolean isNew = state == null;
        if(isNew) {
            state = new PlayerSyncState();
            playerStates.put(uuid, state);
        }

        byte flags = 0;
        int tick = player.ticksExisted + Math.abs(player.getEntityId());

        if(isNew || tick % 5 == 0)  flags |= FLAG_RADIATION;
        if(isNew || tick % 10 == 0) flags |= FLAG_DIGAMMA;
        if(isNew || tick % 5 == 0)  flags |= FLAG_STATUS;

        IEntityHbmProps living = HbmLivingProps.getData(player);
        IHBMData cap = HbmCapability.getData(player);

        int contHash = living.getContaminationEffectList().hashCode();
        if(isNew || contHash != state.contaminationHash) {
            flags |= FLAG_CONTAMINATION;
            state.contaminationHash = contHash;
        }

        int sBits = Float.floatToIntBits(cap.getShield()) * 31 + Float.floatToIntBits(cap.getMaxShield());
        if(isNew || sBits != state.shieldBits) {
            flags |= FLAG_SHIELD;
            state.shieldBits = sBits;
        }

        int tBits = (cap.hasReceivedBook() ? 1 : 0)
                  | (cap.getEnableBackpack() ? 2 : 0)
                  | (cap.getEnableHUD() ? 4 : 0)
                  | (cap.getEnableMagnet() ? 8 : 0);
        if(isNew || tBits != state.toggleBits) {
            flags |= FLAG_TOGGLES;
            state.toggleBits = tBits;
        }

        int rep = cap.getReputation();
        if(isNew || rep != state.lastRep) {
            flags |= FLAG_REPUTATION;
            state.lastRep = rep;
        }

        return flags;
    }

    public static void writePacket(ByteBuf buf, byte flags, ContaminationEffect[] contaminationSnapshot, EntityPlayerMP player) {
        buf.writeByte(HBM_SYNC_VERSION);
        buf.writeByte(flags);

        IEntityHbmProps living = HbmLivingProps.getData(player);
        IHBMData cap = HbmCapability.getData(player);

        if((flags & FLAG_RADIATION) != 0) {
            buf.writeDouble(living.getRads());
            buf.writeDouble(living.getNeutrons());
            buf.writeDouble(living.getRadsEnv());
            buf.writeDouble(living.getRadBuf());
        }
        if((flags & FLAG_DIGAMMA) != 0) {
            buf.writeDouble(living.getDigamma());
        }
        if((flags & FLAG_STATUS) != 0) {
            buf.writeInt(living.getAsbestos());
            buf.writeInt(living.getBlacklung());
            buf.writeInt(living.getBombTimer());
            buf.writeInt(living.getContagion());
            buf.writeInt(living.getOil());
            buf.writeInt(living.getPhosphorus());
            buf.writeInt(living.getFire());
            buf.writeInt(living.getBalefire());
        }
        if((flags & FLAG_CONTAMINATION) != 0) {
            buf.writeInt(contaminationSnapshot.length);
            for(ContaminationEffect e : contaminationSnapshot) e.writeTo(buf);
        }
        if((flags & FLAG_SHIELD) != 0) {
            buf.writeFloat(cap.getShield());
            buf.writeFloat(cap.getMaxShield());
        }
        if((flags & FLAG_TOGGLES) != 0) {
            buf.writeBoolean(cap.hasReceivedBook());
            buf.writeBoolean(cap.getEnableBackpack());
            buf.writeBoolean(cap.getEnableHUD());
            buf.writeBoolean(cap.getEnableMagnet());
        }
        if((flags & FLAG_REPUTATION) != 0) {
            buf.writeInt(cap.getReputation());
        }
    }

    @SideOnly(Side.CLIENT)
    public static void readPacket(ByteBuf buf) {
        byte version = buf.readByte();
        if(version != HBM_SYNC_VERSION) return;

        byte flags = buf.readByte();
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null) return;

        IEntityHbmProps living = HbmLivingProps.getData(player);
        IHBMData cap = HbmCapability.getData(player);

        if((flags & FLAG_RADIATION) != 0) {
            living.setRads(buf.readDouble());
            living.setNeutrons(buf.readDouble());
            living.setRadsEnv(buf.readDouble());
            living.setRadBuf(buf.readDouble());
        }
        if((flags & FLAG_DIGAMMA) != 0) {
            living.setDigamma(buf.readDouble());
        }
        if((flags & FLAG_STATUS) != 0) {
            living.setAsbestos(buf.readInt());
            living.setBlacklung(buf.readInt());
            living.setBombTimer(buf.readInt());
            living.setContagion(buf.readInt());
            living.setOil(buf.readInt());
            living.setPhosphorus(buf.readInt());
            living.setFire(buf.readInt());
            living.setBalefire(buf.readInt());
        }
        if((flags & FLAG_CONTAMINATION) != 0) {
            List<ContaminationEffect> effects = living.getContaminationEffectList();
            effects.clear();
            int size = buf.readInt();
            for(int i = 0; i < size; i++) effects.add(ContaminationEffect.readFrom(buf));
        }
        if((flags & FLAG_SHIELD) != 0) {
            cap.setShield(buf.readFloat());
            cap.setMaxShield(buf.readFloat());
        }
        if((flags & FLAG_TOGGLES) != 0) {
            cap.setReceivedBook(buf.readBoolean());
            cap.setEnableBackpack(buf.readBoolean());
            cap.setEnableHUD(buf.readBoolean());
            cap.setEnableMagnet(buf.readBoolean());
        }
        if((flags & FLAG_REPUTATION) != 0) {
            cap.setReputation(buf.readInt());
        }
    }
}
