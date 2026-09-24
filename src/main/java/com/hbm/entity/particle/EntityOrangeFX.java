package com.hbm.entity.particle;

import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityOrangeFX extends Particle {

	private static final Item[] TEXTURE_ITEMS = new Item[]{
			ModItems.orange1, ModItems.orange2, ModItems.orange3, ModItems.orange4,
			ModItems.orange5, ModItems.orange6, ModItems.orange7, ModItems.orange8
	};

	private final int meta;
	private int lastStage = -1;

	public EntityOrangeFX(World world) {
		this(world, 0, 0, 0, 0, 0, 0);
	}

	public EntityOrangeFX(World world, double x, double y, double z, double mx, double my, double mz) {
		this(world, x, y, z, mx, my, mz, 1.0F, 0);
	}

	public EntityOrangeFX(World world, double x, double y, double z, double mx, double my, double mz, float scale) {
		this(world, x, y, z, mx, my, mz, scale, 0);
	}

	public EntityOrangeFX(World world, double x, double y, double z, double mx, double my, double mz, float scale, int meta) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);

		this.meta = meta;

		this.motionX *= 0.1D;
		this.motionY *= 0.1D;
		this.motionZ *= 0.1D;
		this.motionX += mx;
		this.motionY += my;
		this.motionZ += mz;
		this.particleRed = this.particleGreen = this.particleBlue = 1 - (float)(Math.random() * 0.3D);
		this.setSize(0.2F, 0.2F);
		this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 15.0F * 0.75F * scale;

		this.particleMaxAge = this.rand.nextInt(301) + 900;

		this.canCollide = true;

		updateSprite();
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAge++;
		if (this.particleAge >= this.particleMaxAge) {
			this.setExpired();
			return;
		}

		this.motionX *= 0.86D;
		this.motionY *= 0.86D;
		this.motionZ *= 0.86D;
		this.motionY -= 0.1D;


		double subdivisions = 4;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for(int i = 0; i < subdivisions; i++) {
			this.posX += this.motionX/subdivisions;
			this.posY += this.motionY/subdivisions;
			this.posZ += this.motionZ/subdivisions;
			pos.setPos((int) posX, (int) posY, (int) posZ);
			if(world.getBlockState(pos).getMaterial() != Material.AIR) {
				this.setExpired();
			}
		}

		updateSprite();
	}

	private void updateSprite() {
		if (this.particleMaxAge <= 0) return;
		int stage = this.particleAge * 8 / this.particleMaxAge;
		if (stage < 0) stage = 0;
		if (stage > 7) stage = 7;

		if (stage != this.lastStage) {
			Item item = TEXTURE_ITEMS[stage];
			TextureAtlasSprite tas = Minecraft.getMinecraft()
					.getRenderItem()
					.getItemModelWithOverrides(new ItemStack(item, 1, this.meta), null, null)
					.getParticleTexture();
			this.setParticleTexture(tas);
			this.lastStage = stage;
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}
}
