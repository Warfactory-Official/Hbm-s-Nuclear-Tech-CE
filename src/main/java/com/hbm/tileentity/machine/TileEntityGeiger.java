package com.hbm.tileentity.machine;

import com.hbm.handler.CompatHandler;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.HBMSoundHandler;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.Optional;
import java.util.ArrayList;
import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityGeiger extends TileEntity implements ITickable, SimpleComponent, CompatHandler.OCComponent {

	int timer = 0;
    double ticker = 0;
	
	
	@Override
	public void update() {
		timer++;
		
		if(timer == 10) {
			timer = 0;
			ticker = check();

            // To update the adjacent comparators
            world.updateComparatorOutputLevel(pos, blockType);
		}
		
		if(timer % 5 == 0) {
			if(ticker > 0) {
				List<Integer> list = new ArrayList<>();

                if(ticker < 1) list.add(0);
                if(ticker < 5) list.add(0);
                if(ticker < 10) list.add(1);
                if(ticker > 5 && ticker < 15) list.add(2);
                if(ticker > 10 && ticker < 20) list.add(3);
                if(ticker > 15 && ticker < 25) list.add(4);
                if(ticker > 20 && ticker < 30) list.add(5);
                if(ticker > 25) list.add(6);
			
				int r = list.get(world.rand.nextInt(list.size()));
				
				if(r > 0)
		        	world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.geigerSounds[r-1], SoundCategory.BLOCKS, 1.0F, 1.0F);
			} else if(world.rand.nextInt(50) == 0) {
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.geigerSounds[(world.rand.nextInt(1))], SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}
		
	}

    public double check() {
        return ChunkRadiationManager.proxy.getRadiation(world, pos);
    }

	// OpenComputers Integration
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "ntm_geiger";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "opencomputers")
	public Object[] getRads(Context context, Arguments args) {
		return new Object[] {check()};
	}

	@Optional.Method(modid = "opencomputers")
	public String[] methods() {
		return new String[]{
				"getRads"
		};
	}

	@Optional.Method(modid = "opencomputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case ("getRads"):
				return getRads(context, args);
		}
		throw new NoSuchMethodException();
	}
}
