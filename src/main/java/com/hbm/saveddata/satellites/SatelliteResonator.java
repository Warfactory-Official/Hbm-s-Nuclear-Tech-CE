package com.hbm.saveddata.satellites;

import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SatelliteResonator extends Satellite {

	public SatelliteResonator() {
		this.coordAcs.add(CoordActions.HAS_Y);
		this.satIface = Interfaces.SAT_COORD;
	}

	@Override public String getType() { return "XEN_RELAY"; }

	@Override
	public ITextComponent[] getInfo(World world) {
		return new ITextComponent[] {
				new TextComponentTranslation(ItemSatellite.make(EnumSatType.XENIUM_RESONATOR).getTranslationKey() + ".name")
		};
	}

	@Override
	public void onCoordAction(World world, EntityPlayerMP player, int x, int y, int z) {

		world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.dismountRidingEntity();
		world.getChunkProvider().provideChunk(x >> 4, z >> 4);
		if(y < 0) y = world.getHeight(x, z);
		player.connection.setPlayerLocation(x + 0.5D, y, z + 0.5D, player.rotationYaw, player.rotationPitch);
		world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	@Override
	public float[] getColor() {
		return new float[] { 1.0F, 0.646F, 0.181F };
	}
}
