package com.hbm.explosion;

import com.google.common.collect.Sets;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.*;

// Th3_Sl1ze: ik it's deprecated, but I want to make it working at least like on upstream; I've basically re-ported it
@Deprecated
public class ExplosionNT extends Explosion {

	public Set<ExAttrib> atttributes = new HashSet<>();

	protected int resolution = 16;

	@Deprecated
	public static final List<ExAttrib> nukeAttribs = Arrays.asList(ExAttrib.FIRE, ExAttrib.NOPARTICLE, ExAttrib.NOSOUND, ExAttrib.NODROP, ExAttrib.NOHURT);

	public ExplosionNT(World world, @Nullable Entity exploder, double x, double y, double z, float strength) {
		super(world, exploder, x, y, z, strength, false, true);
	}

	public ExplosionNT addAttrib(ExAttrib attrib) {
		atttributes.add(attrib);
		return this;
	}
	
	public ExplosionNT addAllAttrib(List<ExAttrib> attrib) {
		atttributes.addAll(attrib);
		return this;
	}

	public ExplosionNT overrideResolution(int res) {
		this.resolution = res;
		return this;
	}

	public void explode() {
		if(CompatibilityConfig.isWarDim(this.world)) {
			doExplosionA();
			doExplosionB(false);
		}
	}

	@Override
	public void doExplosionA() {
		float f = this.size;
		Set<BlockPos> hashset = Sets.newHashSet();
		int i;
		int j;
		int k;
		double currentX;
		double currentY;
		double currentZ;

		for(i = 0; i < this.resolution; ++i) {
			for(j = 0; j < this.resolution; ++j) {
				for(k = 0; k < this.resolution; ++k) {
					if(i == 0 || i == this.resolution - 1 || j == 0 || j == this.resolution - 1 || k == 0 || k == this.resolution - 1) {
						double d0 = (float) i / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
						double d1 = (float) j / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
						double d2 = (float) k / ((float) this.resolution - 1.0F) * 2.0F - 1.0F;
						double dist = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						d0 /= dist;
						d1 /= dist;
						d2 /= dist;

						float remainingPower = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
						currentX = this.x;
						currentY = this.y;
						currentZ = this.z;

						for(float step = 0.3F; remainingPower > 0.0F; remainingPower -= step * 0.75F) {
							BlockPos pos = new BlockPos(currentX, currentY, currentZ);
							IBlockState block = this.world.getBlockState(pos);

							if(block.getMaterial() != Material.AIR) {
								float resistance = this.exploder != null ? this.exploder.getExplosionResistance(this, this.world, pos, block) : block.getBlock().getExplosionResistance(this.world, pos, null, this);
								remainingPower -= (resistance + 0.3F) * step;
							}

							if(block.getMaterial() != Material.AIR && remainingPower > 0.0F && (this.exploder == null || this.exploder.canExplosionDestroyBlock(this, this.world, pos, block, remainingPower))) {
								hashset.add(pos);
							} else if(this.has(ExAttrib.ERRODE) && errosion.containsKey(block.getBlock())) {
								hashset.add(pos);
							}

							currentX += d0 * (double) step;
							currentY += d1 * (double) step;
							currentZ += d2 * (double) step;
						}
					}
				}
			}
		}

		this.affectedBlockPositions.addAll(hashset);

		if(!has(ExAttrib.NOHURT)) {
			this.size *= 2.0F;
			i = MathHelper.floor(this.x - (double) this.size - 1.0D);
			j = MathHelper.floor(this.x + (double) this.size + 1.0D);
			k = MathHelper.floor(this.y - (double) this.size - 1.0D);
			int i2 = MathHelper.floor(this.y + (double) this.size + 1.0D);
			int l = MathHelper.floor(this.z - (double) this.size - 1.0D);
			int j2 = MathHelper.floor(this.z + (double) this.size + 1.0D);
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB(i, k, l, j, i2, j2));
			ForgeEventFactory.onExplosionDetonate(this.world, this, list, this.size);
			Vec3d vec3 = new Vec3d(this.x, this.y, this.z);

            for (Entity entity : list) {
                if (!entity.isImmuneToExplosions()) {
                    double d4 = entity.getDistance(this.x, this.y, this.z) / (double) this.size;

                    if (d4 <= 1.0D) {
                        currentX = entity.posX - this.x;
                        currentY = entity.posY + (double) entity.getEyeHeight() - this.y;
                        currentZ = entity.posZ - this.z;
                        double d9 = MathHelper.sqrt(currentX * currentX + currentY * currentY + currentZ * currentZ);

                        if (d9 != 0.0D) {
                            currentX /= d9;
                            currentY /= d9;
                            currentZ /= d9;
                            double d10 = this.world.getBlockDensity(vec3, entity.getEntityBoundingBox());
                            double d11 = (1.0D - d4) * d10;
                            entity.attackEntityFrom(setExplosionSource(this), (float) ((int) ((d11 * d11 + d11) / 2.0D * 8.0D * (double) this.size + 1.0D)));
                            double d8 = d11;
                            if (entity instanceof EntityLivingBase)
                                d8 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase) entity, d11);
                            entity.motionX += currentX * d8;
                            entity.motionY += currentY * d8;
                            entity.motionZ += currentZ * d8;

                            if (entity instanceof EntityPlayer entityplayer) {
                                if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying)) {
                                    this.playerKnockbackMap.put(entityplayer, new Vec3d(currentX * d11, currentY * d11, currentZ * d11));
                                }
                            }
                        }
                    }
                }
            }

			this.size = f;
		}
	}

	public static DamageSource setExplosionSource(Explosion explosion) {
		return explosion != null && explosion.getExplosivePlacedBy() != null ?
				(new EntityDamageSource("explosion.player", explosion.getExplosivePlacedBy())).setExplosion() :
				(new DamageSource("explosion")).setExplosion();
	}

	@Override
	public void doExplosionB(boolean spawnParticles) {
		if(!has(ExAttrib.NOSOUND))
			this.world.playSound(null, this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

		if(!has(ExAttrib.NOPARTICLE)) {
			if(this.size >= 2.0F && this.damagesTerrain) {
				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
			} else {
				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
			}
		}

		if(this.damagesTerrain) {

            for (BlockPos chunkposition : this.affectedBlockPositions) {
                int i = chunkposition.getX();
                int j = chunkposition.getY();
                int k = chunkposition.getZ();
                IBlockState block = this.world.getBlockState(chunkposition);

                if (!has(ExAttrib.NOPARTICLE)) {
                    double d0 = (float) i + this.world.rand.nextFloat();
                    double d1 = (float) j + this.world.rand.nextFloat();
                    double d2 = (float) k + this.world.rand.nextFloat();
                    double d3 = d0 - this.x;
                    double d4 = d1 - this.y;
                    double d5 = d2 - this.z;
                    double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 /= d6;
                    d4 /= d6;
                    d5 /= d6;
                    double d7 = 0.5D / (d6 / (double) this.size + 0.1D);
                    d7 *= this.world.rand.nextFloat() * this.world.rand.nextFloat() + 0.3F;
                    d3 *= d7;
                    d4 *= d7;
                    d5 *= d7;
                    this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.x) / 2.0D, (d1 + this.y) / 2.0D, (d2 + this.z) / 2.0D, d3, d4, d5);
                    this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (block.getMaterial() != Material.AIR) {
                    boolean doesErrode = false;
                    Block errodesInto = Blocks.AIR;

                    if (this.has(ExAttrib.ERRODE) && this.random.nextFloat() < 0.6F) {
                        if (errosion.containsKey(block.getBlock())) {
                            doesErrode = true;
                            errodesInto = errosion.get(block.getBlock());
                        }
                    }

                    if (block.getBlock().canDropFromExplosion(this) && !has(ExAttrib.NODROP) && !doesErrode) {
                        float chance = 1.0F;

                        if (!has(ExAttrib.ALLDROP))
                            chance = 1.0F / this.size;

                        block.getBlock().dropBlockAsItemWithChance(this.world, chunkposition, block, chance, 0);
                    }

                    block.getBlock().onBlockExploded(this.world, chunkposition, this);

                    if (block.isNormalCube()) {
                        if (doesErrode) {
                            this.world.setBlockState(chunkposition, errodesInto.getDefaultState());
                        }

                        if (has(ExAttrib.DIGAMMA)) {
                            this.world.setBlockState(chunkposition, ModBlocks.ash_digamma.getDefaultState());

                            if (this.random.nextInt(5) == 0 && this.world.getBlockState(chunkposition.up()).getBlock() == Blocks.AIR)
                                this.world.setBlockState(chunkposition.up(), ModBlocks.fire_digamma.getDefaultState());

                        } else if (has(ExAttrib.DIGAMMA_CIRCUIT)) {
                            if (i % 3 == 0 && k % 3 == 0) {
                                this.world.setBlockState(chunkposition, ModBlocks.pribris_digamma.getDefaultState());
                            } else if ((i % 3 == 0 || k % 3 == 0) && this.random.nextBoolean()) {
                                this.world.setBlockState(chunkposition, ModBlocks.pribris_digamma.getDefaultState());
                            } else {
                                this.world.setBlockState(chunkposition, ModBlocks.ash_digamma.getDefaultState());

                                if (this.random.nextInt(5) == 0 && this.world.getBlockState(chunkposition.up()).getBlock() == Blocks.AIR)
                                    this.world.setBlockState(chunkposition.up(), ModBlocks.fire_digamma.getDefaultState());
                            }
                        } else if (has(ExAttrib.LAVA_V)) {
                            this.world.setBlockState(chunkposition, ModBlocks.volcanic_lava_block.getDefaultState());
                        } else if (has(ExAttrib.LAVA_R)) {
                            this.world.setBlockState(chunkposition, ModBlocks.rad_lava_block.getDefaultState());
                        }
                    }
                }
            }
		}

		if(has(ExAttrib.FIRE) || has(ExAttrib.BALEFIRE) || has(ExAttrib.LAVA)) {

            for (BlockPos chunkposition : this.affectedBlockPositions) {
                IBlockState block = this.world.getBlockState(chunkposition);
                IBlockState block1 = this.world.getBlockState(chunkposition.down());

                boolean shouldReplace = true;

                if (!has(ExAttrib.ALLMOD) && !has(ExAttrib.DIGAMMA))
                    shouldReplace = this.random.nextInt(3) == 0;

                if (block.getMaterial() == Material.AIR && block1.isFullBlock() && shouldReplace) {
                    if (has(ExAttrib.FIRE))
                        this.world.setBlockState(chunkposition, Blocks.FIRE.getDefaultState());
                    else if (has(ExAttrib.BALEFIRE))
                        this.world.setBlockState(chunkposition, ModBlocks.balefire.getDefaultState());
                    else if (has(ExAttrib.LAVA))
                        this.world.setBlockState(chunkposition, Blocks.FLOWING_LAVA.getDefaultState());
                }
            }
		}
	}

	public boolean has(ExAttrib attrib) {
		return this.atttributes.contains(attrib);
	}

	// this solution is a bit hacky but in the end easier to work with
	public enum ExAttrib {
		FIRE,		//classic vanilla fire explosion
		BALEFIRE,	//same with but with balefire
		DIGAMMA,
		DIGAMMA_CIRCUIT,
		LAVA,		//again the same thing but lava
		LAVA_V,		//again the same thing but volcaniclava
		LAVA_R,		//again the same thing but radioactive lava
		ERRODE,		//will turn select blocks into gravel or sand
		ALLMOD,		//block placer attributes like fire are applied for all destroyed blocks
		ALLDROP,	//miner TNT!
		NODROP,		//the opposite
		NOPARTICLE,
		NOSOUND,
		NOHURT
	}


	public static final Map<Block, Block> errosion = new HashMap<>();

	static {
		errosion.put(ModBlocks.concrete, Blocks.GRAVEL);
		errosion.put(ModBlocks.concrete_smooth, Blocks.GRAVEL);
		errosion.put(ModBlocks.brick_concrete, ModBlocks.brick_concrete_broken);
		errosion.put(ModBlocks.brick_concrete_broken, Blocks.GRAVEL);
	}

}