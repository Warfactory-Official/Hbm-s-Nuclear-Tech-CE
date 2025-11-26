	package com.hbm.items.special.weapon;

    import com.google.common.collect.Multimap;
    import com.hbm.entity.effect.EntityCloudFleijaRainbow;
    import com.hbm.entity.logic.EntityNukeExplosionMK3;
    import com.hbm.entity.projectile.EntityExplosiveBeam;
    import com.hbm.interfaces.IHasCustomModel;
    import com.hbm.items.ModItems;
    import com.hbm.lib.HBMSoundHandler;
    import com.hbm.main.MainRegistry;
    import net.minecraft.client.renderer.block.model.ModelResourceLocation;
    import net.minecraft.client.renderer.texture.TextureMap;
    import net.minecraft.client.util.ITooltipFlag;
    import net.minecraft.entity.Entity;
    import net.minecraft.entity.EntityLivingBase;
    import net.minecraft.entity.ai.attributes.AttributeModifier;
    import net.minecraft.entity.player.EntityPlayer;
    import net.minecraft.init.SoundEvents;
    import net.minecraft.inventory.EntityEquipmentSlot;
    import net.minecraft.item.EnumAction;
    import net.minecraft.item.EnumRarity;
    import net.minecraft.item.Item;
    import net.minecraft.item.ItemStack;
    import net.minecraft.nbt.NBTTagCompound;
    import net.minecraft.util.ActionResult;
    import net.minecraft.util.EnumHand;
    import net.minecraft.util.SoundCategory;
    import net.minecraft.util.text.TextFormatting;
    import net.minecraft.world.World;
    import net.minecraftforge.client.event.ModelBakeEvent;
    import net.minecraftforge.common.MinecraftForge;
    import net.minecraftforge.event.entity.player.ArrowLooseEvent;

    import java.util.List;
    import java.util.Random;

public class GunB92 extends Item implements IHasCustomModel {

	public static final ModelResourceLocation b92Model = new ModelResourceLocation("hbm:gun_b92", "inventory");

	Random rand = new Random();

	public int dmgMin = 16;
	public int dmgMax = 28;

	public GunB92(String s) {

		this.maxStackSize = 1;
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.weaponTab);
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
		if (entityLiving.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) == stack && !entityLiving.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).isEmpty() && entityLiving.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).getItem() == ModItems.gun_b92) {
			entityLiving.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).onPlayerStoppedUsing(worldIn, entityLiving, timeLeft);
		}
		if (!entityLiving.isSneaking()) {
			int j = this.getMaxItemUseDuration(stack) - timeLeft;
			if (entityLiving instanceof EntityPlayer) {
				ArrowLooseEvent evt = new ArrowLooseEvent((EntityPlayer) entityLiving, stack, worldIn, j, false);
				MinecraftForge.EVENT_BUS.post(evt);
				j = evt.getCharge();
			}

			boolean flag = true;

			if (flag) {
				float f = j / 20.0F;
				f = (f * f + f * 2.0F) / 3.0F;

				if (j < 10.0D) {
					return;
				}

				if (j > 10.0F) {
					f = 10.0F;
				}
				if (!worldIn.isRemote)
					for (int i = 0; i < getPower(stack); i++) {
						EntityExplosiveBeam entityarrow1;
						entityarrow1 = new EntityExplosiveBeam(worldIn, entityLiving, 3.0F, entityLiving.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND) == stack);
						float divergence = i * 0.2F;

						if (divergence > 1F)
							divergence = 1F;

						if (i > 0) {
							entityarrow1.motionX += rand.nextGaussian() * divergence;
							entityarrow1.motionY += rand.nextGaussian() * divergence;
							entityarrow1.motionZ += rand.nextGaussian() * divergence;
						}

						stack.damageItem(1, entityLiving);

						worldIn.spawnEntity(entityarrow1);
					}

				worldIn.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, HBMSoundHandler.sparkShoot, SoundCategory.AMBIENT, 5.0F, 1.0F);
				// Well that was a failure. Maybe I'll make it work one day
				// if(worldIn.isRemote)
				// ItemRenderGunAnim.b92Ani.start();
				setAnim(stack, 1);
				setPower(stack, 0);
			}
		} else {
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {

		int j = getAnim(stack);
		if (j > 0) {
			{
				if (j < 30) {
					setAnim(stack, j + 1);

				} else
					setAnim(stack, 0);

				if (j == 15) {
					world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.b92Reload, SoundCategory.AMBIENT, 2.0F, 0.9F);
					setPower(stack, getPower(stack) + 1);

					if (getPower(stack) > 10) {

						setPower(stack, 0);

						if (!world.isRemote) {
							world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 100.0f, world.rand.nextFloat() * 0.1F + 0.9F);

							EntityNukeExplosionMK3 exp = new EntityNukeExplosionMK3(world);
							exp.posX = entity.posX;
							exp.posY = entity.posY;
							exp.posZ = entity.posZ;
							if(!EntityNukeExplosionMK3.isJammed(world, exp)){
								
								exp.destructionRange = 50;
								exp.speed = 25;
								exp.coefficient = 1.0F;
								exp.waste = false;

								world.spawnEntity(exp);

								EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(world, 50);
								cloud.posX = entity.posX;
								cloud.posY = entity.posY;
								cloud.posZ = entity.posZ;
								world.spawnEntity(cloud);
							}
						}
					}
				}
			}

		}

	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_) {
		return 72000;
	}

	/**
	 * returns the action that specifies what animation to play when the items
	 * is being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_) {
		return EnumAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!playerIn.isSneaking() && getPower(stack) > 0) {

			if (GunB92.getAnim(stack) == 0) {
				playerIn.setActiveHand(handIn);
			}

		} else {
			if (getAnim(stack) == 0) {
				setAnim(stack, 1);
			}
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	/**
	 * Return the enchantability factor of the item, most of the time is based
	 * on material.
	 */
	@Override
	public int getItemEnchantability() {
		return 1;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (MainRegistry.polaroidID == 11) {
			list.add("A weapon that came from the stars.");
			list.add("It screams for murder.");
		} else if (MainRegistry.polaroidID == 18) {
			list.add("One could turn the gun into a bomb");
			list.add("by overloading the capacitors...");
		} else {
			list.add("Stay away from me compootur!");
		}
		list.add("");
		list.add("Projectiles explode on impact.");
		list.add("Sneak while holding the right mouse button");
		list.add("to charge additional energy.");
		list.add("The more energy is stored, the less accurate");
		list.add("the beams become.");
		list.add("Only up to ten charges may be stored.");
		list.add("");
		list.add("\"It's nerf or nothing!\"");
		list.add("");
		list.add("§d§l[LEGENDARY WEAPON]");

		// Yeah attribute modifiers don't work too well for this. Not sure why
		// this even needs to be here, but oh well.
		list.add(TextFormatting.BLUE + "+3.5 Attack Damage");
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		// multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new
		// AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 3.5,
		// 0));
		return multimap;
	}

	private static int getAnim(ItemStack stack) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("animation");

	}

	private static void setAnim(ItemStack stack, int i) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		stack.getTagCompound().setInteger("animation", i);

	}

	private static int getPower(ItemStack stack) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("energy");

	}

	private static void setPower(ItemStack stack, int i) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		stack.getTagCompound().setInteger("energy", i);

	}

	public static float getRotationFromAnim(ItemStack stack, float partialTicks) {
		float rad = 0.0174533F;
		rad *= 7.5F;
		int i = getAnim(stack);
		// i+=partialTicks;
		if (i < 10)
			return 0;
		i -= 10;

		if (i < 6)
			return rad * (i - 1 + partialTicks);
		if (i > 14)
			return rad * (5 - (i + partialTicks - 15));
		return rad * 5;
	}

	public static float getOffsetFromAnim(ItemStack stack, float partialTicks) {
		float i = getAnim(stack);
		if (i < 10)
			return 0;
		i -= 10;

		if (i < 10)
			return (i + partialTicks) / 10;
		else
			return 2 - ((i + partialTicks) / 10);
	}

	public static float getTransFromAnim(ItemStack stack, float partialTicks) {
		float i = getAnim(stack);
		if (i < 10)
			return 0;
		i -= 10;

		if (i > 4 && i < 10)
			return (i + partialTicks - 5) * 0.05F;

		if (i > 9 && i < 15)
			return (10 * 0.05F) - ((i + partialTicks - 5) * 0.05F);

		return 0;
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {

		return EnumRarity.UNCOMMON;
	}

	@Override
	public ModelResourceLocation getResourceLocation() {
		return b92Model;
	}

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerSprite(TextureMap map) {

    }
}