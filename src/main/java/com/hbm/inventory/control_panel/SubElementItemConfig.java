package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.inventory.control_panel.controls.configs.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SubElementItemConfig extends SubElement {
    public static ResourceLocation bg_tex = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_base.png");

    public GuiButton btn_done;
    public GuiButton btn_next;
    public GuiButton btn_prev;

    public List<String> variants = Collections.emptyList();
    private int curr_variant = 0;
    private int num_variants = 1;

    SubElementBaseConfig config_gui;
    private Map<String, DataValue> configs;

    public SubElementItemConfig(GuiControlEdit gui) {
        super(gui);
    }

    @Override
    protected void initGui() {
        int cX = gui.width/2;
        int cY = gui.height/2;
        btn_prev = gui.addButton(new GuiButton(gui.currentButtonId(), cX-30, gui.getGuiTop()+24, 15, 20, "<"));
        btn_next = gui.addButton(new GuiButton(gui.currentButtonId(), cX+15, gui.getGuiTop()+24, 15, 20, ">"));
        btn_done = gui.addButton(new GuiButton(gui.currentButtonId(), cX-85, cY+92, 170, 20, "Done"));

        this.config_gui = new SubElementDisplaySevenSeg(gui, ControlRegistry.registry.get("display_7seg").getConfigs());
        this.config_gui.initGui();
        super.initGui();
    }

    Control last_control = null;
    Map<String, DataValue> existing_configs;

    //TODO: clean this up, make variants[] private
    @Override
    protected void drawScreen() {
        int cX = gui.width/2;
        int cY = gui.height/2;

        num_variants = variants.size()-1;

        if (gui.isEditMode) {
            existing_configs = gui.currentEditControl.getConfigs();
            curr_variant = variants.indexOf(ControlRegistry.getName(gui.currentEditControl.getClass()));
        }
        btn_prev.enabled = !gui.isEditMode;
        btn_next.enabled = !gui.isEditMode;

        if (curr_variant > variants.size()-1)
            curr_variant = 0;
        Control variant = ControlRegistry.getNew(variants.get(curr_variant), gui.control.panel);

        String text = variant.name;
        int text_width = gui.getFontRenderer().getStringWidth(text);
        gui.getFontRenderer().drawString(text, (cX-(text_width/2F))+0, gui.getGuiTop()+11, 0xFF777777, false);

        text = (curr_variant+1) + "/" + (num_variants+1);
        text_width = gui.getFontRenderer().getStringWidth(text);
        gui.getFontRenderer().drawString(text, (cX-(text_width/2F))+0, gui.getGuiTop()+30, 0xFF777777, false);

        if (last_control == null || !variant.name.equals(last_control.name)) {
            this.config_gui.enableButtons(false);
            switch (variants.get(curr_variant)) { //TODO: clean
                case "display_7seg": {
                    this.config_gui = new SubElementDisplaySevenSeg(gui, (gui.isEditMode) ? existing_configs : ControlRegistry.registry.get("display_7seg").getConfigs());
                    break;
                }
                case "display_text": {
                    this.config_gui = new SubElementDisplayText(gui, (gui.isEditMode) ? existing_configs : ControlRegistry.registry.get("display_text").getConfigs());
                    break;
                }
                case "knob_control": {
                    this.config_gui = new SubElementKnobControl(gui, (gui.isEditMode) ? existing_configs : ControlRegistry.registry.get("knob_control").getConfigs());
                    break;
                }
                case "label": {
                    this.config_gui = new SubElementLabel(gui, (gui.isEditMode) ? existing_configs : ControlRegistry.registry.get("label").getConfigs());
                    break;
                }
                case "dial_square": {
                    this.config_gui = new SubElementDialSquare(gui, (gui.isEditMode) ? existing_configs : ControlRegistry.registry.get("dial_square").getConfigs());
                    break;
                }
                case "dial_large": {
                    this.config_gui = new SubElementDialSquare(gui, (gui.isEditMode) ? existing_configs : ControlRegistry.registry.get("dial_large").getConfigs());
                    break;
                }
                default:
                    this.config_gui = new SubElementBaseConfig(gui); // blank, shows for variant selection
            }
            if (!gui.isEditMode) {
                gui.currentEditControl = variant;
            }

            this.config_gui.initGui();
            this.config_gui.enableButtons(true);

           variants = ControlRegistry.getAllControlsOfType(gui.currentEditControl.getControlType());
        }
        this.config_gui.drawScreen();

        this.last_control = variant;
    }

    @Override
    protected void renderBackground() {
        gui.mc.getTextureManager().bindTexture(bg_tex);
        gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
    }

    @Override
    protected void update() {
        config_gui.update();
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        config_gui.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        config_gui.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        config_gui.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == btn_done) {
            configs = config_gui.getConfigs();
            gui.currentEditControl.applyConfigs(configs);

            if (gui.isEditMode) {
                gui.linker.linked.clear();
                World world = gui.control.getWorld();
                for (BlockPos p : gui.currentEditControl.connectedSet) {
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof IControllable)
                        gui.linker.linked.add((IControllable) te);
                    gui.linker.refreshButtons();
                }
            }

            config_gui.enableButtons(false);
            last_control = null;
            gui.pushElement(gui.linker);
        }
        else if (button == btn_next) {
            curr_variant = Math.min(num_variants, curr_variant+1);
        }
        else if (button == btn_prev) {
            curr_variant = Math.max(0, curr_variant-1);
        }
    }

    @Override
    protected void enableButtons(boolean enable) {
        btn_done.visible = enable;
        btn_done.enabled = enable;
        btn_next.visible = enable;
        btn_next.enabled = enable;
        btn_prev.visible = enable;
        btn_prev.enabled = enable;
    }
}
