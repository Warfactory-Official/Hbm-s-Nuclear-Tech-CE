package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.BedrockOreRegistry;
import com.hbm.items.ISatChip;
import com.hbm.items.tool.ItemSatInterface;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.SatLaserPacket;
import com.hbm.render.NTMRenderHelper;
import com.hbm.saveddata.satellites.Satellite.InterfaceActions;
import com.hbm.saveddata.satellites.Satellite.Interfaces;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.List;

public class GUIScreenSatInterface extends GuiScreen {
	
    protected static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/satellites/gui_sat_interface.png");
    protected int xSize = 216;
    protected int ySize = 216;
    protected int guiLeft;
    protected int guiTop;
    private final EntityPlayer player;
    int x;
    int z;
    
    public GUIScreenSatInterface(EntityPlayer player) {
    	
    	this.player = player;
    }
    
    public void updateScreen() {
    }

    protected void mouseClicked(int i, int j, int k) {
    	
    	if(ItemSatInterface.currentSat != null && ItemSatInterface.currentSat.ifaceAcs.contains(InterfaceActions.CAN_CLICK)) {

    		if(i >= this.guiLeft + 8 && i < this.guiLeft + 208 && j >= this.guiTop + 8 && j < this.guiTop + 208 && player != null) {
    			
    			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(HBMSoundHandler.techBleep, 1.0F));
    			
    			int x = this.x - guiLeft + i - 8 - 100;
    			int z = this.z - guiTop + j - 8 - 100;
    			PacketDispatcher.wrapper.sendToServer(new SatLaserPacket(x, z, ISatChip.getFreqS(player.getHeldItemMainhand())));
    		}
    	}
    }
    
    public void drawScreen(int mouseX, int mouseY, float f)
    {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
    }
    
    @Override
    public void drawBackground(int tint) {
    	super.drawDefaultBackground();
    	super.drawBackground(tint);
    }
    
    public void initGui()
    {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        
        x = (int) player.posX;
        z = (int) player.posZ;
    }
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	protected void drawGuiContainerForegroundLayer(int i, int j) {

    	if(ItemSatInterface.currentSat != null && ItemSatInterface.currentSat.ifaceAcs.contains(InterfaceActions.SHOW_COORDS)) {
    		
    		if(i >= this.guiLeft + 8 && i < this.guiLeft + 208 && j >= this.guiTop + 8 && j < this.guiTop + 208 && player != null) {

    			int x = this.x - guiLeft + i - 8 - 100;
    			int z = this.z - guiTop + j - 8 - 100;
    			drawHoveringText(Arrays.asList(new String[] { x + " / " + z }), i, j);
    		}
    	}
	}

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(ItemSatInterface.currentSat == null) {
			drawNotConnected();
		} else {
			
			if(ItemSatInterface.currentSat.satIface != Interfaces.SAT_PANEL) {
				drawNoService();
				return;
			}

			if(ItemSatInterface.currentSat.ifaceAcs.contains(InterfaceActions.HAS_MAP)) {
				drawMap();
			}
			if(ItemSatInterface.currentSat.ifaceAcs.contains(InterfaceActions.HAS_ORES)) {
				drawScan();
			}
			if(ItemSatInterface.currentSat.ifaceAcs.contains(InterfaceActions.HAS_RADAR)) {
				drawRadar();
			}
		}
	}
	
	private int scanPos = 0;
	private long lastMilli = 0;
	
	private void progresScan() {
		
		if(lastMilli + 15 < System.currentTimeMillis()) {
			lastMilli = System.currentTimeMillis();
			scanPos++;
		}
		
		if(scanPos >= 200)
			scanPos -= 200;
	}
	
	private int[][] map = new int[200][200];
	
	private void drawMap() {
		
		World world = player.world;
		
		for(int i = -100; i < 100; i++) {
			int x = this.x + i;
			int z = this.z + scanPos - 100;
			int y = world.getHeight(x, z) - 1;
			map[i + 100][scanPos] = world.getBlockState(new BlockPos(x, y, z)).getMaterial().getMaterialMapColor().colorValue;
		}
		prontMap();
		progresScan();
	}
	
	private void drawScan() {
		
		World world = player.world;
		
		for(int i = -100; i < 100; i++) {
			int x = this.x + i;
			int z = this.z + scanPos - 100;
			
			for(int j = (int)player.posY; j >= 0; j--) {
				IBlockState state = world.getBlockState(new BlockPos(x, j, z));
				int c = getColorFromBlock(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
				
				if(c != 0) {
					map[i + 100][scanPos] = c;
					break;
				}
			}
		}
		prontMap();
		progresScan();
	}
	
	private int getColorFromBlock(ItemStack stack) {
		
		if(stack == null || stack.getItem() == null || stack.isEmpty())
			return 0;

		int color = 0;
		for(int id : OreDictionary.getOreIDs(stack)){
			color = BedrockOreRegistry.getOreScanColor(OreDictionary.getOreName(id));
			if(color != 0) return color;
		}

		if(Block.getBlockFromItem(stack.getItem()) == ModBlocks.ore_bedrock_block)
			return 0xF84800;
		return 0;
	}
	
	private void drawRadar() {
		
		List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(player.posX - 100, 0, player.posZ - 100, player.posX + 100, 5000, player.posZ + 100));
		
		if(!entities.isEmpty()) {
			for(Entity e : entities) {
				
				if(e.width * e.width * e.height >= 0.5D) {
					int x = (int)((e.posX - this.x) / ((double)100 * 2 + 1) * (200D - 8D)) - 4;
					int z = (int)((e.posZ - this.z) / ((double)100 * 2 + 1) * (200D - 8D)) - 4 - 9;
					
					int t = 5;
					
					//TODO: fix radar screen implementation
					/*if(e instanceof EntityMissileBaseAdvanced) {
						t = ((EntityMissileBaseAdvanced)e).getMissileType().ordinal();
					}*/
					
					if(e instanceof EntityMob) {
						t = 6;
					}
					
					if(e instanceof EntityPlayer) {
						t = 7;
					}
	
					drawTexturedModalRect(guiLeft + 108 + x, guiTop + 117 + z, 216, 8 * t, 8, 8);
				}
			}
		}
		
	}
	
	private void prontMap() {
		for(int x = 0; x < 200; x++) {
			for(int z = 0; z < 200; z++) {
				NTMRenderHelper.setColor(map[x][z]);
				drawTexturedModalRect(guiLeft + 8 + x, guiTop + 8 + z, 216, 216, 1, 1);
			}
		}
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	private void drawNoService() {
		drawTexturedModalRect((this.width - 77) / 2, (this.height - 12) / 2, 0, 228, 77, 12);
	}
	
	private void drawNotConnected() {
		drawTexturedModalRect((this.width - 121) / 2, (this.height - 12) / 2, 0, 216, 121, 12);
	}
	
    protected void keyTyped(char p_73869_1_, int p_73869_2_)
    {
        if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.player.closeScreen();
        }
        
        if (p_73869_2_ == this.mc.gameSettings.keyBindForward.getKeyCode())
        {
            this.z -= 50;
            map = new int[200][200];
        }
        
        if (p_73869_2_ == this.mc.gameSettings.keyBindBack.getKeyCode())
        {
            this.z += 50;
            map = new int[200][200];
        }
        
        if (p_73869_2_ == this.mc.gameSettings.keyBindLeft.getKeyCode())
        {
            this.x -= 50;
            map = new int[200][200];
        }
        
        if (p_73869_2_ == this.mc.gameSettings.keyBindRight.getKeyCode())
        {
            this.x += 50;
            map = new int[200][200];
        }
        
    }

}