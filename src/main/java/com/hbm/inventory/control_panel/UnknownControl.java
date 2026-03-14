package com.hbm.inventory.control_panel;

import com.hbm.inventory.control_panel.controls.ControlType;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustom;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

/**
 * for round-tripping in case someone added and removed an addon and wants to add it back later
 *
 * @author mlbv
 */
public class UnknownControl extends Control {

	private static final ControlType UNKNOWN = ControlType.registerOrGet("Unknown");
	private static final float MIN_PLACEHOLDER_SPAN = 0.25F;

	private NBTTagCompound rawTag;
	private float boxMinX;
	private float boxMinY;
	private float boxMaxX;
	private float boxMaxY;
	private float boxHeight;
	private AxisAlignedBB boundingBox;

	public UnknownControl(ControlPanel panel, NBTTagCompound rawTag) {
		super(resolveDisplayName(rawTag), rawTag.getString("name"), panel);
		loadRawTag(rawTag);
	}

	private static String resolveDisplayName(NBTTagCompound rawTag) {
		if(rawTag.hasKey("myName")) {
			return rawTag.getString("myName");
		}
		String registryName = rawTag.getString("name");
		return registryName.isEmpty() ? "Unknown Control" : registryName;
	}

	private void loadRawTag(NBTTagCompound tag) {
		this.rawTag = tag.copy();
		this.name = resolveDisplayName(this.rawTag);
		this.posX = this.rawTag.getFloat("X");
		this.posY = this.rawTag.getFloat("Y");
		loadPlaceholderMeta(this.rawTag.getCompoundTag(PLACEHOLDER_META_TAG));
	}

	@Override
	public ControlType getControlType() {
		return UNKNOWN;
	}

	@Override
	public float[] getSize() {
		return new float[] {Math.max(boxMaxX - boxMinX, 0.1F), Math.max(boxMaxY - boxMinY, 0.1F), boxHeight};
	}

	@Override
	public void fillBox(float[] box) {
		box[0] = boxMinX;
		box[1] = boxMinY;
		box[2] = boxMaxX;
		box[3] = boxMaxY;
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Control newControl(ControlPanel panel) {
		return new UnknownControl(panel, rawTag);
	}

	@Override
	public void populateDefaultNodes(List<ControlEvent> receiveEvents) {
	}

	@Override
	public List<String> getInEvents() {
		return Collections.emptyList();
	}

	@Override
	public void render() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IModelCustom getModel() {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getGuiTexture() {
		return ResourceManager.white;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		NBTTagCompound copy = rawTag.copy();
		copy.setString("myName", name);
		copy.setFloat("X", posX);
		copy.setFloat("Y", posY);
		copy.setTag(PLACEHOLDER_META_TAG, writePlaceholderMeta());
		return copy;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		loadRawTag(tag);
	}

	private void loadPlaceholderMeta(NBTTagCompound placeholderMeta) {
		this.boxMinX = placeholderMeta.hasKey(PLACEHOLDER_BOX_MIN_X) ? placeholderMeta.getFloat(PLACEHOLDER_BOX_MIN_X) : this.posX;
		this.boxMinY = placeholderMeta.hasKey(PLACEHOLDER_BOX_MIN_Y) ? placeholderMeta.getFloat(PLACEHOLDER_BOX_MIN_Y) : this.posY;
		this.boxMaxX = placeholderMeta.hasKey(PLACEHOLDER_BOX_MAX_X) ? placeholderMeta.getFloat(PLACEHOLDER_BOX_MAX_X) : this.posX + 1;
		this.boxMaxY = placeholderMeta.hasKey(PLACEHOLDER_BOX_MAX_Y) ? placeholderMeta.getFloat(PLACEHOLDER_BOX_MAX_Y) : this.posY + 1;
		if(this.boxMaxX <= this.boxMinX) {
			this.boxMaxX = this.boxMinX + MIN_PLACEHOLDER_SPAN;
		}
		if(this.boxMaxY <= this.boxMinY) {
			this.boxMaxY = this.boxMinY + MIN_PLACEHOLDER_SPAN;
		}
		this.boxHeight = placeholderMeta.hasKey(PLACEHOLDER_SIZE_HEIGHT) ? Math.max(placeholderMeta.getFloat(PLACEHOLDER_SIZE_HEIGHT), 0.1F) : 0.1F;

		if(placeholderMeta.hasKey(PLACEHOLDER_HAS_BOUNDING_BOX) && !placeholderMeta.getBoolean(PLACEHOLDER_HAS_BOUNDING_BOX)) {
			this.boundingBox = null;
			return;
		}
		if(placeholderMeta.hasKey(PLACEHOLDER_BOUNDS_MIN_X)
				&& placeholderMeta.hasKey(PLACEHOLDER_BOUNDS_MIN_Y)
				&& placeholderMeta.hasKey(PLACEHOLDER_BOUNDS_MIN_Z)
				&& placeholderMeta.hasKey(PLACEHOLDER_BOUNDS_MAX_X)
				&& placeholderMeta.hasKey(PLACEHOLDER_BOUNDS_MAX_Y)
				&& placeholderMeta.hasKey(PLACEHOLDER_BOUNDS_MAX_Z)) {
			this.boundingBox = new AxisAlignedBB(
					placeholderMeta.getDouble(PLACEHOLDER_BOUNDS_MIN_X),
					placeholderMeta.getDouble(PLACEHOLDER_BOUNDS_MIN_Y),
					placeholderMeta.getDouble(PLACEHOLDER_BOUNDS_MIN_Z),
					placeholderMeta.getDouble(PLACEHOLDER_BOUNDS_MAX_X),
					placeholderMeta.getDouble(PLACEHOLDER_BOUNDS_MAX_Y),
					placeholderMeta.getDouble(PLACEHOLDER_BOUNDS_MAX_Z)
			);
			this.boxHeight = Math.max(this.boxHeight, (float) Math.max(0.1D, this.boundingBox.maxY - this.boundingBox.minY));
			return;
		}
		this.boundingBox = createFallbackBoundingBox();
	}

	private AxisAlignedBB createFallbackBoundingBox() {
		return new AxisAlignedBB(boxMinX, 0, boxMinY, boxMaxX, boxHeight, boxMaxY);
	}

	private NBTTagCompound writePlaceholderMeta() {
		NBTTagCompound placeholderMeta = new NBTTagCompound();
		placeholderMeta.setFloat(PLACEHOLDER_BOX_MIN_X, boxMinX);
		placeholderMeta.setFloat(PLACEHOLDER_BOX_MIN_Y, boxMinY);
		placeholderMeta.setFloat(PLACEHOLDER_BOX_MAX_X, boxMaxX);
		placeholderMeta.setFloat(PLACEHOLDER_BOX_MAX_Y, boxMaxY);
		placeholderMeta.setFloat(PLACEHOLDER_SIZE_HEIGHT, boxHeight);
		placeholderMeta.setBoolean(PLACEHOLDER_HAS_BOUNDING_BOX, boundingBox != null);
		if(boundingBox != null) {
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MIN_X, boundingBox.minX);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MIN_Y, boundingBox.minY);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MIN_Z, boundingBox.minZ);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MAX_X, boundingBox.maxX);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MAX_Y, boundingBox.maxY);
			placeholderMeta.setDouble(PLACEHOLDER_BOUNDS_MAX_Z, boundingBox.maxZ);
		}
		return placeholderMeta;
	}
}
