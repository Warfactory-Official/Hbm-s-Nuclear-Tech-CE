package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Vec3NT;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemColtanCompass extends Item {

	public int lastX = 0;
	public int lastZ = 0;
	public long lease = 0;
	
	public ItemColtanCompass(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter(){

			@SideOnly(Side.CLIENT)
            double rotation;
            @SideOnly(Side.CLIENT)
            double rota;
            @SideOnly(Side.CLIENT)
            long lastUpdateTick;
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                if (entityIn == null && !stack.isOnItemFrame())
                {
                    return 0.0F;
                }
                else
                {
                    boolean flag = entityIn != null;
                    Entity entity = flag ? entityIn : stack.getItemFrame();

                    if (worldIn == null)
                    {
                        worldIn = entity.world;
                    }

                    double d0;

                    if (worldIn.provider.isSurfaceWorld())
                    {
                        double d1 = flag ? (double)entity.rotationYaw : this.getFrameRotation((EntityItemFrame)entity);
                        d1 = MathHelper.positiveModulo(d1 / 360.0D, 1.0D);
                        double d2 = this.getColtToAngle(worldIn, entity) / (Math.PI * 2D);
                        d0 = 0.5D - (d1 - 0.25D - d2);
                    }
                    else
                    {
                        d0 = Math.random();
                    }

                    if (flag)
                    {
                        d0 = this.wobble(worldIn, d0);
                    }

                    return MathHelper.positiveModulo((float)d0, 1.0F);
                }
            }
            @SideOnly(Side.CLIENT)
            private double wobble(World worldIn, double p_185093_2_)
            {
                if (worldIn.getTotalWorldTime() != this.lastUpdateTick)
                {
                    this.lastUpdateTick = worldIn.getTotalWorldTime();
                    double d0 = p_185093_2_ - this.rotation;
                    d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
                    this.rota += d0 * 0.1D;
                    this.rota *= 0.8D;
                    this.rotation = MathHelper.positiveModulo(this.rotation + this.rota, 1.0D);
                }

                return this.rotation;
            }
            @SideOnly(Side.CLIENT)
            private double getFrameRotation(EntityItemFrame p_185094_1_)
            {
                return MathHelper.wrapDegrees(180 + p_185094_1_.facingDirection.getHorizontalIndex() * 90);
            }
            @SideOnly(Side.CLIENT)
            private double getColtToAngle(World p_185092_1_, Entity p_185092_2_)
            {
            	if(ItemColtanCompass.this.lease < System.currentTimeMillis()){
            		return Math.random() * Math.PI * 2.0D;
            	}
            	double d4 = ItemColtanCompass.this.lastX;
				double d5 = ItemColtanCompass.this.lastZ;
                return Math.atan2(d5 - p_185092_2_.posZ, d4 - p_185092_2_.posX);
            }
			
		});
		
		ModItems.ALL_ITEMS.add(this);
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        list.add("Points towards the coltan deposit.");
        list.add("The deposit is a large area where coltan ore spawns like standard ore,");
        list.add("it's not one large blob of ore on that exact location.");
    }
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected){
		if(world.isRemote) {
			if(stack.hasTagCompound()) {
				lastX = stack.getTagCompound().getInteger("colX");
				lastZ = stack.getTagCompound().getInteger("colZ");
				lease = System.currentTimeMillis() + 1000;
				
				Vec3NT vec = Vec3NT.createVectorHelper(entity.posX - lastX, 0, entity.posZ - lastZ);
				MainRegistry.proxy.displayTooltip(((int) vec.length()) + "m");
			}
			
			if(ItemColtanCompass.this.lease < System.currentTimeMillis()) {
				lastX = 0;
				lastZ = 0;
			}
			
		} else {
			if(!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());

				Random colRand = new Random(world.getSeed() + 5);
				int colX = (int) (colRand.nextGaussian() * 1500);
				int colZ = (int) (colRand.nextGaussian() * 1500);

				stack.getTagCompound().setInteger("colX", colX);
				stack.getTagCompound().setInteger("colZ", colZ);
			}
		}
	}
}
