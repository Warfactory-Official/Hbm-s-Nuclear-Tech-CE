package com.hbm.entity.cart;

import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import com.hbm.render.entity.item.RenderNeoCart;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public abstract class EntityMinecartNTM extends EntityMinecart {

    protected static final DataParameter<Integer> CART_BASE = EntityDataManager.createKey(EntityMinecartNTM.class, DataSerializers.VARINT);

    public EntityMinecartNTM(World world) {
        super(world);
    }

    public EntityMinecartNTM(World world, double x, double y, double z, EnumCartBase type) {
        super(world, x, y, z);
        this.setBase(type);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CART_BASE, 0);
    }

    public void setBase(EnumCartBase type) {
        this.dataManager.set(CART_BASE, type.ordinal());
    }

    public EnumCartBase getBase() {
        return EnumCartBase.values()[this.dataManager.get(CART_BASE)];
    }

    @Override
    public @NotNull Type getType() {
        return Type.RIDEABLE;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBox(@NotNull Entity entity) {
        return entity.getEntityBoundingBox();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public void killMinecart(@NotNull DamageSource source) {
        this.setDead();
        ItemStack itemstack = getCartItem();

        if(this.hasCustomName()) {
            itemstack.setStackDisplayName(this.getCustomNameTag());
        }

        this.entityDropItem(itemstack, 0.0F);
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("base", this.dataManager.get(CART_BASE));
    }

    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.dataManager.set(CART_BASE, nbt.getInteger("base"));
    }

    @SideOnly(Side.CLIENT)
    public void renderSpecialContent(RenderNeoCart renderer) { }
}
