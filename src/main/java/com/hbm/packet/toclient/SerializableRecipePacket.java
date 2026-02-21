package com.hbm.packet.toclient;

import com.hbm.inventory.recipes.loader.SerializableRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.nio.file.Files;

public class SerializableRecipePacket extends PrecompiledPacket {

    private String filename;
    private File fileSource;
    private byte[] rawBytes;
    private boolean reinit;
    private ByteBuf payload;
    private int dataLength;

    public SerializableRecipePacket() {}

    public SerializableRecipePacket(File recipeFile) {
        this.filename = recipeFile.getName();
        this.fileSource = recipeFile;
        this.reinit = false;
    }

    public SerializableRecipePacket(String filename, byte[] fileBytes) {
        this.filename = filename;
        this.rawBytes = fileBytes;
        this.reinit = false;
    }

    public SerializableRecipePacket(boolean reinit) {
        this.reinit = reinit;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(reinit);
        if(reinit) return;

        BufferUtil.writeString(buf, filename);

        byte[] dataToSend = rawBytes;
        if (fileSource != null) {
            try {
                dataToSend = Files.readAllBytes(fileSource.toPath());
            } catch (Exception e) {
                MainRegistry.logger.error("Failed to read recipe file for packet: {}", filename, e);
                dataToSend = new byte[0];
            }
        }

        if (dataToSend == null) dataToSend = new byte[0];

        buf.writeInt(dataToSend.length);
        buf.writeBytes(dataToSend);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        reinit = buf.readBoolean();
        if(reinit) return;
        filename = BufferUtil.readString(buf);
        dataLength = buf.readInt();
        this.payload = buf.retain();
    }

    public static class Handler implements IMessageHandler<SerializableRecipePacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(SerializableRecipePacket m, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    handle(m);
                } catch (Exception e) {
                    MainRegistry.logger.catching(e);
                } finally {
                    m.payload.release();
                }
            });
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handle(SerializableRecipePacket m) {
            if(m.reinit) {
                SerializableRecipe.initialize();
                return;
            }

            if (m.payload != null && m.dataLength > 0) {
                byte[] data = new byte[m.dataLength];
                if (m.payload.readableBytes() >= m.dataLength) {
                    m.payload.readBytes(data);
                    SerializableRecipe.receiveRecipes(m.filename, data);
                } else {
                    MainRegistry.logger.error("Recipe packet buffer underrun for {}", m.filename);
                }
            }
        }
    }
}
