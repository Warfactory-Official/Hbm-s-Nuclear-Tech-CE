package com.hbm.items.weapon;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.IEquipReceiver;
import com.hbm.items.tool.ItemSwordAbility;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.toserver.PacketMobSlicer;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.anim.HbmAnimations.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ItemSwordCutter extends ItemSwordAbility implements IEquipReceiver {

	//All of this stuff is client side, only for first person rendering
	//This whole system is a mess. Bruh.
	public static Vec3d startPos;
	public static boolean clicked;
	public static boolean canClick = true;
	public static Vec3d planeNormal;
	public static double prevAngle = 0;
	public static float yaw, pitch;
	public static final float MAX_DYAW = 120;
	public static final float MAX_DPITCH = 120;
	
	public ItemSwordCutter(float damage, double movement, ToolMaterial material, String s) {
		super(damage, movement, material, s, false);
	}

	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, @NotNull EntityPlayer playerIn, @NotNull EnumHand handIn) {
		rightClickClient(worldIn, playerIn);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@SideOnly(Side.CLIENT)
	public void rightClickClient(World worldIn, EntityPlayer playerIn){
		if(worldIn.isRemote && playerIn == Minecraft.getMinecraft().player && !clicked && canClick){
			startPos = playerIn.getLookVec();
			planeNormal = null;
			clicked = true;
			canClick = false;
			yaw = playerIn.rotationYaw;
			pitch = playerIn.rotationPitch;
		}
	}
	
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, @NotNull ItemStack stack) {
		if(entityLiving.world.isRemote){
			swingClient(entityLiving, stack);
		}
		return super.onEntitySwing(entityLiving, stack);
	}
	
	@Override
	public boolean onLeftClickEntity(@NotNull ItemStack stack, EntityPlayer player, @NotNull Entity entity) {
		if(stack == player.getHeldItemMainhand()){
			Animation a = HbmAnimations.getRelevantAnim(EnumHand.MAIN_HAND);
			if(a != null && a.animation != null){
				return true;
			}
		}
		
		return super.onLeftClickEntity(stack, player, entity);
	}
	
	@SideOnly(Side.CLIENT)
	public void swingClient(EntityLivingBase entityLiving, ItemStack stack){
		if(clicked && planeNormal != null && entityLiving instanceof EntityPlayer){
			PacketDispatcher.wrapper.sendToServer(new PacketMobSlicer(startPos.add(entityLiving.getPositionEyes(1)), planeNormal, getTexId()));
			planeNormal = null;
			clicked = false;
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "anim");
			nbt.setInteger("hand", EnumHand.MAIN_HAND.ordinal());
			nbt.setString("mode", "swing");
			nbt.setString("name", stack.getItem().getRegistryName().getPath());
			MainRegistry.proxy.effectNT(nbt);
		}
	}
	
	public byte getTexId(){
		return 0;
	}
	
	@Override
	public void onEquip(EntityPlayer player, EnumHand hand) {
		if(!(player instanceof EntityPlayerMP))
			return;
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("type", "anim");
		nbt.setInteger("hand", hand.ordinal());
		nbt.setString("mode", "equip");
		nbt.setString("name", this.getRegistryName().getPath());
		PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0), (EntityPlayerMP)player);
	}

}
