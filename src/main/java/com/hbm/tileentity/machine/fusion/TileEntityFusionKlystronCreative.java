package com.hbm.tileentity.machine.fusion;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.recipes.FusionRecipes;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.KlystronNetwork;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityFusionKlystronCreative extends TileEntityLoadedBase implements ITickable {

    protected KlystronNetwork.KlystronNode klystronNode;

    public float fan;
    public float prevFan;
    public float fanSpeed;
    public static final float FAN_ACCELERATION = 0.125F;

    public boolean isConnected = false;

    private AudioWrapper audio;

    @Override
    public void update() {

        if(!world.isRemote) {

            this.klystronNode = TileEntityFusionKlystron.handleKNode(klystronNode, this);
            this.isConnected = TileEntityFusionKlystron.provideKyU(klystronNode, FusionRecipes.INSTANCE.maxInput);

            this.networkPackNT(100);

        } else {

            if(this.isConnected) this.fanSpeed += FAN_ACCELERATION;
            else this.fanSpeed -= FAN_ACCELERATION;

            this.fanSpeed = MathHelper.clamp(this.fanSpeed, 0F, 5F);

            this.prevFan = this.fan;
            this.fan += this.fanSpeed;

            if(this.fan >= 360F) {
                this.fan -= 360F;
                this.prevFan -= 360F;
            }

            if(this.fanSpeed > 0 && MainRegistry.proxy.me().getDistanceSq(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5) < 30 * 30) {

                float speed = this.fanSpeed / 5F;

                if(audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.fel, SoundCategory.BLOCKS, pos.getX() + 0.5F, pos.getY() + 2.5F, pos.getZ() + 0.5F, getVolume(speed), 15F, speed, 20);
                    audio.startSound();
                } else {
                    audio.updateVolume(getVolume(speed));
                    audio.updatePitch(speed);
                    audio.keepAlive();
                }

            } else {

                if(audio != null) {
                    if(audio.isPlaying()) audio.stopSound();
                    audio = null;
                }
            }
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if(audio != null) {
            audio.stopSound();
            audio = null;
        }

        if(!world.isRemote && this.klystronNode != null) {
            UniNodespace.destroyNode(world, klystronNode);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(isConnected);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.isConnected = buf.readBoolean();
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 4,
                    pos.getY(),
                    pos.getZ() - 4,
                    pos.getX() + 5,
                    pos.getY() + 5,
                    pos.getZ() + 5
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
