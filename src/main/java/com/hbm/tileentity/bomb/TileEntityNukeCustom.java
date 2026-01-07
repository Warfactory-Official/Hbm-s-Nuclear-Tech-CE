package com.hbm.tileentity.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.container.ContainerNukeCustom;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUINukeCustom;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemCell;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.UUID;

@AutoRegister
public class TileEntityNukeCustom extends TileEntity implements ITickable, IGUIProvider {

	public ItemStackHandler inventory;
	private String customName;
	public UUID placerID;
	
	public TileEntityNukeCustom() {
		inventory = new ItemStackHandler(27){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.nukeCustom";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}
	
	public void setCustomName(String name) {
		this.customName = name;
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	public static HashMap<ComparableStack, CustomNukeEntry> entries = new HashMap<>();

	public static void registerBombItems() {
		//TNT
        entries.put(new ComparableStack(Items.GUNPOWDER), new CustomNukeEntry(EnumBombType.TNT, 0.8F));
        entries.put(new ComparableStack(Blocks.TNT), new CustomNukeEntry(EnumBombType.TNT, 4F));
        entries.put(new ComparableStack(ModBlocks.det_cord), new CustomNukeEntry(EnumBombType.TNT, 1.5F));
        entries.put(new ComparableStack(ModItems.ingot_semtex), new CustomNukeEntry(EnumBombType.TNT, 8F));
        entries.put(new ComparableStack(ModItems.ingot_c4), new CustomNukeEntry(EnumBombType.TNT, 10F));
        entries.put(new ComparableStack(ModItems.ball_dynamite), new CustomNukeEntry(EnumBombType.TNT, 4F));
        entries.put(new ComparableStack(ModItems.ball_tnt), new CustomNukeEntry(EnumBombType.TNT, 6F));
        entries.put(new ComparableStack(ModBlocks.det_charge), new CustomNukeEntry(EnumBombType.TNT, 15F));
        entries.put(new ComparableStack(ModItems.canister_full, 1, Fluids.DIESEL.getID()), new CustomNukeEntry(EnumBombType.TNT, 0.5F));
        entries.put(new ComparableStack(ModItems.canister_napalm), new CustomNukeEntry(EnumBombType.TNT, 2.5F));
        entries.put(new ComparableStack(ModItems.canister_full, 1, Fluids.KEROSENE.getID()), new CustomNukeEntry(EnumBombType.TNT, 0.8F));
        entries.put(new ComparableStack(ModBlocks.red_barrel), new CustomNukeEntry(EnumBombType.TNT, 2.5F));
        entries.put(new ComparableStack(ModBlocks.pink_barrel), new CustomNukeEntry(EnumBombType.TNT, 4F));
        entries.put(new ComparableStack(ModItems.custom_tnt), new CustomNukeEntry(EnumBombType.TNT, 10F));

        entries.put(new ComparableStack(ModItems.n2_charge), new CustomNukeEntry(EnumBombType.TNT, 1.25F, EnumEntryType.MULT));
        entries.put(new ComparableStack(Items.REDSTONE), new CustomNukeEntry(EnumBombType.TNT, 1.05F, EnumEntryType.MULT));
        entries.put(new ComparableStack(Blocks.REDSTONE_BLOCK), new CustomNukeEntry(EnumBombType.TNT, 1.5F, EnumEntryType.MULT));




        //NUKE
        entries.put(new ComparableStack(ModItems.ingot_u235), new CustomNukeEntry(EnumBombType.NUKE, 15F));
        entries.put(new ComparableStack(ModItems.ingot_pu239), new CustomNukeEntry(EnumBombType.NUKE, 25F));
        entries.put(new ComparableStack(ModItems.ingot_neptunium), new CustomNukeEntry(EnumBombType.NUKE, 30F));
        entries.put(new ComparableStack(ModItems.nugget_u235), new CustomNukeEntry(EnumBombType.NUKE, 1.5F));
        entries.put(new ComparableStack(ModItems.nugget_pu239), new CustomNukeEntry(EnumBombType.NUKE, 2.5F));
        entries.put(new ComparableStack(ModItems.nugget_neptunium), new CustomNukeEntry(EnumBombType.NUKE, 3.0F));
        entries.put(new ComparableStack(ModItems.powder_neptunium), new CustomNukeEntry(EnumBombType.NUKE, 30F));
        entries.put(new ComparableStack(ModItems.custom_nuke), new CustomNukeEntry(EnumBombType.NUKE, 30F));
        entries.put(new ComparableStack(ModItems.ingot_uranium), new CustomNukeEntry(EnumBombType.NUKE, 1.05F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.ingot_plutonium), new CustomNukeEntry(EnumBombType.NUKE, 1.15F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.ingot_u238), new CustomNukeEntry(EnumBombType.NUKE, 1.1F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.ingot_pu238), new CustomNukeEntry(EnumBombType.NUKE, 1.15F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.nugget_uranium), new CustomNukeEntry(EnumBombType.NUKE, 1.005F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.nugget_plutonium), new CustomNukeEntry(EnumBombType.NUKE, 1.15F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.nugget_u238), new CustomNukeEntry(EnumBombType.NUKE, 1.01F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.nugget_pu238), new CustomNukeEntry(EnumBombType.NUKE, 1.015F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.powder_uranium), new CustomNukeEntry(EnumBombType.NUKE, 1.05F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.powder_plutonium), new CustomNukeEntry(EnumBombType.NUKE, 1.15F, EnumEntryType.MULT));



        //SUPER
        entries.put(new ComparableStack(ItemCell.getFullCell(Fluids.DEUTERIUM)), new CustomNukeEntry(EnumBombType.HYDRO, 20F));
        entries.put(new ComparableStack(ItemCell.getFullCell(Fluids.TRITIUM)), new CustomNukeEntry(EnumBombType.HYDRO, 30F));
        entries.put(new ComparableStack(ModItems.lithium), new CustomNukeEntry(EnumBombType.HYDRO, 20F));
        entries.put(new ComparableStack(ModItems.tritium_deuterium_cake), new CustomNukeEntry(EnumBombType.HYDRO, 200F));
        entries.put(new ComparableStack(ModItems.custom_hydro), new CustomNukeEntry(EnumBombType.HYDRO, 30F));


		//ANTIMATTER
        entries.put(new ComparableStack(ItemCell.getFullCell(Fluids.AMAT)), new CustomNukeEntry(EnumBombType.BALE, 5F));
        entries.put(new ComparableStack(ModItems.custom_amat), new CustomNukeEntry(EnumBombType.BALE, 15F));
        entries.put(new ComparableStack(ModItems.egg_balefire_shard), new CustomNukeEntry(EnumBombType.BALE, 15F));
        entries.put(new ComparableStack(ModItems.egg_balefire), new CustomNukeEntry(EnumBombType.BALE, 150F));

		
		//SALTED
        entries.put(new ComparableStack(ModItems.ingot_tungsten), new CustomNukeEntry(EnumBombType.DIRTY, 1F));
        entries.put(new ComparableStack(ModItems.custom_dirty), new CustomNukeEntry(EnumBombType.DIRTY, 10F));
        entries.put(new ComparableStack(ModItems.ingot_pu240), new CustomNukeEntry(EnumBombType.DIRTY, 1.05F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModItems.nuclear_waste), new CustomNukeEntry(EnumBombType.DIRTY, 1.025F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModBlocks.block_waste), new CustomNukeEntry(EnumBombType.DIRTY, 1.25F, EnumEntryType.MULT));
        entries.put(new ComparableStack(ModBlocks.yellow_barrel), new CustomNukeEntry(EnumBombType.DIRTY, 1.2F, EnumEntryType.MULT));



        //ANTISCHRABIDIUM
        entries.put(new ComparableStack(ModItems.ingot_schrabidium), new CustomNukeEntry(EnumBombType.SCHRAB, 5F));
        entries.put(new ComparableStack(ModBlocks.block_schrabidium), new CustomNukeEntry(EnumBombType.SCHRAB, 50F));
        entries.put(new ComparableStack(ModItems.nugget_schrabidium), new CustomNukeEntry(EnumBombType.SCHRAB, 0.5F));
        entries.put(new ComparableStack(ModItems.powder_schrabidium), new CustomNukeEntry(EnumBombType.SCHRAB, 5F));
        entries.put(new ComparableStack(ItemCell.getFullCell(Fluids.SAS3)), new CustomNukeEntry(EnumBombType.SCHRAB, 7.5F));
        entries.put(new ComparableStack(ItemCell.getFullCell(Fluids.ASCHRAB)), new CustomNukeEntry(EnumBombType.SCHRAB, 15F));
        entries.put(new ComparableStack(ModItems.custom_schrab), new CustomNukeEntry(EnumBombType.SCHRAB, 15F));


        //SOLINIUM
		entries.put(new ComparableStack(ModItems.solinium_core), new CustomNukeEntry(EnumBombType.SOL, 20F));
		entries.put(new ComparableStack(ModItems.nugget_solinium), new CustomNukeEntry(EnumBombType.SOL, 0.5F));
		entries.put(new ComparableStack(ModItems.ingot_solinium), new CustomNukeEntry(EnumBombType.SOL, 5F));
		entries.put(new ComparableStack(ModItems.billet_solinium), new CustomNukeEntry(EnumBombType.SOL, 3F));
		entries.put(new ComparableStack(ModBlocks.block_solinium), new CustomNukeEntry(EnumBombType.SOL, 50F));

		entries.put(new ComparableStack(ModItems.custom_sol), new CustomNukeEntry(EnumBombType.SOL, 15F));

		//ANTI-MASS
        entries.put(new ComparableStack(ModItems.nugget_euphemium), new CustomNukeEntry(EnumBombType.EUPH, 1F));
        entries.put(new ComparableStack(ModItems.ingot_euphemium), new CustomNukeEntry(EnumBombType.EUPH, 1F));

    }
	
	public float tnt;
	public float nuke;
	public float hydro;
	public float bale;
	public float dirty;
	public float schrab;
	public float sol;
	public float euph;
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void update() {
		float tnt = 0F,		tntMod = 1F;
		float nuke = 0F,	nukeMod = 1F;
		float hydro = 0F,	hydroMod = 1F;
		float bale = 0F,	baleMod = 1F;
		float dirty = 0F,	dirtyMod = 1F;
		float schrab = 0F,	schrabMod = 1F;
		float sol = 0F,		solMod = 1F;
		float euph = 0F;
		
		for(int i = 0; i < inventory.getSlots(); i ++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack.isEmpty())
				continue;
			
			ComparableStack comp = new NbtComparableStack(stack).makeSingular();
			CustomNukeEntry ent = entries.get(comp);
			
			if(ent == null)
				continue;
			
			if(ent.entry == EnumEntryType.ADD) {

				switch(ent.type) {
				case TNT: tnt += ent.value; break;
				case NUKE: nuke += ent.value; break;
				case HYDRO: hydro += ent.value; break;
				case BALE: bale += ent.value; break;
				case DIRTY: dirty += ent.value; break;
				case SCHRAB: schrab += ent.value; break;
				case SOL: sol += ent.value; break;
				case EUPH: euph += ent.value; break;
				}

			} else if(ent.entry == EnumEntryType.MULT) {

				switch(ent.type) {
				case TNT: tntMod *= ent.value; break;
				case NUKE: nukeMod *= ent.value; break;
				case HYDRO: hydroMod *= ent.value; break;
				case BALE: baleMod *= ent.value; break;
				case DIRTY: dirtyMod *= ent.value; break;
				case SOL: solMod *= ent.value; break;
				case SCHRAB: schrabMod *= ent.value; break;
				}
			}
		}
		tnt *= tntMod;
		nuke *= nukeMod;
		hydro *= hydroMod;
		bale *= baleMod;
		dirty *= dirtyMod;
		sol *= solMod;
		schrab *= schrabMod;

		if(tnt < 16) nuke = 0;
		if(nuke < 100) hydro = 0;
		if(nuke < 50) bale = 0;
		if(nuke < 50) schrab = 0;
		if(nuke < 25) sol = 0;
		if(schrab < 1 || sol < 1) euph = 0;

		this.tnt = Math.min(tnt, BombConfig.maxCustomTNTRadius);
		this.nuke = Math.min(nuke, BombConfig.maxCustomNukeRadius);
		this.hydro = Math.min(hydro, BombConfig.maxCustomHydroRadius);
		this.bale = Math.min(bale, BombConfig.maxCustomBaleRadius);
		this.dirty = Math.min(dirty, BombConfig.maxCustomDirtyRadius);
		this.schrab = Math.min(schrab, BombConfig.maxCustomSchrabRadius);
		this.sol = Math.min(sol, BombConfig.maxCustomSolRadius);
		this.euph = Math.min(euph, BombConfig.maxCustomEuphLvl);
	}
	
	public float getNukeAdj() {

		if(nuke == 0)
			return 0;

		return Math.min(nuke + tnt / 2, BombConfig.maxCustomNukeRadius);
	}

	public float getHydroAdj() {

		if(hydro == 0)
			return 0;

		return Math.min(hydro + nuke / 2 + tnt / 4, BombConfig.maxCustomHydroRadius);
	}

	public float getBaleAdj() {

		if(bale == 0)
			return 0;

		return Math.min(bale + hydro / 2 + nuke / 4 + tnt / 8, BombConfig.maxCustomBaleRadius);
	}

	public float getSchrabAdj() {

		if(schrab == 0)
			return 0;

		return Math.min(schrab + bale / 2 + hydro / 4 + nuke / 8 + tnt / 16, BombConfig.maxCustomSchrabRadius);
	}

	public float getSolAdj() {

		if(sol == 0)
			return 0;

		return Math.min(sol + schrab / 2 + bale / 4 + hydro / 8 + nuke / 16 + tnt / 32, BombConfig.maxCustomSolRadius);
	}
	
	public boolean isFalling() {

		for(int i = 0; i < inventory.getSlots(); i ++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if(stack != null && stack.getItem() == ModItems.custom_fall)
				return true;
		}
		
		return false;
	}
	
	public void destruct() {

		clearSlots();
		world.destroyBlock(pos, false);
	}
	
	public void clearSlots() {
		for(int i = 0; i < inventory.getSlots(); i++)
		{
			inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	public static enum EnumBombType {
		TNT("TNT"),
		NUKE("Nuclear"),
		HYDRO("Hydrogen"),
		BALE("Balefire"),
		DIRTY("Salted"),
		SCHRAB("Schrabidium"),
		SOL("Solinium"),
		EUPH("Anti Mass");

		String name;

		EnumBombType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static enum EnumEntryType {
		ADD,
		MULT
	}

	public static class CustomNukeEntry {

		public EnumBombType type;
		public EnumEntryType entry;
		public float value;

		public CustomNukeEntry(EnumBombType type, float value) {
			this.type = type;
			this.entry = EnumEntryType.ADD;
			this.value = value;
		}

		public CustomNukeEntry(EnumBombType type, float value, EnumEntryType entry) {
			this(type, value);
			this.entry = entry;
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerNukeCustom(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUINukeCustom(player.inventory, this);
	}
}
