package com.hbm.inventory.fluid;

import com.hbm.Tags;
import com.hbm.api.fluidmk2.FluidNetMK2;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.*;
import com.hbm.render.misc.EnumSymbol;
import com.hbm.uninos.INetworkProvider;
import com.hbm.util.I18nUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FluidType {

    // mlbv: according to upstream all these fields below are MUTABLE
    //The numeric ID of the fluid
    private int id;
    //The internal name
    private String stringId;
    //Approximate HEX Color of the fluid, used for pipe rendering
    private int color;
    //Unlocalized string ID of the fluid
    private String unlocalized;
    //localization override for custom fluids
    private String localizedOverride;
    private int guiTint = 0xffffff;

    public int poison;
    public int flammability;
    public int reactivity;
    public EnumSymbol symbol;
    public boolean renderWithTint = false;
    public boolean ffBan = false;

    public static final int ROOM_TEMPERATURE = 20;

    // v v v this entire system is a pain in the ass to work with. i'd much rather define state transitions and heat values manually.
    /** How hot this fluid is. Simple enough. */
    public int temperature = ROOM_TEMPERATURE;

    public HashMap<Class<?>, Object> containers = new HashMap<>();
    public HashMap<Class<? extends FluidTrait>, FluidTrait> traits = new HashMap<>();
    //public List<EnumFluidTrait> enumTraits = new ArrayList();

    private ResourceLocation texture;
    private String ffNameOverride;

    public FluidType(String name, int color, int p, int f, int r, EnumSymbol symbol) {
        this.stringId = name;
        this.color = color;
        this.unlocalized = "hbmfluid." + name.toLowerCase(Locale.US);
        this.poison = p;
        this.flammability = f;
        this.reactivity = r;
        this.symbol = symbol;
        this.texture = new ResourceLocation(Tags.MODID + ":textures/gui/fluids/" + name.toLowerCase(Locale.US) + ".png");

        this.id = Fluids.registerSelf(this);
    }

    /** For custom fluids */
    public FluidType(String name, int color, int p, int f, int r, EnumSymbol symbol, String texName, int tint, int id, String displayName) {
        setupCustom(name, color, p, f, r, symbol, texName, tint, id, displayName);
    }

    public FluidType setupCustom(String name, int color, int p, int f, int r, EnumSymbol symbol, String texName, int tint, int id, String displayName) {
        this.stringId = name;
        this.color = color;
        this.unlocalized = "hbmfluid." + name.toLowerCase(Locale.US);
        this.poison = p;
        this.flammability = f;
        this.reactivity = r;
        this.symbol = symbol;
        this.texture = new ResourceLocation(Tags.MODID + ":textures/gui/fluids/" + texName + ".png");
        this.guiTint = tint;
        this.localizedOverride = displayName;
        this.renderWithTint = true;

        this.id = id;
        Fluids.register(this, id);
        return this;
    }

    public FluidType(int forcedId, String name, int color, int p, int f, int r, EnumSymbol symbol) {
        this(name, color, p, f, r, symbol);

        if(this.id != forcedId) {
            throw new IllegalStateException("Howdy! I am a safeguard put into place by Bob to protect you, the player, from Bob's dementia. For whatever reason, Bob decided to either add or remove a fluid in a way that shifts the IDs, despite the entire system being built to prevent just that. Instead of people's fluids getting jumbled for the 500th time, I am here to prevent the game from starting entirely. The expected ID was " + forcedId + ", but turned out to be " + this.id + ".");
        }
    }

    /** For CompatFluidRegistry */
    public FluidType(String name, int id, int color, int p, int f, int r, EnumSymbol symbol, ResourceLocation texture) {
        setupForeign(name, id, color, p, f, r, symbol, texture);
    }

    public FluidType setupForeign(String name, int id, int color, int p, int f, int r, EnumSymbol symbol, ResourceLocation texture) {
        this.stringId = name;
        this.color = color;
        this.unlocalized = "hbmfluid." + name.toLowerCase(Locale.US);
        this.poison = p;
        this.flammability = f;
        this.reactivity = r;
        this.symbol = symbol;
        this.texture = texture;
        this.renderWithTint = true;

        this.id = id;
        Fluids.foreignFluids.add(this);
        Fluids.register(this, id);
        return this;
    }

    public FluidType setTemp(int temperature) {
        this.temperature = temperature;
        return this;
    }
        public FluidType noFF(boolean bool) {
        this.ffBan = bool;
        return this;
    }

    @Nullable
    public Fluid getFF(){
        if (this.ffBan) return null;
        return FluidRegistry.getFluid(this.getFFName());
    }

    public FluidType addContainers(Object... containers) {
        for(Object container : containers) this.containers.put(container.getClass(), container);
        return this;
    }

    public <T> T getContainer(Class<? extends T> container) {
        return (T) this.containers.get(container);
    }

    public FluidType addTraits(FluidTrait... traits) {
        for(FluidTrait trait : traits) this.traits.put(trait.getClass(), trait);
        return this;
    }

    public FluidType setFFNameOverride(String override){
        this.ffNameOverride = override;
        return this;
    }

    public boolean hasTrait(Class<? extends FluidTrait> trait) {
        return this.traits.containsKey(trait);
    }

    public <T extends FluidTrait> T getTrait(Class<? extends T> trait) { //generics, yeah!
        return (T) this.traits.get(trait);
    }

    public int getID() {
        return this.id;
    }
    /** The unique mapping name for this fluid, usually matches the unlocalied name, minus the prefix */
    public String getName() {
        return this.stringId;
    }

    public int getColor() {
        return this.color;
    }

    public int getTint() {
        return this.guiTint;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }
    public String getTranslationKey() {
        return this.unlocalized;
    }
    /** Returns the localized override name if present, or otherwise the I18n converted name */
    @SideOnly(Side.CLIENT) public String getLocalizedName() {
        return this.localizedOverride != null ? this.localizedOverride : I18nUtil.resolveKey(this.unlocalized);
    }
    /** Returns the localized override name if present, or otherwise the raw unlocalized name. Used for server-side code that needs ChatComponentTranslation. */
    public String getConditionalName() {
        return this.localizedOverride != null ? this.localizedOverride : this.unlocalized;
    }
    public String getDict(int quantity) {
        String prefix = GeneralConfig.enableFluidContainerCompat ? "container" : "ntmcontainer";
        return prefix + quantity + this.stringId.replace("_", "").toLowerCase(Locale.US);
    }

    protected INetworkProvider<FluidNetMK2> NETWORK_PROVIDER = () -> new FluidNetMK2(this);

    public INetworkProvider<FluidNetMK2> getNetworkProvider() {
        return NETWORK_PROVIDER;
    }

    public boolean isHot() {
        return this.temperature >= 100;
    }
    public boolean isCorrosive() {
        return this.traits.containsKey(FT_Corrosive.class);
    }
    public boolean isAntimatter() {
        return this.traits.containsKey(FT_Amat.class);
    }
    public boolean hasNoContainer() {
        return this.traits.containsKey(FT_NoContainer.class);
    }
    public boolean hasNoID() {
        return this.traits.containsKey(FT_NoID.class);
    }
    public boolean needsLeadContainer() {
        return this.traits.containsKey(FT_LeadContainer.class);
    }
    public boolean isDispersable() {
        return !(this.traits.containsKey(FT_Amat.class) || this.traits.containsKey(FT_NoContainer.class) || this.traits.containsKey(FT_Viscous.class));
    }

    public String getFFName(){
        return ffNameOverride == null ? stringId.toLowerCase(Locale.US) : ffNameOverride;
    }

    /**
     * Called when the tile entity is broken, effectively voiding the fluids.
     * @param te
     * @param tank
     */
    public void onTankBroken(TileEntity te, FluidTankNTM tank) { }
    /**
     * Called by the tile entity's update loop. Also has an arg for the fluid tank for possible tanks using child-classes that are shielded or treated differently.
     * @param te
     * @param tank
     */
    public void onTankUpdate(TileEntity te, FluidTankNTM tank) { }
    /**
     * For when the tile entity is releasing this fluid into the world, either by an overflow or (by proxy) when broken.
     * @param te
     * @param tank
     * @param overflowAmount
     */
    public void onFluidRelease(TileEntity te, FluidTankNTM tank, int overflowAmount) {
        this.onFluidRelease(te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), tank, overflowAmount);
    }

    public void onFluidRelease(World world, int x, int y, int z, FluidTankNTM tank, int overflowAmount) { }
    //public void onFluidTransmit(FluidNetwork net) { }

    @SideOnly(Side.CLIENT)
    public void addInfo(List<String> info) {

        if(temperature != ROOM_TEMPERATURE) {
            if(temperature < 0) info.add(TextFormatting.BLUE + "" + temperature + "°C");
            if(temperature > 0) info.add(TextFormatting.RED + "" + temperature + "°C");
        }

        boolean shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        List<String> hidden = new ArrayList<>();

        for(Class<? extends FluidTrait> clazz : FluidTrait.traitList) {
            FluidTrait trait = this.getTrait(clazz);
            if(trait != null) {
                trait.addInfo(info);
                if(shiftHeld) trait.addInfoHidden(info);
                trait.addInfoHidden(hidden);
            }
        }

        if(!hidden.isEmpty() && !shiftHeld) {
            info.add(I18nUtil.resolveKey("desc.tooltip.hold", "LSHIFT"));
        }
    }

    // mlbv: slize wrote this, git blame is fucked up by me
    // that is for inventory barrels, tanks and lead tanks - I want to hide everything to SHIFT
    @SideOnly(Side.CLIENT)
    public void addInfoItemTanks(List<String> info) {

        boolean shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        List<String> hidden = new ArrayList<>();

        for(Class<? extends FluidTrait> clazz : FluidTrait.traitList) {
            FluidTrait trait = this.getTrait(clazz);
            if(trait != null) {
                if(shiftHeld){
                    if(temperature != ROOM_TEMPERATURE) {
                        if(temperature < 0) info.add(TextFormatting.BLUE + "" + temperature + "°C");
                        if(temperature > 0) info.add(TextFormatting.RED + "" + temperature + "°C");
                    }
                    trait.addInfo(info);
                    trait.addInfoHidden(info);
                    trait.addInfoHidden(hidden);
                }
            }
        }

        if(!shiftHeld) {
            info.add(I18nUtil.resolveKey("desc.tooltip.hold", "LSHIFT"));
        }
    }
}
