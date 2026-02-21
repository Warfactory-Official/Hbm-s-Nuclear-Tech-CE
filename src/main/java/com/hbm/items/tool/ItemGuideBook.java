package com.hbm.items.tool;

import com.hbm.Tags;
import com.hbm.inventory.gui.GUIScreenGuide;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.I18nUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemGuideBook extends Item implements IGUIProvider {

	public ItemGuideBook(String s){
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public @NotNull ActionResult<ItemStack> onItemRightClick(World world, @NotNull EntityPlayer player, @NotNull EnumHand hand){
		if(world.isRemote) {
			BlockPos pos = player.getPosition();
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items){
		if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab())
			for(int i = 1; i < BookType.VALUES.length; i++)
				items.add(new ItemStack(this, 1, i));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, @NotNull ITooltipFlag flagIn){
		tooltip.add(String.join(" ", I18nUtil.resolveKeyArray(BookType.getType(stack.getItemDamage()).title)));
	}

	public enum BookType {

		TEST("book.test.cover", 2F, statFacTest()),
		RBMK("book.rbmk.cover", 1.5F, statFacRBMK()),
		MSWORD("book.msword.cover", 1.5F, statFacMSword()),
		HADRON("book.error.cover", 1.5F, statFacHadron()),
		STARTER("book.starter.cover", 1.5F, statFacStarter());

        public static final BookType[] VALUES = values();

		public final List<GuidePage> pages;
		public final float titleScale;
		public final String title;

		BookType(String title, float titleScale, List<GuidePage> pages) {
			this.title = title;
			this.titleScale = titleScale;
			this.pages = pages;
		}

		public static BookType getType(int i) {
			return BookType.VALUES[Math.abs(i) % BookType.VALUES.length];
		}
	}

	private static List<GuidePage> statFacTest() {

		List<GuidePage> pages = new ArrayList<>();
		pages.add(new GuidePage().addTitle("Title LMAO", 0x800000, 1F)
				.addText("book.test.page1", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/smileman.png"), 100, 40, 40));
		pages.add(new GuidePage().addTitle("LA SEXO", 0x800000, 0.5F)
				.addText("book.test.page1", 1.75F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/smileman.png"), 100, 40, 40));
		pages.add(new GuidePage().addText("test test"));
		pages.add(new GuidePage().addText("test test test"));
		pages.add(new GuidePage().addText("test test"));
		pages.add(new GuidePage().addText("test test test"));
		pages.add(new GuidePage().addText("test test"));
		return pages;
	}

	private static List<GuidePage> statFacHadron() {

		List<GuidePage> pages = new ArrayList<>();

		for(int i = 1; i <= 9; i++) {
			pages.add(new GuidePage().addText("book.error.page" + i, 2F).addTitle("book.error.title" + i, 0x800000, 1F));
		}

		return pages;
	}

	private static List<GuidePage> statFacRBMK() {

		List<GuidePage> pages = new ArrayList<>();
		pages.add(new GuidePage().addTitle("book.rbmk.title1", 0x800000, 1F)
				.addText("book.rbmk.page1", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk1.png"), 90, 80, 60));
		pages.add(new GuidePage().addTitle("book.rbmk.title2", 0x800000, 1F)
				.addText("book.rbmk.page2", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk2.png"), 95, 52, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title3", 0x800000, 1F)
				.addText("book.rbmk.page3", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk3.png"), 95, 88, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title4", 0x800000, 1F)
				.addText("book.rbmk.page4", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk4.png"), 95, 88, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title5", 0x800000, 1F)
				.addText("book.rbmk.page5", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk5.png"), 95, 80, 42));
		pages.add(new GuidePage().addTitle("book.rbmk.title6", 0x800000, 1F)
				.addText("book.rbmk.page6", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk6.png"), 90, 100, 60));
		pages.add(new GuidePage().addTitle("book.rbmk.title7", 0x800000, 1F)
				.addText("book.rbmk.page7", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk7.png"), 95, 52, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title8", 0x800000, 1F)
				.addText("book.rbmk.page8", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk8.png"), 95, 88, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title9", 0x800000, 1F)
				.addText("book.rbmk.page9", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk9.png"), 95, 88, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title10", 0x800000, 1F)
				.addText("book.rbmk.page10", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk10.png"), 95, 88, 52));
		pages.add(new GuidePage().addTitle("book.rbmk.title11", 0x800000, 1F)
				.addText("book.rbmk.page11", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk11.png"), 75, 85, 72));
		pages.add(new GuidePage().addTitle("book.rbmk.title12", 0x800000, 1F)
				.addText("book.rbmk.page12", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk12.png"), 90, 80, 60));
		pages.add(new GuidePage().addTitle("book.rbmk.title13", 0x800000, 1F)
				.addText("book.rbmk.page13", 2F));
		pages.add(new GuidePage()
				.addText("book.rbmk.page14", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk13.png"), 70, 103, 78));
		pages.add(new GuidePage().addTitle("book.rbmk.title15", 0x800000, 1F)
				.addText("book.rbmk.page15", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk15.png"), 100, 48, 48));
		pages.add(new GuidePage().addTitle("book.rbmk.title16", 0x800000, 1F)
				.addText("book.rbmk.page16", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/rbmk16.png"), 50, 70, 100));
		return pages;
	}

	private static class PageImageInfo {
		final int yOffset;
		final float ratio;
		PageImageInfo(int yOffset, float ratio) { this.yOffset = yOffset; this.ratio = ratio; }
	}

	private static List<GuidePage> statFacMSword() {
		int widthX = 100;
		List<GuidePage> pages = new ArrayList<>();

		pages.add(new GuidePage().addText("book.msword.page0", 2F).addTitle("book.msword.title0", 0x800000, 1F));

		PageImageInfo[] pageImageInfos = {
				new PageImageInfo(95, 64F/164F),    // 1
				new PageImageInfo(90, 64F/158F),    // 2
				new PageImageInfo(75, 62F/90F),     // 3
				new PageImageInfo(100, 26F/98F),    // 4
				new PageImageInfo(75, 62F/90F),     // 5
				new PageImageInfo(100, 26F/98F),    // 6
				new PageImageInfo(80, 62F/124F),    // 7
				new PageImageInfo(75, 62F/90F),     // 8
				new PageImageInfo(100, 26F/98F),    // 9
				new PageImageInfo(75, 64F/92F),     // 10
				new PageImageInfo(90, 64F/158F),    // 11
				new PageImageInfo(70, 118F/172F),   // 12
				new PageImageInfo(70, 118F/172F),   // 13
				new PageImageInfo(70, 118F/172F),   // 14
				new PageImageInfo(70, 118F/172F),   // 15
				new PageImageInfo(90, 63F/163F),    // 16
				new PageImageInfo(75, 63F/98F),     // 17
				new PageImageInfo(70, 80F/160F),    // 18
				null,                               // 19 (no image)
				new PageImageInfo(60, 118F/136F),   // 20
				new PageImageInfo(60, 118F/136F),   // 21
				new PageImageInfo(70, 122F/176F),   // 22
				new PageImageInfo(70, 122F/176F),   // 23
				new PageImageInfo(80, 60F/142F)     // 24
		};

		List<Integer> pagesWithoutTitle = Arrays.asList(13, 15, 19, 23);

		for (int i = 1; i <= 24; i++) {
			GuidePage page = new GuidePage().addText("book.msword.page" + i, 2F);
			if (!pagesWithoutTitle.contains(i)) {
				page.addTitle("book.msword.title" + i, 0x800000, 1F);
			}

			PageImageInfo imageInfo = pageImageInfos[i - 1];
			if (imageInfo != null) {
				String imageNumber = String.format("%02d", i);
				page.addImage(
						new ResourceLocation(Tags.MODID + ":textures/gui/book/guide_meteor_sword/" + imageNumber + ".png"),
						imageInfo.yOffset,
						widthX,
						(int)(widthX * imageInfo.ratio)
				);
			}
			pages.add(page);
		}
		return pages;
	}

	/**
	 * Mmm, maybe I should include something that allows you to have variable textures for the gui + item
	 * That would be something to do after the book is done though (nah, fuck that)
	 */
	private static List<GuidePage> statFacStarter() {

		List<GuidePage> pages = new ArrayList<>();

		pages.add(new GuidePage().addTitle("book.starter.title1", 0x800000, 1F)
				.addText("book.starter.page1", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter1.png"), 96, 101, 56));
		pages.add(new GuidePage().addTitle("book.starter.title2", 0x800000, 1F)
				.addText("book.starter.page2", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/mask_piss.png"), 85, 64, 64)); //meh
		pages.add(new GuidePage().addTitle("book.starter.title3", 0x800000, 1F)
				.addText("book.starter.page3", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter3.png"), 89, 100, 64));
		pages.add(new GuidePage().addTitle("book.starter.title4", 0x800000, 1F)
				.addText("book.starter.page4", 1.4F, 0, 6, 72)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/template_folder.png"), 72, 30, 24, 24)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/stamp_iron_flat.png"), 72, 60, 24, 24)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/assembly_template.png"), 72, 90, 24, 24)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/chemistry_template.png"), 72, 120, 24, 24));
		pages.add(new GuidePage().addTitle("book.starter.title5", 0x800000, 1F)
				.addText("book.starter.page5", 2F));
		pages.add(new GuidePage().addTitle("book.starter.title6", 0x800000, 1F)
				.addText("book.starter.page6a", 2F)
				.addText("book.starter.page6b", 2f, 0, 96, 100)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter6.png"), 9, 89, 84, 36));
		pages.add(new GuidePage()
				.addText("book.starter.page7a", 2F)
				.addText("book.starter.page7b", 2F, 0, 95, 100)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter7.png"), 9, 67, 84, 58));
		pages.add(new GuidePage().addTitle("book.starter.title8", 0x800000, 1F)
				.addText("book.starter.page8a", 2F, 0, -1, 50)
				.addText("book.starter.page8b", 2F, 50, 70, 50)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter8a.png"), 53, 36, 47, 61)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter8b.png"), 0, 102, 47, 61));
		pages.add(new GuidePage().addTitle("book.starter.title9", 0x800000, 1F)
				.addText("book.starter.page9", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/ingot_polymer.png"), 4, 106, 24, 24)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/ingot_desh.png"), 28, 130, 24, 24)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/solid_fuel_presto_triplet.png"), 52, 106, 24, 24)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/items/canister_gasoline.png"), 76, 130, 24, 24));
		pages.add(new GuidePage().addTitle("book.starter.title10", 0x800000, 1F)
				.addText("book.starter.page10", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter10.png"), 0, 115, 100, 39));
		pages.add(new GuidePage().addTitle("book.starter.title11", 0x800000, 1F)
				.addText("book.starter.page11", 2F, 0, -1, 60)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter11a.png"), 61, 36, 45, 57)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter11b.png"), 61, 97, 45, 57));
		pages.add(new GuidePage().addTitle("book.starter.title12", 0xfece00, 1F)
				.addText("book.starter.page12a", 3F)
				.addText("book.starter.page12b", 2F, 0, 20, 100));
		pages.add(new GuidePage().addTitle("book.starter.title13", 0x800000, 1F)
				.addText("book.starter.page13", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter13.png"), 110, 84, 42));
		pages.add(new GuidePage().addTitle("book.starter.title14", 0x800000, 1F)
				.addText("book.starter.page14", 2F, 0, 54, 100)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter14.png"), 34, 100, 46));
		pages.add(new GuidePage().addTitle("book.starter.title15", 0x800000, 1F)
				.addText("book.starter.page15", 2F));
		pages.add(new GuidePage().addTitle("book.starter.title16", 0x800000, 1F)
				.addText("book.starter.page16", 2F));
		pages.add(new GuidePage());
		pages.add(new GuidePage().addTitle("book.starter.title18", 0x800000, 1F)
				.addText("book.starter.page18", 2F)
				.addImage(new ResourceLocation(Tags.MODID + ":textures/gui/book/starter18.png"), 10, 69, 100, 100));

		return pages;
	}

	public static class GuidePage {

		public String title;
		public int titleColor;
		public float titleScale;

		public List<GuideText> texts = new ArrayList<>();
		public List<GuideImage> images = new ArrayList<>();

		GuidePage() { }

		GuidePage addTitle(String title, int color, float scale) {
			this.title = title;
			this.titleColor = color;
			this.titleScale = scale;
			return this;
		}

		GuidePage addText(String text) {
			texts.add(new GuideText(text));
			return this;
		}

		GuidePage addText(String text, float scale) {
			texts.add(new GuideText(text).setScale(scale));
			return this;
		}

		GuidePage addText(String text, float scale, int xOffset, int yOffset, int width) {
			texts.add(new GuideText(text).setSize(xOffset, yOffset, width).setScale(scale));
			return this;
		}

		GuidePage addImage(ResourceLocation image, int xOffset, int yOffset, int sizeX, int sizeY) {
			images.add(new GuideImage(image, xOffset, yOffset, sizeX, sizeY));
			return this;
		}

		//xOffset = -1 for automatic centering
        GuidePage addImage(ResourceLocation image, int yOffset, int sizeX, int sizeY) {
			images.add(new GuideImage(image, -1, yOffset, sizeX, sizeY));
			return this;
		}
	}

	public static class GuideText {
		public String text;
		public float scale = 1F;
		public int xOffset = 0;
		public int yOffset = -1;
		public int width = 100;

		GuideText(String text) {
			this.text = text;
		}

		public GuideText setScale(float scale) {
			this.scale = scale;
			return this;
		}

		//yOffset = -1, xOffset = 0 for default
		public GuideText setSize(int xOffset, int yOffset, int width) {
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.width = width;
			return this;
		}
	}

	public static class GuideImage {
		public ResourceLocation image;
		public int x;
		public int y;
		public int sizeX;
		public int sizeY;

		GuideImage(ResourceLocation image, int x, int y, int sizeX, int sizeY) {
			this.image = image;
			this.x = x;
			this.y = y;
			this.sizeX = sizeX;
			this.sizeY = sizeY;
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenGuide(player);
	}
}