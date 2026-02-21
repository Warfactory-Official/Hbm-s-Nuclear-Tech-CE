package com.hbm.tileentity.turret;

import com.hbm.handler.CasingEjector;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretJeremy;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryTurret;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.CasingCreator;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Vec3dUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretJeremy extends TileEntityTurretBaseNT implements IGUIProvider {

    protected static CasingEjector ejector = new CasingEjector().setAngleRange(0.01F, 0.01F).setMotion(0, 0, -0.2);
    private static List<Integer> configs = new ArrayList<>();

    static {
        configs.add(XFactoryTurret.shell_normal.id);
        configs.add(XFactoryTurret.shell_explosive.id);
        configs.add(XFactoryTurret.shell_ap.id);
        configs.add(XFactoryTurret.shell_du.id);
        configs.add(XFactoryTurret.shell_w9.id);
    }

    public int timer;
    public int reload;

    @Override
    protected List<Integer> getAmmoList() {
        return configs;
    }

    @Override
    public String getDefaultName() {
        return "container.turretJeremy";
    }

    @Override
    public double getDecetorGrace() {
        return 16D;
    }

    @Override
    public double getTurretDepression() {
        return 45D;
    }

    @Override
    public long getMaxPower() {
        return 10000;
    }

    @Override
    public double getBarrelLength() {
        return 4.25D;
    }

    @Override
    public double getDecetorRange() {
        return 80D;
    }

    @Override
    public void update() {
        if (reload > 0)
            reload--;

        if (reload == 1)
            this.world.playSound(null, pos, HBMSoundHandler.jeremy_reload, SoundCategory.BLOCKS, 2.0F, 1.0F);

        super.update();
    }

    @Override
    public void updateFiringTick() {
        timer++;

        if (timer % 40 == 0) {

            BulletConfig conf = this.getFirstConfigLoaded();

            if (conf != null) {
                this.cachedCasingConfig = conf.casing;
                this.spawnBullet(conf, 50F);
                this.consumeAmmo(conf.ammo);
                this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.jeremy_fire, SoundCategory.BLOCKS, 4.0F, 1.0F);

                Vec3 pos = new Vec3(this.getTurretPos());
                Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
                vec.rotateAroundZ((float) -this.rotationPitch);
                vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));

                reload = 20;

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "vanillaExt");
                data.setString("mode", "largeexplode");
                data.setFloat("size", 0F);
                data.setByte("count", (byte) 5);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord), new TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50));
            }
        }
    }

    // FIXME: CasingEjector seems to be broken so this doesn't work for now
    @Override
    protected Vec3d getCasingSpawnPos() {
        Vec3d pos = this.getTurretPos();
        Vec3d vec = new Vec3d(-2, 0, 0);
        Vec3dUtil.rotateRoll(vec, (float) -this.rotationPitch);
        vec.rotateYaw((float) -(this.rotationYaw + Math.PI * 0.5));
        return pos.add(vec);
    }

    @Override
    protected void spawnCasing() {

        if (cachedCasingConfig == null) return;

        Vec3d spawn = this.getCasingSpawnPos();
        float yaw = (float) Math.toDegrees(rotationYaw);
        float pitch = (float) -Math.toDegrees(this.rotationPitch);

        CasingCreator.composeEffect(world,
                spawn,
                yaw, pitch,
                -0.2, -0.2, 0,
                0.01, -5, 0,
                cachedCasingConfig.getName(),
                true, 100, 0.5, 20);

        cachedCasingConfig = null;
    }

    @Override
    protected CasingEjector getEjector() {
        return ejector;
    }

    @Override
    public boolean usesCasings() {
        return true;
    }

    @Override
    public int casingDelay() {
        return 22;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerTurretBase(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUITurretJeremy(player.inventory, this);
    }


}
