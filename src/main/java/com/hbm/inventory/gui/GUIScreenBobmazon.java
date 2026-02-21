package com.hbm.inventory.gui;

import com.hbm.Tags;
import com.hbm.main.AdvancementManager;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toserver.ItemBobmazonPacket;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hbm.util.SoundUtil.playClickSound;

public class GUIScreenBobmazon extends GuiScreen {
	
    protected static final ResourceLocation texture = new ResourceLocation(Tags.MODID + ":textures/gui/gui_bobmazon.png");
    protected int xSize = 176;
    protected int ySize = 229;
    protected int guiLeft;
    protected int guiTop;
    int currentPage = 0;
    List<Offer> offers = new ArrayList<Offer>();
    List<FolderButton> buttons = new ArrayList<FolderButton>();
    private final EntityPlayer player;
    
    public GUIScreenBobmazon(EntityPlayer player, List<Offer> offers) {
    	
    	this.player = player;

    	this.offers = offers;
    }
    
    int getPageCount() {
    	return (int)Math.ceil((offers.size() - 1) / 3);
    }
    
    public void updateScreen() {
    	if(currentPage < 0)
    		currentPage = 0;
    	if(currentPage > getPageCount())
    		currentPage = getPageCount();
    }
    
    public void drawScreen(int mouseX, int mouseY, float f)
    {
        this.drawDefaultBackground();
        this.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.enableLighting();
    }
    
    public void initGui()
    {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        updateButtons();
    }
    
    protected void updateButtons() {
        
        if(!buttons.isEmpty())
        	buttons.clear();
        
        for(int i = currentPage * 3; i < Math.min(currentPage * 3 + 3, offers.size()); i++) {
    		buttons.add(new FolderButton(guiLeft + 34, guiTop + 35 + (54 * (int)Math.floor(i)) - currentPage * 3 * 54, offers.get(i)));
        }

        if(currentPage != 0)
        	buttons.add(new FolderButton(guiLeft + 25 - 18, guiTop + 26 + (27 * 3), 1, "Previous"));
        if(currentPage != getPageCount())
        	buttons.add(new FolderButton(guiLeft + 25 + (27 * 4) + 18, guiTop + 26 + (27 * 3), 2, "Next"));
    }

    protected void mouseClicked(int i, int j, int k) {
    	try {
    		for(FolderButton b : buttons)
    			if(b.isMouseOnButton(i, j))
    				b.executeAction();
    	} catch (Exception ex) {
    		updateButtons();
    	}
    }
	
	protected void drawGuiContainerForegroundLayer(int i, int j) {

		this.fontRenderer.drawString(I18n.format((currentPage + 1) + "/" + (getPageCount() + 1)), 
				guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(I18n.format((currentPage + 1) + "/" + (getPageCount() + 1))) / 2, guiTop + 205, 4210752);
		
		for(FolderButton b : buttons)
			if(b.isMouseOnButton(i, j))
				b.drawString(i, j);
	}

	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(FolderButton b : buttons)
			b.drawButton(b.isMouseOnButton(i, j));
		for(FolderButton b : buttons)
			b.drawIcon(b.isMouseOnButton(i, j));
        
        for(int d = currentPage * 3; d < Math.min(currentPage * 3 + 3, offers.size()); d++) {
    		offers.get(d).drawRequirement(this, guiLeft + 34, guiTop + 53 + (54 * (int)Math.floor(d)) - currentPage * 3 * 54);
        }
	}
	
    protected void keyTyped(char p_73869_1_, int p_73869_2_)
    {
        if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.player.closeScreen();
        }
        
    }
	
	class FolderButton {
		
		int xPos;
		int yPos;
		//0: regular, 1: prev, 2: next
		int type;
		String info;
		Offer offer;
		
		public FolderButton(int x, int y, int t, String i) {
			xPos = x;
			yPos = y;
			type = t;
			info = i;
		}
		
		public FolderButton(int x, int y, Offer offer) {
			xPos = x;
			yPos = y;
			type = 0;
			this.offer = offer;
		}
		
		public void updateButton(int mouseX, int mouseY) {
		}
		
		public boolean isMouseOnButton(int mouseX, int mouseY) {
			return xPos <= mouseX && xPos + 18 > mouseX && yPos < mouseY && yPos + 18 >= mouseY;
		}
		
		public void drawButton(boolean b) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
			drawTexturedModalRect(xPos, yPos, b ? 176 + 18 : 176, type == 1 ? 18 : (type == 2 ? 36 : 0), 18, 18);
		}
		
		public void drawIcon(boolean b) {
			try {
		        RenderHelper.enableGUIStandardItemLighting();
				if(offer != null) {
					itemRender.renderItemAndEffectIntoGUI(player, offer.offer, xPos + 1, yPos + 1);
				}
				RenderHelper.disableStandardItemLighting();
			} catch(Exception x) { }
		}
		
		public void drawString(int x, int y) {
			if(info == null || info.isEmpty())
				return;
			
			drawHoveringText(Arrays.asList(new String[] { info }), x, y);
		}
		
		public void executeAction() {
			playClickSound();
			if(type == 0) {
				PacketDispatcher.wrapper.sendToServer(new ItemBobmazonPacket(player, offer));
			} else if(type == 1) {
				if(currentPage > 0)
					currentPage--;
				updateButtons();
			} else if(type == 2) {
				if(currentPage < getPageCount())
					currentPage++;
				updateButtons();
			}
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	public static class Offer {
		
		public ItemStack offer;
		public Requirement requirement;
		public int cost;
		public int rating;
		public String comment;
		public String author;

		public Offer(ItemStack offer, Requirement requirement, int cost, int rating, String comment, String author) {
			this.offer = offer;
			this.requirement = requirement;
			this.cost = cost;
			this.rating = rating * 4 - 1;
			this.comment = comment;
			this.author = author;
		}

		public Offer(ItemStack offer, Requirement requirement, int cost) {
			this(offer, requirement, cost, 0);
		}

		public Offer(ItemStack offer, Requirement requirement, int cost, int rating) {
			this(offer, requirement, cost, rating, "No Ratings", "");
		}
		
		public void drawRequirement(GUIScreenBobmazon gui, int x, int y) {
			try {

				RenderHelper.enableGUIStandardItemLighting();
				GlStateManager.color(1.0F, 1.0F, 1.0F);
				Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
				gui.drawTexturedModalRect(x + 19, y - 4, 176, 62, 39, 8);
				gui.drawTexturedModalRect(x + 19, y - 4, 176, 54, rating, 8);
				
				String count = "";
				if(!offer.isEmpty())
					count = " x" + offer.getCount();

				GlStateManager.pushMatrix();
				
				float scale = 0.65F;
				GlStateManager.scale(scale, scale, scale);
				gui.fontRenderer.drawString(I18n.format(offer.getDisplayName()) + count, (int)((x + 20) / scale), (int)((y - 12) / scale), 4210752);
				
				GlStateManager.popMatrix();
				
				String price = cost + " Cap";
				if(cost != 1)
					price += "s";

				gui.fontRenderer.drawString(price, x + 62, y - 3, 4210752);
				
				GlStateManager.pushMatrix();
					
					GlStateManager.scale(0.5F, 0.5F, 0.5F);
					
					if(!author.isEmpty())
						gui.fontRenderer.drawString("- " + author, (x + 20) * 2, (y + 18) * 2, 0x222222);
					gui.fontRenderer.drawString(comment, (x + 20) * 2, (y + 8) * 2, 0x222222);
					
				GlStateManager.popMatrix();

				GlStateManager.disableLighting();
				gui.itemRender.renderItemAndEffectIntoGUI(null, requirement.getIconStack(), x + 1, y + 1);
				GlStateManager.enableLighting();
		        
			} catch(Exception ex) { }
		}
		
	}

	public enum Requirement {

		STEEL(AdvancementManager.achBlastFurnace, "achblastfurnace"),
		ASSEMBLY(AdvancementManager.achAssembly, "achassembly"),
		CHEMICS(AdvancementManager.achChemplant, "achchemplant"),
		OIL(AdvancementManager.achDesh, "achdesh"),
		NUCLEAR(AdvancementManager.achTechnetium, "achtechnetium"),
		HIDDEN(AdvancementManager.bobHidden, "bobhidden"),
		NONE;

		private Advancement achievement;
		private String advName;
		private ResourceLocation advId;

		private Requirement() { }

		private Requirement(Advancement achievement, String advName) {
			this.setAchievement(achievement);
			this.advName = advName;
			this.advId = new ResourceLocation(Tags.MODID, advName);
		}

		public boolean fullfills(EntityPlayerMP player) {
			Advancement adv = getAchievement();
			if (adv == null) {
				return true;
			}
			return player.getAdvancements().getProgress(adv).isDone();
		}

		public Advancement getAchievement() {
			if (this == NONE) {
				return null;
			}
			if (FMLCommonHandler.instance().getSide().isServer()){
				return achievement;
			}
			return getAchClient();
		}

		@SideOnly(Side.CLIENT)
		private Advancement getAchClient(){
			if (this == NONE) {
				return null;
			}
			if (advId == null && advName != null) {
				advId = new ResourceLocation(Tags.MODID, advName);
			}
			return Minecraft.getMinecraft()
					.player.connection.getAdvancementManager()
					.getAdvancementList()
					.getAdvancement(advId);
		}

		public void setAchievement(Advancement achievement) {
			this.achievement = achievement;
		}

		public ItemStack getIconStack() {
			Advancement adv = getAchievement();
			if (adv != null && adv.getDisplay() != null) {
				return adv.getDisplay().getIcon();
			}
			return new ItemStack(Items.BOOK);
		}
	}

}
