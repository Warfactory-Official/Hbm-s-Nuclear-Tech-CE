package com.hbm.inventory.control_panel;

import com.hbm.Tags;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

import static com.hbm.inventory.control_panel.DataValue.DataType.ENUM;
import static com.hbm.inventory.control_panel.DataValue.DataType.NUMBER;

// sorry.
public class SubElementVariables extends SubElement {
    public static ResourceLocation list_bg = new ResourceLocation(Tags.MODID + ":textures/gui/control_panel/gui_variable_list_front.png");

    GuiButton btn_clearAll;
    GuiButton btn_back;
    GuiButton btn_newNumber;
    GuiButton btn_newString;
    GuiButton btn_local;
    GuiButton btn_global;
    GuiButton btn_confirmNewVar;
    GuiButton btn_nextPage;
    GuiButton btn_prevPage;

    GuiTextField txt_newVarName;
    GuiTextField txt_newVarData;

    DataValue newVarData;

    public int numPages = 1;
    public int currentPage = 1;

    int list_offset = 0;
    boolean isGlobalScope = true;
    boolean isCreatingNewVar = false;

    Map<String, GuiButton> btns_var_delete = new HashMap<>();
    Map<String, GuiTextField> txts_var_data = new HashMap<>();

    public SubElementVariables(GuiControlEdit gui) {
        super(gui);
        newVarData = new DataValueFloat(0);
    }

    @Override
    protected void initGui() {
        int cY = gui.height/2;
        int gLeft = gui.getGuiLeft();
        int gTop = gui.getGuiTop();

        btn_back = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+7, gTop+13, 30, 20, "Back"));
        btn_newString = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+7, gTop+68, 43, 20, "String"));
        btn_newNumber = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+7, gTop+91, 43, 20, "Number"));
        btn_local = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+59, gTop+17, 65, 20, "Local"));
        btn_global = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+59, gTop+17, 65, 20, "Global"));
        btn_clearAll = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+132, gTop+17, 43, 20, "Clear"));
        btn_prevPage = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+186, gTop+17, 15, 20, "<"));
        btn_nextPage = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+231, gTop+17, 15, 20, ">"));
        btn_confirmNewVar = gui.addButton(new GuiButton(gui.currentButtonId(), gLeft+223, cY-80, 20, 20, "+"));
        btn_confirmNewVar.packedFGColour = 0x5dac5d;
        Keyboard.enableRepeatEvents(true);
        txt_newVarName = new GuiTextField(gui.currentButtonId(), gui.getFontRenderer(), gLeft+77, cY-80, 60, 20);
        txt_newVarName.setText("name");
        txt_newVarData = new GuiTextField(gui.currentButtonId(), gui.getFontRenderer(), gLeft+152, cY-80, 65, 20);

        super.initGui();
    }

    @Override
    protected void renderBackground() {
        gui.mc.getTextureManager().bindTexture(list_bg);
        gui.drawTexturedModalRect(gui.getGuiLeft(), gui.getGuiTop(), 0, 0, gui.getXSize(), gui.getYSize());
    }

    @Override
    protected void update() {
        super.update();
        for (Map.Entry<String, GuiTextField> e : txts_var_data.entrySet()) {
            e.getValue().updateCursorCounter();
        }
    }

    @Override
    protected void drawScreen() {
        int cY = gui.height/2;

        String text = currentPage + "/" + numPages;
        gui.getFontRenderer().drawString(text, (gui.getGuiLeft()+216) - gui.getFontRenderer().getStringWidth(text)/2F, cY-103, 0xFF777777, false);

        if (isGlobalScope) {
            drawVarList(gui.control.panel.globalVars);
        } else {
            drawVarList(gui.currentEditControl.vars);
        }

        if (isCreatingNewVar) {
            drawCreateVar();
        }

        btn_confirmNewVar.enabled = isCreatingNewVar;

        if (gui.currentEditControl != null) {
            btn_global.enabled = isGlobalScope;
            btn_global.visible = isGlobalScope;
            btn_local.enabled = !isGlobalScope;
            btn_local.visible = !isGlobalScope;
        } else {
            btn_global.enabled = false;
            btn_global.visible = true;
            btn_local.enabled = false;
            btn_local.visible = false;
        }
    }

    protected void drawVarList(Map<String, DataValue> varList) {
        int cY = gui.height/2 + this.list_offset;

        gui.getButtons().removeAll(btns_var_delete.values());
        btns_var_delete.clear();

        String text;
        int i = 0;

        for (Map.Entry<String, DataValue> e : varList.entrySet()) {
            int j = i%6;
            text = e.getKey();

            btns_var_delete.put(e.getKey(), new GuiButton(gui.currentButtonId(), gui.getGuiLeft()+60, (cY-52)+(j*28), 20, 20, "X"));
            GuiButton btn_del = btns_var_delete.get(e.getKey());
            btn_del.enabled = false;
            btn_del.visible = false;

            if ((i >= (currentPage-1)*6) && (i < ((currentPage-1)*6)+6)) {
                GlStateManager.disableTexture2D();
                GlStateManager.color(.15F, .19F, .14F, .2F);
                NTMRenderHelper.drawGuiRect(gui.getGuiLeft() + 55, (cY - 54.5F) + (j * 28), 0, 0, 193, 25, 0, 0);
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableTexture2D();

                if (isGlobalScope || gui.currentEditControl.customVarNames.contains(e.getKey())) {
                    btn_del.enabled = true;
                }
                btn_del.visible = true;

                gui.getFontRenderer().drawString((gui.getFontRenderer().getStringWidth(text) > 70) ? text.substring(0, 10) + "..." : text, gui.getGuiLeft() + 87, (cY - 46) + (j * 28), 0xFFFFFFFF, false);
                gui.getFontRenderer().drawString(" = ", gui.getGuiLeft() + 154, (cY - 46) + (j * 28), 0xFFFFFFFF, false);

                if (txts_var_data.containsKey(e.getKey())) {
                    if (txts_var_data.get(e.getKey()).isFocused()) {
                        DataValue.DataType type = varList.get(e.getKey()).getType();
                        switch (type) {
                            case NUMBER: varList.put(e.getKey(), new DataValueFloat(Float.parseFloat((txts_var_data.get(e.getKey()).getText().isEmpty())? "0" : txts_var_data.get(e.getKey()).getText()))); break;
                            case STRING: varList.put(e.getKey(), new DataValueString(txts_var_data.get(e.getKey()).getText())); break;
                        }
                    }
                } else {
                    GuiTextField txt = new GuiTextField(3000 + i, gui.getFontRenderer(), gui.getGuiLeft() + 167, (cY - 52) + (j * 28), 75, 20);
                    txt.setText(e.getValue().toString());
                    txt.setTextColor((e.getValue().getType() == NUMBER) ? 0x99CCFF : 0xFFFF99);
                    txts_var_data.put(e.getKey(), txt);
                }
            }

            i++;
        }
        numPages = (int) Math.ceil(Math.max(1, btns_var_delete.size()) / 6F);
        currentPage = MathHelper.clamp(currentPage, 1, numPages);

        gui.getButtons().addAll(btns_var_delete.values());

        for (Map.Entry<String, GuiTextField> txt : txts_var_data.entrySet()) {
            txt.getValue().drawTextBox();
        }
    }

    protected void drawCreateVar() {
        int cY = gui.height/2;

        String text = "";
        switch (newVarData.getType()) {
            case NUMBER : text = "N : "; break;
            case STRING : text = "S : "; break;
        }
        gui.getFontRenderer().drawString(text, gui.getGuiLeft()+60, (cY-74), 0xFFFFFFFF, false);
        txt_newVarName.drawTextBox();
        gui.getFontRenderer().drawString(" = ", gui.getGuiLeft()+138, (cY-74), 0xFFFFFFFF, false);
        txt_newVarData.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.txt_newVarName.mouseClicked(mouseX, mouseY, button);
        this.txt_newVarData.mouseClicked(mouseX, mouseY, button);

        for (Map.Entry<String, GuiTextField> field : txts_var_data.entrySet()) {
            //TODO: support enums, probably just cycle through values on press
            if (isGlobalScope || (gui.currentEditControl.vars.get(field.getKey()).getType() != ENUM)) {
                field.getValue().mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        this.txt_newVarName.textboxKeyTyped(typedChar, keyCode);
        this.txt_newVarData.textboxKeyTyped(typedChar, keyCode);

        final boolean isDigitOrControlChar = Character.isDigit(typedChar) || Character.getType(typedChar) == Character.CONTROL;

        for (Map.Entry<String, GuiTextField> field : txts_var_data.entrySet()) {
            final boolean isNumberType = (isGlobalScope && gui.control.panel.globalVars.get(field.getKey()).getType() == NUMBER)
                    || (!isGlobalScope && gui.currentEditControl.vars.get(field.getKey()).getType() == NUMBER);

            if (!isNumberType || isDigitOrControlChar) {
                field.getValue().textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == btn_newNumber) {
            newVarData = new DataValueFloat(0);
            txt_newVarData.setText(newVarData.toString());
            isCreatingNewVar = true;
        }
        else if (button == btn_newString) {
            newVarData = new DataValueString("");
            txt_newVarData.setText(newVarData.toString());
            isCreatingNewVar = true;
        }
        else if (button == btn_prevPage) {
            currentPage = Math.max(1, currentPage-1);
            txts_var_data.clear();
        }
        else if (button == btn_nextPage) {
            currentPage = Math.min(numPages, currentPage+1);
            txts_var_data.clear();
        }
        else if (button == btn_clearAll) {
            if (isGlobalScope) {
                gui.control.panel.globalVars.clear();
            } else {
                for (String s : gui.currentEditControl.customVarNames) {
                    gui.currentEditControl.vars.remove(s);
                }
            }
            txts_var_data.clear();
        }
        else if (button == btn_local || button == btn_global) {
            isGlobalScope = !isGlobalScope;
            txts_var_data.clear();
        }
        else if (button == btn_back) {
            // hack to unfuck another hack, see: SubElementPlacement:242
            if (!gui.subElementStack.contains(gui.linker)) {
                gui.currentEditControl = null;
            }
            gui.popElement();
        }
        else if (button == btn_confirmNewVar) {
            if (txt_newVarName.getText().isEmpty() || txt_newVarData.getText().isEmpty()) {
                return;
            }
            isCreatingNewVar = false;
            if (isGlobalScope) {
                switch (newVarData.getType()) {
                    case NUMBER: {
                        gui.control.panel.globalVars.put(txt_newVarName.getText(), new DataValueFloat(Float.parseFloat(txt_newVarData.getText())));
                        break;
                    }
                    case STRING: {
                        gui.control.panel.globalVars.put(txt_newVarName.getText(), new DataValueString(txt_newVarData.getText()));
                        break;
                    }
                }
            } else {
                switch (newVarData.getType()) {
                    case NUMBER: {
                        gui.currentEditControl.vars.put(txt_newVarName.getText(), new DataValueFloat(Float.parseFloat(txt_newVarData.getText())));
                        gui.currentEditControl.customVarNames.add(txt_newVarName.getText());
                        break;
                    }
                    case STRING: {
                        gui.currentEditControl.vars.put(txt_newVarName.getText(), new DataValueString(txt_newVarData.getText()));
                        gui.currentEditControl.customVarNames.add(txt_newVarName.getText());
                        break;
                    }
                }
            }
            txt_newVarName.setText("name");
            txts_var_data.clear();
        }
        else if (btns_var_delete.containsValue(button)) {
            for (Map.Entry<String, GuiButton> e : btns_var_delete.entrySet()) {
                if (e.getValue().equals(button)) {
                    if (isGlobalScope) {
                        gui.control.panel.globalVars.remove(e.getKey());
                    } else {
                        gui.currentEditControl.vars.remove(e.getKey());
                        gui.currentEditControl.customVarNames.remove(e.getKey());
                    }
                    txts_var_data.clear();
                }
            }
        }
    }

    @Override
    protected void enableButtons(boolean enable) {
        btn_clearAll.enabled = enable;
        btn_clearAll.visible = enable;
        btn_back.enabled = enable;
        btn_back.visible = enable;
        btn_nextPage.enabled = enable;
        btn_nextPage.visible = enable;
        btn_prevPage.enabled = enable;
        btn_prevPage.visible = enable;
        btn_newNumber.enabled = enable;
        btn_newNumber.visible = enable;
        btn_newString.enabled = enable;
        btn_newString.visible = enable;
        btn_local.enabled = enable;
        btn_local.visible = enable;
        btn_global.enabled = enable;
        btn_global.visible = enable;
        btn_confirmNewVar.enabled = enable;
        btn_confirmNewVar.visible = enable;

        txt_newVarName.setEnabled(enable);
        txt_newVarName.setVisible(enable);
        txt_newVarData.setEnabled(enable);
        txt_newVarData.setVisible(enable);

        for (Map.Entry<String, GuiTextField> e : txts_var_data.entrySet()) {
            e.getValue().setEnabled(enable);
            e.getValue().setVisible(enable);
        }

        for (Map.Entry<String, GuiButton> b : btns_var_delete.entrySet()) {
            b.getValue().enabled = enable;
            b.getValue().visible = enable;
        }
    }
}
