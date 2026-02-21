package com.hbm.items.weapon;

import com.hbm.config.BombConfig;
import com.hbm.entity.grenade.*;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemGrenade extends ItemBakedBase {
	
	protected int fuse = 4;
	
	public ItemGrenade(int fuse, String s) {
		super(s);
		this.maxStackSize = 16;
		this.fuse = fuse;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (!playerIn.capabilities.isCreativeMode) {
			stack.shrink(1);;
		}

		worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		/*
		 * kill all this stupid bullshit
		 * make a PROPER grenade entity base class
		 * have all the grenade items be an NBT stat in the entity instead of having new entities for every fucking grenade type
		 * register explosion effects with some lambdas to save on LOC
		 * jesus christ why do i keep doing this
		 */
		// mlbv: I totally agree
		if (!worldIn.isRemote) {
			if (this == ModItems.grenade_generic) {
				worldIn.spawnEntity(new EntityGrenadeGeneric(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_strong) {
				worldIn.spawnEntity(new EntityGrenadeStrong(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_frag) {
				EntityGrenadeFrag frag = new EntityGrenadeFrag(worldIn, playerIn, handIn);
				frag.shooter = playerIn;
				worldIn.spawnEntity(frag);
			}
			if (this == ModItems.grenade_fire) {
				EntityGrenadeFire fire = new EntityGrenadeFire(worldIn, playerIn, handIn);
				fire.shooter = playerIn;
				worldIn.spawnEntity(fire);
			}
			if (this == ModItems.grenade_cluster) {
				worldIn.spawnEntity(new EntityGrenadeCluster(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_flare) {
				worldIn.spawnEntity(new EntityGrenadeFlare(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_electric) {
				worldIn.spawnEntity(new EntityGrenadeElectric(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_poison) {
				worldIn.spawnEntity(new EntityGrenadePoison(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_gas) {
				worldIn.spawnEntity(new EntityGrenadeGas(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_schrabidium) {
				worldIn.spawnEntity(new EntityGrenadeSchrabidium(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_nuke) {
				worldIn.spawnEntity(new EntityGrenadeNuke(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_nuclear) {
				worldIn.spawnEntity(new EntityGrenadeNuclear(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_solinium) {
				worldIn.spawnEntity(new EntityGrenadeSolinium(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_pulse) {
				worldIn.spawnEntity(new EntityGrenadePulse(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_plasma) {
				worldIn.spawnEntity(new EntityGrenadePlasma(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_tau) {
				worldIn.spawnEntity(new EntityGrenadeTau(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_lemon) {
				worldIn.spawnEntity(new EntityGrenadeLemon(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_mk2) {
				worldIn.spawnEntity(new EntityGrenadeMk2(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_aschrab) {
				worldIn.spawnEntity(new EntityGrenadeASchrab(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_zomg) {
				worldIn.spawnEntity(new EntityGrenadeZOMG(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_shrapnel) {
				worldIn.spawnEntity(new EntityGrenadeShrapnel(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_black_hole) {
				worldIn.spawnEntity(new EntityGrenadeBlackHole(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_gascan) {
				worldIn.spawnEntity(new EntityGrenadeGascan(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_cloud) {
				worldIn.spawnEntity(new EntityGrenadeCloud(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_pink_cloud) {
				worldIn.spawnEntity(new EntityGrenadePC(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_smart) {
				worldIn.spawnEntity(new EntityGrenadeSmart(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_mirv) {
				worldIn.spawnEntity(new EntityGrenadeMIRV(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_breach) {
				worldIn.spawnEntity(new EntityGrenadeBreach(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_burst) {
				worldIn.spawnEntity(new EntityGrenadeBurst(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_generic) {
				worldIn.spawnEntity(new EntityGrenadeIFGeneric(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_he) {
				worldIn.spawnEntity(new EntityGrenadeIFHE(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_bouncy) {
				worldIn.spawnEntity(new EntityGrenadeIFBouncy(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_sticky) {
				worldIn.spawnEntity(new EntityGrenadeIFSticky(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_impact) {
				worldIn.spawnEntity(new EntityGrenadeIFImpact(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_incendiary) {
				worldIn.spawnEntity(new EntityGrenadeIFIncendiary(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_toxic) {
				worldIn.spawnEntity(new EntityGrenadeIFToxic(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_concussion) {
				worldIn.spawnEntity(new EntityGrenadeIFConcussion(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_brimstone) {
				worldIn.spawnEntity(new EntityGrenadeIFBrimstone(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_mystery) {
				worldIn.spawnEntity(new EntityGrenadeIFMystery(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_spark) {
				worldIn.spawnEntity(new EntityGrenadeIFSpark(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_hopwire) {
				worldIn.spawnEntity(new EntityGrenadeIFHopwire(worldIn, playerIn, handIn));
			}
			if (this == ModItems.grenade_if_null) {
				worldIn.spawnEntity(new EntityGrenadeIFNull(worldIn, playerIn, handIn));
			}
//			if (this == ModItems.nuclear_waste_pearl) {
//				p_77659_2_.spawnEntityInWorld(new EntityWastePearl(p_77659_2_, p_77659_3_));
//			}
			if (this == ModItems.stick_dynamite) {
				worldIn.spawnEntity(new EntityGrenadeDynamite(worldIn, playerIn, handIn));
			}
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack) {
		if (this == ModItems.grenade_schrabidium || this == ModItems.grenade_aschrab || this == ModItems.grenade_cloud) {
			return EnumRarity.RARE;
		}

		if (this == ModItems.grenade_plasma || this == ModItems.grenade_zomg || this == ModItems.grenade_black_hole || this == ModItems.grenade_pink_cloud || this == ModItems.grenade_solinium) {
			return EnumRarity.EPIC;
		}

		if (this == ModItems.grenade_nuke || this == ModItems.grenade_nuclear || this == ModItems.grenade_tau || this == ModItems.grenade_lemon || this == ModItems.grenade_mk2 || this == ModItems.grenade_pulse || this == ModItems.grenade_gascan) {
			return EnumRarity.UNCOMMON;
		}

		return EnumRarity.COMMON;
	}
	
	private String translateFuse() {
		if(fuse == -1)
			return "Impact";
		
		if(fuse == 0)
			return "Instant";
		
		return fuse + "s";
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add("Fuse: " + translateFuse());

		if (this == ModItems.grenade_if_generic) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"How do you like " + TextFormatting.RESET + TextFormatting.GRAY + "them" + TextFormatting.ITALIC + " apples?\"");
		}
		if (this == ModItems.grenade_if_he) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"You better run, you better take cover!\"");
		}
		if (this == ModItems.grenade_if_bouncy) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Boing!\"");
		}
		if (this == ModItems.grenade_if_sticky) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"This one is the booger grenade.\"");
		}
		if (this == ModItems.grenade_if_impact) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Tossable boom.\"");
		}
		if (this == ModItems.grenade_if_incendiary) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Flaming wheel of destruction!\"");
		}
		if (this == ModItems.grenade_if_toxic) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"TOXIC SHOCK\"");
		}
		if (this == ModItems.grenade_if_concussion) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Oof ouch owie, my bones!\"");
		}
		if (this == ModItems.grenade_if_brimstone) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Zoop!\"");
		}
		if (this == ModItems.grenade_if_mystery) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"It's a mystery!\"");
		}
		if (this == ModItems.grenade_if_spark) {
			list.add("");
			//list.add(TextFormatting.ITALIC + "\"31-31-31-31-31-31-31-31-31-31-31-31-31\"");
			list.add(TextFormatting.ITALIC + "\"We can't rewind, we've gone too far.\"");
		}
		if (this == ModItems.grenade_if_hopwire) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"All I ever wished for was a bike that didn't fall over.\"");
		}
		if (this == ModItems.grenade_if_null) {
			list.add("");
			list.add(TextFormatting.ITALIC + "java.lang.NullPointerException");
		}
		if (this == ModItems.grenade_smart) {
			list.add("");
			list.add("\"Why did it not blow up????\"");
			list.add(TextFormatting.ITALIC + "If it didn't blow up it means it worked.");
		}
		if (this == ModItems.grenade_solinium) {
			list.add("§3[Solinium Grenade]§r");
			list.add(" §eRadius: "+(int)BombConfig.soliniumRadius/10+"m§r");
		}
		if (this == ModItems.grenade_nuclear) {
			list.add("§2[Nuclear Grenade]§r");
			list.add(" §eRadius: "+(int)BombConfig.fatmanRadius/2+"m§r");
			list.add("§2[Fallout]§r");
			list.add(" §aRadius: "+(int)(BombConfig.fatmanRadius/2*(1+BombConfig.falloutRange/100))+"m§r");
		}
	}
	
	public static int getFuseTicks(Item grenade) {
		return ((ItemGrenade)grenade).fuse * 20;
	}
}
