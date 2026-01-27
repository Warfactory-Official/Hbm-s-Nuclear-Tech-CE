package com.hbm.render;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.GroupObject;
import com.hbm.render.loader.IModelCustom;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Deprecated //We should use VAOs for this, not displaylists
public class WavefrontObjDisplayList implements IModelCustom {

    public List<Pair<String, Integer>> nameToCallList = new ArrayList<>();



    public WavefrontObjDisplayList(HFRWavefrontObject obj) {
        for(GroupObject g : obj.groupObjects){
            int list = GL11.glGenLists(1);
            GL11.glNewList(list, GL11.GL_COMPILE);
            g.render();
            GL11.glEndList();
            nameToCallList.add(Pair.of(g.name, list));
        }
    }

    public int getListForName(String name){
        for(Pair<String, Integer> p : nameToCallList){
            if(p.getLeft().equalsIgnoreCase(name)){
                return p.getRight();
            }
        }
        return 0;
    }

    @Override
    public String getType() {
        return "obj_list";
    }

    @Override
    public void renderAll() {
        for(Pair<String, Integer> p : nameToCallList)
            GL11.glCallList(p.getRight());
    }

    @Override
    public void renderOnly(String... groupNames) {
        for(Pair<String, Integer> p : nameToCallList){
            for(String name : groupNames){
                if(p.getLeft().equalsIgnoreCase(name)){
                    GL11.glCallList(p.getRight());
                    break;
                }
            }
        }
    }

    @Override
    public void renderPart(String partName) {
        for(Pair<String, Integer> p : nameToCallList){
            if(p.getLeft().equalsIgnoreCase(partName)){
                GL11.glCallList(p.getRight());
            }
        }
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames) {
        for(Pair<String, Integer> p : nameToCallList){
            boolean skip = false;
            for(String name : excludedGroupNames){
                if(p.getLeft().equalsIgnoreCase(name)){
                    skip = true;
                    break;
                }
            }
            if(!skip){
                GL11.glCallList(p.getRight());
            }
        }
    }


}
