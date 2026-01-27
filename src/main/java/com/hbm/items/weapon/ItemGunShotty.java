package com.hbm.items.weapon;

import com.hbm.handler.GunConfiguration;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.util.ShadyUtil;
import com.hbm.packet.toclient.GunAnimationPacket;
import com.hbm.packet.toclient.MeathookResetStrafePacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.SetGunAnimPacket;
import com.hbm.render.anim.HbmAnimations.AnimType;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Spaghetti("This is broken as fuck")
public class ItemGunShotty extends ItemGunBase {
	
	//The 
	public static Vec3d rayTrace = null;
	@SideOnly(Side.CLIENT)
	public static Vec2f screenPos;
	@SideOnly(Side.CLIENT)
	public static Vec2f prevScreenPos;
	//For swinging left or right with the meathook
	public static float motionStrafe;
	
	public ItemGunShotty(GunConfiguration config, String s) {
		super(config, s);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void startActionClient(ItemStack stack, World world, EntityPlayer player, boolean main, EnumHand hand) {
		if(mainConfig.firingMode == GunConfiguration.FIRE_MANUAL && m1 && tryShoot(stack, world, player, main)){
			long time = System.currentTimeMillis();
			float mult = player.getUniqueID().equals(ShadyUtil.Dr_Nostalgia) ? 10 : 1;
			NBTTagCompound anim = new NBTTagCompound();
			anim.setLong("time", time);
			anim.setInteger("id", 0);
			anim.setFloat("mult", mult);
			if(stack.getTagCompound() == null)
				stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setTag("animation", anim);
			PacketDispatcher.wrapper.sendToServer(new SetGunAnimPacket(time, 0, mult, player.getHeldItemMainhand() == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND));
		}
		super.startActionClient(stack, world, player, main, hand);
	}
	
	@Override
	protected void spawnProjectile(World world, EntityPlayer player, ItemStack stack, int config, EnumHand hand) {
		setHookedEntity(player, stack, null);
		super.spawnProjectile(world, player, stack, config, hand);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
			ItemStack stack = playerIn.getHeldItem(handIn);
			RayTraceResult ray = Library.rayTraceEntitiesInCone(playerIn, 30, 1, 6);
			if(!worldIn.isRemote && ray != null && ray.typeOfHit == Type.ENTITY && !hasHookedEntity(worldIn, stack)){
				Entity ent = ray.entityHit;
				setHookedEntity(playerIn, stack, ent);
				setTimeout(stack, 30);
				
				/*Vec3d toEnt = ent.getPositionVector().add(0, ent.getEyeHeight()*0.75, 0).subtract(playerIn.getPositionEyes(1.0F)).normalize();
				double yaw = Math.toDegrees(Math.atan2(toEnt.x, toEnt.z));
				double sqrt = MathHelper.sqrt(toEnt.x * toEnt.x + toEnt.z * toEnt.z);
				double pitch = Math.toDegrees(Math.atan2(toEnt.y, sqrt));
				float diffYaw = (float) (-yaw - playerIn.rotationYaw%360)%360;
				if(diffYaw < -180)
					diffYaw += 360;
				if(diffYaw > 180)
					diffYaw -= 360;
				playerIn.rotationYaw +=diffYaw;
				playerIn.rotationPitch = (float) -pitch;*/
				
				PacketDispatcher.sendTo(new GunAnimationPacket(AnimType.ALT_CYCLE.ordinal(), handIn), (EntityPlayerMP) playerIn);
			}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	protected void updateServer(ItemStack stack, World world, EntityPlayer player, int slot, EnumHand hand) {
		super.updateServer(stack, world, player, slot, hand);
		int timeout = getTimeout(stack);
		if(timeout > 0)
			setTimeout(stack, timeout-1);
		
		if(hasHookedEntity(world, stack)){
			player.fallDistance = 0;
			Entity ent = getHookedEntity(world, stack);
			Vec3d entPos = ent.getPositionVector().add(0, ent.getEyeHeight()*0.75, 0);
			Vec3d playerPos = player.getPositionEyes(1);
			double toEnt = entPos.subtract(playerPos).lengthSquared();
			if(toEnt < 16 || Library.isObstructed(world, playerPos.x, playerPos.y, playerPos.z, entPos.x, entPos.y, entPos.z))
				setHookedEntity(player, stack, null);
		}
		
		if(player.getUniqueID().equals(ShadyUtil.Dr_Nostalgia) && getDelay(stack) < this.mainConfig.rateOfFire * 0.9){
			setDelay(stack, 0);
		}
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void updateClient(ItemStack stack, World world, EntityPlayer player, int slot, EnumHand hand) {
		if (screenPos == null) screenPos = new Vec2f(0, 0);
		prevScreenPos = screenPos;
		RayTraceResult ray = Library.rayTraceEntitiesInCone(player, 30, 1, 6);
		if(ray != null && ray.typeOfHit == Type.ENTITY){
			rayTrace = ray.entityHit.getPositionVector().add(0, ray.entityHit.getEyeHeight()*0.75, 0);
		} else {
			rayTrace = null;
		}
		
		if(hasHookedEntity(world, stack)){
			Entity ent = getHookedEntity(world, stack);
			Vec3d toEnt = ent.getPositionVector().add(0, ent.getEyeHeight()*0.75, 0).subtract(player.getPositionEyes(1.0F)).normalize().scale(1.3);
			motionStrafe *= 0.9;
			//Cross product returns a vector perpendicular to the two supplied vectors.
			//In this case, it gives us the tangent vector to the circle we want to travel around to get a nice swing.
			Vec3d cross = toEnt.crossProduct(new Vec3d(0, 1, 0)).scale(-motionStrafe);
			player.motionX = toEnt.x + cross.x;
			player.motionY = toEnt.y + 0.05;
			player.motionZ = toEnt.z + cross.z;
			
			if(rayTrace != null){
				//Measures the difference in angle between the vector from player to hooked entity and the vector from player+motion to hooked entity,
				//then adds the difference to the player's rotation. Only do this when the player is actually tracking the entity.
				//This essentially acts as aim assist so you don't have to track the entity manually.
				toEnt = toEnt.normalize();
				Vec3d newToEnt = ent.getPositionVector().add(player.motionX, ent.getEyeHeight()*0.75+player.motionY, player.motionZ).subtract(player.getPositionEyes(1.0F)).normalize();
				Vec3d toEntAngle = Library.getEuler(toEnt);
				Vec3d toEntAngle2 = Library.getEuler(newToEnt);
				Vec3d diff = toEntAngle2.subtract(toEntAngle);
				if(diff.x > 180){
					diff = diff.subtract(360, 0, 0);
				} else if(diff.x < -180){
					diff = diff.add(360, 0, 0);
				}
				player.rotationYaw += diff.x;
				player.rotationPitch += diff.y;
				//These have to be increased, too, so the held item doesn't jitter weirdly.
				((EntityPlayerSP)player).renderArmYaw += diff.x;
				((EntityPlayerSP)player).renderArmPitch += diff.y;
			}
			
			player.motionX += ent.motionX;
			player.motionY += ent.motionY;
			player.motionZ += ent.motionZ;
			
			player.isAirBorne = true;
			player.onGround = false;
		}
		
		super.updateClient(stack, world, player, slot, hand);
	}
	
	public static void setHookedEntity(EntityPlayer p, ItemStack stack, Entity e){
		if(!stack.hasTagCompound()){
			stack.setTagCompound(new NBTTagCompound());
		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
			PacketDispatcher.sendTo(new MeathookResetStrafePacket(), (EntityPlayerMP) p);
		}
		if(e == null){
			stack.getTagCompound().removeTag("hooked_entity");
		} else {
			stack.getTagCompound().setInteger("hooked_entity", e.getEntityId());
		}
	}
	
	public static boolean hasHookedEntity(World w, ItemStack stack){
		if(stack == null || stack.getItem() != ModItems.gun_supershotgun)
			return false;
		int timeout = getTimeout(stack);
		return timeout > 0 && getHookedEntity(w, stack) != null;	
	}
	
	public static Entity getHookedEntity(World world, ItemStack stack){
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("hooked_entity")){
			return world.getEntityByID(stack.getTagCompound().getInteger("hooked_entity"));
		}
		return null;
	}
	
	public static int getTimeout(ItemStack stack){
		if(!stack.hasTagCompound()){
			stack.setTagCompound(new NBTTagCompound());
		}
		return stack.getTagCompound().getInteger("meathook_timeout");
	}
	
	public static void setTimeout(ItemStack stack, int timeout){
		if(!stack.hasTagCompound()){
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("meathook_timeout", timeout);
	}
}
