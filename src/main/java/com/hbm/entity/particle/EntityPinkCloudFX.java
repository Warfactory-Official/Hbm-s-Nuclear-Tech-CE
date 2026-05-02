package com.hbm.entity.particle;

import com.hbm.items.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityPinkCloudFX extends Particle {

	private static final Item[] TEXTURE_ITEMS = new Item[]{
			ModItems.pc1, ModItems.pc2, ModItems.pc3, ModItems.pc4,
			ModItems.pc5, ModItems.pc6, ModItems.pc7, ModItems.pc8
	};

	private final int meta;
	private int lastStage = -1;

	public EntityPinkCloudFX(World world) {
		this(world, 0, 0, 0, 0, 0, 0);
	}

	public EntityPinkCloudFX(World world, double x, double y, double z, double mx, double my, double mz) {
		this(world, x, y, z, mx, my, mz, 1.0F, 0);
	}

	public EntityPinkCloudFX(World world, double x, double y, double z, double mx, double my, double mz, float scale) {
		this(world, x, y, z, mx, my, mz, scale, 0);
	}

	public EntityPinkCloudFX(World world, double x, double y, double z, double mx, double my, double mz, float scale, int meta) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);

		this.meta = meta;

		this.motionX *= 0.10000000149011612D;
		this.motionY *= 0.10000000149011612D;
		this.motionZ *= 0.10000000149011612D;
		this.motionX += mx;
		this.motionY += my;
		this.motionZ += mz;

		this.particleRed = this.particleGreen = this.particleBlue = 1 - (float) (Math.random() * 0.3D);
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

		if (this.world.isRaining() && this.world.canBlockSeeSky(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)))) {
			this.motionY -= 0.01D;
		}

		this.motionX *= 0.7599999785423279D;
		this.motionY *= 0.7599999785423279D;
		this.motionZ *= 0.7599999785423279D;

		this.move(this.motionX, this.motionY, this.motionZ);

		if (this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}

		BlockPos pos = new BlockPos((int) this.posX, (int) this.posY, (int) this.posZ);
		IBlockState state = this.world.getBlockState(pos);

		if (state.isNormalCube()) {
			if (this.rand.nextInt(5) != 0) {
				this.setExpired();
			}
			this.posX = this.prevPosX;
			this.posY = this.prevPosY;
			this.posZ = this.prevPosZ;
			this.motionX = 0.0D;
			this.motionY = 0.0D;
			this.motionZ = 0.0D;
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
