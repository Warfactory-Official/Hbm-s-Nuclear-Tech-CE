package com.hbm.tileentity.deco;

import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@AutoRegister
public class TileEntityLantern extends TileEntity implements ITickable {

    @Override
    public void update() {

        if(!world.isRemote && world.getTotalWorldTime() % 20 == 0) {

            List<EntityGlyphid> glyphids = world.getEntitiesWithinAABB(EntityGlyphid.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 5.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 5.5, pos.getZ() + 0.5).expand(7.5, 7.5, 7.5));

            for(EntityGlyphid glyphid : glyphids) {
                glyphid.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("blindness")), 100, 0));
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    pos.getX() + 1,
                    pos.getY() + 6,
                    pos.getZ() + 1
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
