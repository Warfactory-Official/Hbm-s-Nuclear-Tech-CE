package com.hbm.render.tileentity.door;

import java.nio.DoubleBuffer;

import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna.Animation;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.util.Clock;

public interface IRenderDoors {

	public void render(TileEntityDoorGeneric door, DoubleBuffer buf);
	
	public static double[] getRelevantTransformation(String bus, Animation anim) {

		if(anim != null) {

			BusAnimationSedna buses = anim.animation;
			int millis = (int)(Clock.get_ms() - anim.startMillis);

			BusAnimationSequenceSedna seq = buses.getBus(bus);

			if(seq != null) {
				double[] trans = seq.getTransformation(millis);

				if(trans != null)
					return trans;
			}
		}

		return new double[] {
			0, 0, 0, // position
			0, 0, 0, // rotation
			1, 1, 1, // scale
			0, 0, 0, // offset
			0, 1, 2, // XYZ order
		};
	}
}
