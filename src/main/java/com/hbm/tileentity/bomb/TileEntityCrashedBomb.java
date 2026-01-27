package com.hbm.tileentity.bomb;

import com.hbm.blocks.bomb.BlockCrashedBomb.EnumDudType;
import com.hbm.interfaces.AutoRegister;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.EnumUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.function.BiConsumer;

@AutoRegister
public class TileEntityCrashedBomb extends TileEntity implements ITickable {

    @Override
    public void update() {

        if (!world.isRemote) {

            if (world.getTotalWorldTime() % 2 == 0) {
                EnumDudType type = EnumUtil.grabEnumSafely(EnumDudType.VALUES, this.getBlockMetadata());

                switch (type) {
                    case BALEFIRE -> affectEntities(
                            (entity, intensity) -> ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE,
                                    1F * intensity), 15D);
                    case NUKE -> affectEntities(
                            (entity, intensity) -> ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE,
                                    0.25F * intensity), 10D);
                    case SALTED -> affectEntities(
                            (entity, intensity) -> ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE,
                                    0.5F * intensity), 10D);
                }
            }
        }
    }

    public void affectEntities(BiConsumer<EntityLivingBase, Float> effect, double range) {
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class,
                new AxisAlignedBB(pos.add(0.5, 0.5, 0.5), pos.add(0.5, 0.5, 0.5)).grow(range, range, range));
        for (EntityLivingBase entity : list) {
            double dist = Math.sqrt(getDistanceSq(entity.posX, entity.posY + entity.height / 2, entity.posZ));
            if (dist > range) continue;
            float intensity = (float) (1D - dist / range);
            effect.accept(entity, intensity);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}