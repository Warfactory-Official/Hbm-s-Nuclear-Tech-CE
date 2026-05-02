package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerFEL;
import com.hbm.inventory.gui.GUIFEL;
import com.hbm.items.machine.ItemFELCrystal;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityFEL extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IGUIProvider {
	
	public long power;
	public static final long maxPower = 2000000000;
	public static final int powerReq = 1000;
	public EnumWavelengths mode = EnumWavelengths.NULL;
	public boolean isOn;
	public boolean missingValidSilex = true	;
	public int distance;
	private int prevDistance;
	public List<EntityLivingBase> entities = new ArrayList<>();
	private int audioDuration = 0;
	private AudioWrapper audio;
	
	
	public TileEntityFEL() {
		super(2, false, true);
	}

	@Override
	public String getDefaultName() {
		return "container.machineFEL";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
			this.trySubscribe(world, pos.getX() +dir.offsetX * -5, pos.getY() + 1, pos.getZ() + dir.offsetZ  * -5, dir);
			this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			
			if(this.isOn && !(inventory.getStackInSlot(1).getCount() == 0)) {
				
				if(inventory.getStackInSlot(1).getItem() instanceof ItemFELCrystal crystal) {

                    this.mode = crystal.wavelength;
					
				} else { this.mode = EnumWavelengths.NULL; }
				
			} else { this.mode = EnumWavelengths.NULL; }
			
			int range = 24;
			boolean silexSpacing = false;
			double xCoord = pos.getX();
			double yCoord = pos.getY();
			double zCoord = pos.getZ();
			if(this.isOn &&  this.mode != EnumWavelengths.NULL) {
				if(this.power < powerReq * Math.pow(4, mode.ordinal())){
					this.mode = EnumWavelengths.NULL;
					this.power = 0;
				} else {
					int distance = this.distance-1;
					double blx = Math.min(xCoord, xCoord + (double)dir.offsetX * distance) + 0.2;
					double bux = Math.max(xCoord, xCoord + (double)dir.offsetX * distance) + 0.8;
					double bly = Math.min(yCoord, 1 + yCoord + (double)dir.offsetY * distance) + 0.2;
					double buy = Math.max(yCoord, 1 + yCoord + (double)dir.offsetY * distance) + 0.8;
					double blz = Math.min(zCoord, zCoord + (double)dir.offsetZ * distance) + 0.2;
					double buz = Math.max(zCoord, zCoord + (double)dir.offsetZ * distance) + 0.8;
					
					List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(blx, bly, blz, bux, buy, buz));
					
					for(EntityLivingBase entity : list) {
                        switch (this.mode) {
                            case IR -> {}
                            case VISIBLE -> entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60 * 60 * 65536, 0));
                            case UV -> entity.setFire(10);
                            case GAMMA -> ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 25);
                            case DRX -> ContaminationUtil.applyDigammaData(entity, 0.1F);
                        }
					}
					
					this.power -= (long) (powerReq * ((mode.ordinal() == 0) ? 0 : Math.pow(4, mode.ordinal())));
					for(int i = 3; i < range; i++) {
					
						double x = xCoord + dir.offsetX * i;
						double y = yCoord + 1;
						double z = zCoord + dir.offsetZ * i;
						
						IBlockState b = world.getBlockState(new BlockPos(x, y, z));
						
						if(!(b.getMaterial().isOpaque()) && b != Blocks.TNT) {
							this.distance = range;
							silexSpacing = false;
							continue;
						}
						
						if(b.getBlock() == ModBlocks.machine_silex) {
							BlockPos silex_pos = new BlockPos(x + dir.offsetX, yCoord, z + dir.offsetZ);
							TileEntity te = world.getTileEntity(silex_pos);
						
							if(te instanceof TileEntitySILEX silex) {
                                int meta = silex.getBlockMetadata() - BlockDummyable.offset;
								if(rotationIsValid(meta, this.getBlockMetadata() - BlockDummyable.offset) && i >= 5 && !silexSpacing) {
									if(silex.mode != this.mode) {
										silex.mode = this.mode;
										this.missingValidSilex = false;
										silexSpacing = true;
                                    }
								} else {
									world.setBlockToAir(silex_pos);
									world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Item.getItemFromBlock(ModBlocks.machine_silex))));
								} 
							}
							
						} else if(b.getBlock() != Blocks.AIR){
							this.distance = i;
							float hardness = b.getBlock().getExplosionResistance(null);
							boolean blocked = false;
                            switch (this.mode) {
                                case IR -> {
                                    if (b.getMaterial().isOpaque() || b.getMaterial() == Material.GLASS)
                                        blocked = true;
                                }
                                case VISIBLE -> {
                                    if (b.getMaterial().isOpaque()) {
                                        if (hardness < 10 && world.rand.nextInt(40) == 0) {
                                            world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                            world.setBlockState(new BlockPos(x, y, z), Blocks.FIRE.getDefaultState());
                                        } else {
                                            blocked = true;
                                        }
                                    }
                                }
                                case UV -> {
                                    if (b.getMaterial().isOpaque()) {
                                        if (hardness < 100 && world.rand.nextInt(20) == 0) {
                                            world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                            world.setBlockState(new BlockPos(x, y, z), Blocks.FIRE.getDefaultState());
                                        } else {
                                            blocked = true;
                                        }
                                    }
                                }
                                case GAMMA -> {
                                    if (b.getMaterial().isOpaque()) {
                                        if (hardness < 3000 && world.rand.nextInt(5) == 0) {
                                            world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                            world.setBlockState(new BlockPos(x, y, z), ModBlocks.balefire.getDefaultState());
                                        } else {
                                            blocked = true;
                                        }
                                    }
                                }
                                case DRX -> {
                                    world.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                    world.setBlockState(new BlockPos(x, y, z), ((MainRegistry.polaroidID == 11) ? ModBlocks.digamma_matter : ModBlocks.fire_digamma).getDefaultState());
                                    world.setBlockState(new BlockPos(x, y - 1, z), ModBlocks.ash_digamma.getDefaultState());
                                }
                            }
							if(blocked)
								break;
						}
					}
				}
			}

			networkPackNT(250);
		} else {

			if(prevDistance != distance) {
				prevDistance = distance;
				world.markBlockRangeForRenderUpdate(pos, pos);
			}

			if(power > powerReq * Math.pow(2, mode.ordinal()) && isOn && !(mode == EnumWavelengths.NULL) && distance - 3 > 0) {
				audioDuration += 2;
			} else {
				audioDuration -= 3;
			}

			audioDuration = MathHelper.clamp(audioDuration, 0, 60);

			if(audioDuration > 10) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(2F));
				audio.updatePitch((audioDuration - 10) / 100F + 0.5F);

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}
	
	private boolean rotationIsValid(int silexMeta, int felMeta) {
		ForgeDirection silexDir = ForgeDirection.getOrientation(silexMeta);
		ForgeDirection felDir = ForgeDirection.getOrientation(felMeta);
        return silexDir == felDir || silexDir == felDir.getOpposite();
    }
	
	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		BufferUtil.writeString(buf, mode.toString());
		buf.writeBoolean(isOn);
		buf.writeBoolean(missingValidSilex);
		buf.writeInt(distance);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.power = buf.readLong();
		this.mode = EnumWavelengths.valueOf(BufferUtil.readString(buf));
		this.isOn = buf.readBoolean();
		this.missingValidSilex = buf.readBoolean();
		this.distance = buf.readInt();
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.fel, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 2.0F, 10F, 2.0F);
	}

	@Override
	public void handleButtonPacket(int value, int meta) {
		
		if(meta == 2){
			this.isOn = !this.isOn;
		}
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		this.power = nbt.getLong("power");
		this.mode = nbt.hasKey("mode") ? EnumWavelengths.valueOf(nbt.getString("mode")) : EnumWavelengths.NULL;
		this.isOn = nbt.getBoolean("isOn");
		this.missingValidSilex = nbt.getBoolean("valid");
		this.distance = nbt.getInteger("distance");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		nbt.setLong("power", this.power);
		nbt.setString("mode", this.mode.toString());
		nbt.setBoolean("isOn", this.isOn);
		nbt.setBoolean("valid", this.missingValidSilex);
		nbt.setInteger("distance", this.distance);
		return nbt;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		int d = distance + 4;
		return new AxisAlignedBB(pos.getX() - d, pos.getY(), pos.getZ() - d, pos.getX() + 1 + d, pos.getY() + 3, pos.getZ() + 1 + d);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerFEL(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIFEL(player.inventory, this);
	}
}